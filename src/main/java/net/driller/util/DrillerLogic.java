package net.driller.util;

import net.driller.access.MinecartAccess;
import net.driller.mixin.access.MinecartFurnaceAccessor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.entity.vehicle.MinecartChest;
import net.minecraft.world.entity.vehicle.MinecartFurnace;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.RailShape;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;

import java.util.*;

public class DrillerLogic {

    private static final int TUNNEL_WIDTH = 3;
    private static final int TUNNEL_HEIGHT = 3;
    private static final int TICKS_PER_BLOCK = 20;
    private static final int RAIL_CHECK_DISTANCE = 3;

    private static final Map<UUID, BorerState> borerStates = new HashMap<>();
    private static final Map<UUID, Long> lastActionTime = new HashMap<>();
    private static final Map<UUID, Integer> currentBlockIndex = new HashMap<>();
    private static final Map<UUID, Long> lastFuelCheck = new HashMap<>();
    private static final Map<UUID, Direction> lastKnownDirection = new HashMap<>();
    private static final Map<UUID, Long> lastRailCheck = new HashMap<>();

    public enum BorerState {
        MOVING_TO_WALL,
        BREAKING_LAYER,
        PLACING_RAIL,
        MOVING_FORWARD,
        EXTENDING_RAILS
    }

    public static void processTunnelBoring(AbstractMinecart minecart) {
        if (!(minecart instanceof MinecartFurnace furnaceCart)) {
            return;
        }

        MinecartAccess accessor = (MinecartAccess) minecart;
        if (!accessor.isTunnelBorerEnabled()) {
            borerStates.remove(minecart.getUUID());
            lastKnownDirection.remove(minecart.getUUID());
            return;
        }

        Level world = minecart.level();
        if (world.isClientSide()) {
            return;
        }

        Vec3 movement = furnaceCart.getDeltaMovement();
        if (movement.horizontalDistanceSqr() > 0.001) {
            Direction currentDir = getDirectionFromMovement(movement);
            if (currentDir != null) {
                lastKnownDirection.put(furnaceCart.getUUID(), currentDir);
            }
        }

        long currentTime = world.getGameTime();
        long lastFuel = lastFuelCheck.getOrDefault(minecart.getUUID(), 0L);
        if (currentTime - lastFuel >= 100) {
            lastFuelCheck.put(minecart.getUUID(), currentTime);
            refuelFurnace(furnaceCart);
        }

        BorerState currentState = borerStates.getOrDefault(minecart.getUUID(), BorerState.MOVING_TO_WALL);

        if (currentState == BorerState.MOVING_TO_WALL) {
            long lastRailCheckTime = lastRailCheck.getOrDefault(minecart.getUUID(), 0L);
            if (currentTime - lastRailCheckTime >= 10) {
                lastRailCheck.put(minecart.getUUID(), currentTime);

                if (checkRailsAhead(furnaceCart)) {
                    System.out.println("DEBUG: Rails ending ahead! Switching to EXTENDING_RAILS");
                    borerStates.put(furnaceCart.getUUID(), BorerState.EXTENDING_RAILS);
                    furnaceCart.setDeltaMovement(Vec3.ZERO);
                }
            }
        }

        switch (currentState) {
            case MOVING_TO_WALL -> handleMovingToWall(furnaceCart, accessor);
            case BREAKING_LAYER -> handleBreakingLayer(furnaceCart, accessor);
            case PLACING_RAIL -> handlePlacingRail(furnaceCart, accessor);
            case MOVING_FORWARD -> handleMovingForward(furnaceCart, accessor);
            case EXTENDING_RAILS -> handleExtendingRails(furnaceCart, accessor);
        }
    }

    private static boolean checkRailsAhead(MinecartFurnace furnaceCart) {
        Level world = furnaceCart.level();
        Direction facing = getFacingDirection(furnaceCart);

        if (facing == null) {
            return false;
        }

        BlockPos currentPos = furnaceCart.blockPosition();

        for (int distance = 1; distance <= RAIL_CHECK_DISTANCE; distance++) {
            BlockPos checkPos = currentPos.relative(facing, distance);
            BlockState state = world.getBlockState(checkPos);

            if (!(state.getBlock() instanceof BaseRailBlock)) {
                BlockPos belowPos = checkPos.below();
                BlockState belowState = world.getBlockState(belowPos);

                if (!(belowState.getBlock() instanceof BaseRailBlock)) {
                    System.out.println("DEBUG: No rail found at distance " + distance + " at " + checkPos);
                    return true;
                }
            }
        }

        return false;
    }

    private static void handleExtendingRails(MinecartFurnace furnaceCart, MinecartAccess accessor) {
        Level world = furnaceCart.level();
        Direction facing = getFacingDirection(furnaceCart);

        if (facing == null) {
            System.out.println("DEBUG: No facing direction in EXTENDING_RAILS");
            borerStates.put(furnaceCart.getUUID(), BorerState.MOVING_TO_WALL);
            return;
        }

        BlockPos currentPos = furnaceCart.blockPosition();
        BlockPos nextRailPos = findNextMissingRail(furnaceCart, facing);

        if (nextRailPos == null) {
            System.out.println("DEBUG: No more missing rails, back to MOVING_TO_WALL");
            borerStates.put(furnaceCart.getUUID(), BorerState.MOVING_TO_WALL);

            pushMinecartForward(furnaceCart, facing);
            return;
        }

        ItemStack railStack = findAndConsumeRail(furnaceCart);
        if (railStack.isEmpty()) {
            System.out.println("DEBUG: No rails in inventory, cannot extend");
            borerStates.put(furnaceCart.getUUID(), BorerState.MOVING_TO_WALL);
            furnaceCart.setDeltaMovement(Vec3.ZERO);
            return;
        }

        BlockPos groundPos = nextRailPos.below();
        BlockState groundState = world.getBlockState(groundPos);

        if (!groundState.isSolid() || groundState.isAir()) {
            ItemStack groundBlock = findAndConsumeBlock(furnaceCart);
            if (!groundBlock.isEmpty()) {
                Block blockToPlace = Block.byItem(groundBlock.getItem());
                if (blockToPlace != Blocks.AIR) {
                    world.setBlock(groundPos, blockToPlace.defaultBlockState(), 3);
                    System.out.println("DEBUG: Placed ground block at " + groundPos);
                }
            } else {
                System.out.println("DEBUG: No ground blocks available");
                borerStates.put(furnaceCart.getUUID(), BorerState.MOVING_TO_WALL);
                furnaceCart.setDeltaMovement(Vec3.ZERO);
                return;
            }
        }

        RailShape railShape = getRailShapeForDirection(world, nextRailPos, facing);
        Block railBlock = getRailBlockFromItem(railStack);

        BlockState railState = railBlock.defaultBlockState();
        if (railBlock instanceof RailBlock) {
            railState = railState.setValue(RailBlock.SHAPE, railShape);
        } else if (railBlock instanceof PoweredRailBlock) {
            railState = railState.setValue(PoweredRailBlock.SHAPE, railShape);
        }

        world.setBlock(nextRailPos, railState, 3);
        world.levelEvent(2001, nextRailPos, Block.getId(railState));

        System.out.println("DEBUG: Extended rail at " + nextRailPos);
    }

    private static BlockPos findNextMissingRail(MinecartFurnace furnaceCart, Direction facing) {
        Level world = furnaceCart.level();
        BlockPos currentPos = furnaceCart.blockPosition();

        for (int distance = 1; distance <= RAIL_CHECK_DISTANCE; distance++) {
            BlockPos checkPos = currentPos.relative(facing, distance);
            BlockState state = world.getBlockState(checkPos);

            if (!(state.getBlock() instanceof BaseRailBlock)) {
                BlockPos belowPos = checkPos.below();
                BlockState belowState = world.getBlockState(belowPos);

                if (!(belowState.getBlock() instanceof BaseRailBlock)) {
                    return checkPos;
                }
            }
        }

        return null;
    }

    private static void handleMovingToWall(MinecartFurnace furnaceCart, MinecartAccess accessor) {
        Vec3 movement = furnaceCart.getDeltaMovement();

        double speed = movement.horizontalDistance();

        if (speed < 0.05) {
            Direction facing = getFacingDirection(furnaceCart);

            if (facing != null && isWallAhead(furnaceCart, facing)) {
                borerStates.put(furnaceCart.getUUID(), BorerState.BREAKING_LAYER);
                currentBlockIndex.put(furnaceCart.getUUID(), 0);
                lastActionTime.put(furnaceCart.getUUID(), furnaceCart.level().getGameTime());

                furnaceCart.setDeltaMovement(Vec3.ZERO);

                System.out.println("DEBUG: Switched to BREAKING_LAYER. Facing: " + facing);
            }
        }
    }

    private static void handleBreakingLayer(MinecartFurnace furnaceCart, MinecartAccess accessor) {
        Level world = furnaceCart.level();
        long currentTime = world.getGameTime();
        long lastAction = lastActionTime.getOrDefault(furnaceCart.getUUID(), 0L);

        if (currentTime - lastAction < TICKS_PER_BLOCK) {
            return;
        }

        Direction facing = getFacingDirection(furnaceCart);
        if (facing == null) {
            System.out.println("DEBUG: No facing direction in BREAKING_LAYER");
            return;
        }

        BlockPos cartPos = furnaceCart.blockPosition();
        BlockPos targetPos = cartPos.relative(facing, 2);
        List<BlockPos> blocksToBreak = getTunnelBlockPositions(targetPos, facing);

        int blockIndex = currentBlockIndex.getOrDefault(furnaceCart.getUUID(), 0);

        if (blockIndex < blocksToBreak.size()) {
            BlockPos posToBreak = blocksToBreak.get(blockIndex);

            if (checkAndSealFluid(world, posToBreak, furnaceCart)) {
                lastActionTime.put(furnaceCart.getUUID(), currentTime);
                return;
            }

            breakBlock(world, posToBreak, furnaceCart, accessor);

            System.out.println("DEBUG: Broke block " + (blockIndex + 1) + "/" + blocksToBreak.size() + " at " + posToBreak);

            currentBlockIndex.put(furnaceCart.getUUID(), blockIndex + 1);
            lastActionTime.put(furnaceCart.getUUID(), currentTime);
        } else {
            System.out.println("DEBUG: All blocks broken, switching to PLACING_RAIL");
            borerStates.put(furnaceCart.getUUID(), BorerState.PLACING_RAIL);
            currentBlockIndex.remove(furnaceCart.getUUID());
        }
    }

    private static void handlePlacingRail(MinecartFurnace furnaceCart, MinecartAccess accessor) {
        Level world = furnaceCart.level();

        Direction facing = getFacingDirection(furnaceCart);
        if (facing == null) {
            System.out.println("DEBUG: No facing direction in PLACING_RAIL");
            return;
        }

        BlockPos cartPos = furnaceCart.blockPosition();
        BlockPos railPos = cartPos.relative(facing, 1);

        BlockState currentState = world.getBlockState(railPos);
        if (currentState.getBlock() instanceof BaseRailBlock) {
            System.out.println("DEBUG: Rail already exists, moving forward");
            borerStates.put(furnaceCart.getUUID(), BorerState.MOVING_FORWARD);
            pushMinecartForward(furnaceCart, facing);
            return;
        }

        ItemStack railStack = findAndConsumeRail(furnaceCart);
        if (railStack.isEmpty()) {
            System.out.println("DEBUG: No rails available, stopping");
            borerStates.put(furnaceCart.getUUID(), BorerState.MOVING_TO_WALL);
            furnaceCart.setDeltaMovement(Vec3.ZERO);
            return;
        }

        BlockPos groundPos = railPos.below();
        BlockState groundState = world.getBlockState(groundPos);

        if (!groundState.isSolid() || groundState.isAir()) {
            ItemStack groundBlock = findAndConsumeBlock(furnaceCart);
            if (!groundBlock.isEmpty()) {
                Block blockToPlace = Block.byItem(groundBlock.getItem());
                if (blockToPlace != Blocks.AIR) {
                    world.setBlock(groundPos, blockToPlace.defaultBlockState(), 3);
                    System.out.println("DEBUG: Placed ground block at " + groundPos);
                }
            } else {
                System.out.println("DEBUG: No ground blocks available, stopping");
                borerStates.put(furnaceCart.getUUID(), BorerState.MOVING_TO_WALL);
                return;
            }
        }

        RailShape railShape = getRailShapeForDirection(world, railPos, facing);
        Block railBlock = getRailBlockFromItem(railStack);

        BlockState railState = railBlock.defaultBlockState();
        if (railBlock instanceof RailBlock) {
            railState = railState.setValue(RailBlock.SHAPE, railShape);
        } else if (railBlock instanceof PoweredRailBlock) {
            railState = railState.setValue(PoweredRailBlock.SHAPE, railShape);
        }

        world.setBlock(railPos, railState, 3);
        world.levelEvent(2001, railPos, Block.getId(railState));

        System.out.println("DEBUG: Placed rail at " + railPos + " with shape " + railShape);

        borerStates.put(furnaceCart.getUUID(), BorerState.MOVING_FORWARD);
        pushMinecartForward(furnaceCart, facing);
    }

    private static void handleMovingForward(MinecartFurnace furnaceCart, MinecartAccess accessor) {
        Vec3 movement = furnaceCart.getDeltaMovement();
        double speed = movement.horizontalDistance();

        if (speed < 0.02) {
            System.out.println("DEBUG: Minecart stopped moving, back to MOVING_TO_WALL");
            borerStates.put(furnaceCart.getUUID(), BorerState.MOVING_TO_WALL);
            lastActionTime.put(furnaceCart.getUUID(), furnaceCart.level().getGameTime());
        }
    }

    private static void pushMinecartForward(MinecartFurnace furnaceCart, Direction facing) {
        double pushStrength = 0.3;
        Vec3 push = new Vec3(
                facing.getStepX() * pushStrength,
                0,
                facing.getStepZ() * pushStrength
        );
        furnaceCart.setDeltaMovement(push);
        System.out.println("DEBUG: Pushed minecart in direction " + facing + " with strength " + pushStrength);
    }

    private static Direction getFacingDirection(MinecartFurnace furnaceCart) {
        Level world = furnaceCart.level();
        BlockPos blockPos = furnaceCart.blockPosition();

        BlockPos railPos = blockPos;
        if (world.getBlockState(blockPos.below()).is(net.minecraft.tags.BlockTags.RAILS)) {
            railPos = blockPos.below();
        }

        BlockState blockState = world.getBlockState(railPos);
        if (blockState.getBlock() instanceof BaseRailBlock railBlock) {
            RailShape shape = blockState.getValue(railBlock.getShapeProperty());

            Direction railDir = getRailDirection(shape);
            if (railDir != null) {
                Vec3 movement = furnaceCart.getDeltaMovement();
                if (movement.horizontalDistanceSqr() > 0.001) {
                    double dot = movement.x * railDir.getStepX() + movement.z * railDir.getStepZ();
                    Direction result = dot >= 0 ? railDir : railDir.getOpposite();
                    lastKnownDirection.put(furnaceCart.getUUID(), result);
                    return result;
                }
            }
        }

        Direction stored = lastKnownDirection.get(furnaceCart.getUUID());
        if (stored != null) return stored;

        return furnaceCart.getMotionDirection();
    }

    private static Direction getRailDirection(RailShape shape) {
        return switch (shape) {
            case NORTH_SOUTH, ASCENDING_NORTH, ASCENDING_SOUTH -> Direction.SOUTH;
            case EAST_WEST, ASCENDING_EAST, ASCENDING_WEST -> Direction.EAST;
            case SOUTH_EAST -> Direction.SOUTH;
            case SOUTH_WEST -> Direction.SOUTH;
            case NORTH_EAST -> Direction.NORTH;
            case NORTH_WEST -> Direction.NORTH;
        };
    }

//    private static Direction getFacingDirection(MinecartFurnace furnaceCart) {
//        Direction stored = lastKnownDirection.get(furnaceCart.getUUID());
//        if (stored != null) {
//            return stored;
//        }
//
//        Vec3 movement = furnaceCart.getDeltaMovement();
//        if (movement.horizontalDistanceSqr() > 0.001) {
//            Direction fromMovement = getDirectionFromMovement(movement);
//            if (fromMovement != null) {
//                lastKnownDirection.put(furnaceCart.getUUID(), fromMovement);
//                return fromMovement;
//            }
//        }
//
//        Direction motionDir = furnaceCart.getMotionDirection();
//        lastKnownDirection.put(furnaceCart.getUUID(), motionDir);
//        return motionDir;
//    }

    private static boolean isWallAhead(MinecartFurnace furnaceCart, Direction facing) {
        Level world = furnaceCart.level();
        BlockPos cartPos = furnaceCart.blockPosition();
        BlockPos checkPos = cartPos.relative(facing, 2);

        List<BlockPos> positions = getTunnelBlockPositions(checkPos, facing);

        for (BlockPos pos : positions) {
            BlockState state = world.getBlockState(pos);
            if (state.isSolid() && !state.isAir() && !(state.getBlock() instanceof BaseRailBlock)) {
                return true;
            }
        }

        return false;
    }

    private static boolean checkAndSealFluid(Level world, BlockPos pos, MinecartFurnace furnaceCart) {
        for (Direction dir : Direction.values()) {
            BlockPos neighborPos = pos.relative(dir);
            FluidState fluidState = world.getFluidState(neighborPos);

            if (!fluidState.isEmpty()) {
                if (fluidState.is(FluidTags.WATER) || fluidState.is(FluidTags.LAVA)) {
                    ItemStack sealBlock = findAndConsumeBlock(furnaceCart);
                    if (!sealBlock.isEmpty()) {
                        Block blockToPlace = Block.byItem(sealBlock.getItem());
                        if (blockToPlace != Blocks.AIR) {
                            world.setBlock(neighborPos, blockToPlace.defaultBlockState(), 3);
                            System.out.println("DEBUG: Sealed fluid at " + neighborPos);
                            return true;
                        }
                    }
                }
            }
        }

        return false;
    }

    private static void refuelFurnace(MinecartFurnace furnaceCart) {
        int currentFuel = ((MinecartFurnaceAccessor) furnaceCart).getFuel();

        if (currentFuel < 200) {
            ItemStack coal = findAndConsumeFuel(furnaceCart);
            if (!coal.isEmpty()) {
                int fuelValue = currentFuel + getFuelValue(coal.getItem());
                ((MinecartFurnaceAccessor) furnaceCart).setFuel(fuelValue);
                System.out.println("DEBUG: Refueled furnace. New fuel: " + fuelValue);
            }
        }
    }

    private static ItemStack findAndConsumeFuel(MinecartFurnace parent) {
        List<AbstractMinecart> children = MinecartLinkData.getChildren(parent);

        for (AbstractMinecart child : children) {
            if (child instanceof MinecartChest chestCart) {
                for (int i = 0; i < chestCart.getContainerSize(); i++) {
                    ItemStack stack = chestCart.getItem(i);

                    if (stack.getItem() == Items.COAL ||
                            stack.getItem() == Items.CHARCOAL ||
                            stack.getItem() == Items.COAL_BLOCK ||
                            stack.getItem() == Items.BLAZE_ROD) {

                        ItemStack result = stack.copy();
                        result.setCount(1);
                        stack.shrink(1);

                        if (stack.isEmpty()) {
                            chestCart.setItem(i, ItemStack.EMPTY);
                        }

                        return result;
                    }
                }
            }
        }

        return ItemStack.EMPTY;
    }

    private static int getFuelValue(net.minecraft.world.item.Item item) {
        if (item == Items.COAL || item == Items.CHARCOAL) {
            return 3600;
        } else if (item == Items.COAL_BLOCK) {
            return 32000;
        } else if (item == Items.BLAZE_ROD) {
            return 2400;
        }
        return 0;
    }

    private static Direction getDirectionFromMovement(Vec3 movement) {
        double absX = Math.abs(movement.x);
        double absZ = Math.abs(movement.z);

        if (absX < 0.01 && absZ < 0.01) {
            return null;
        }

        if (absX > absZ) {
            return movement.x > 0 ? Direction.EAST : Direction.WEST;
        } else {
            return movement.z > 0 ? Direction.SOUTH : Direction.NORTH;
        }
    }

    private static List<BlockPos> getTunnelBlockPositions(BlockPos center, Direction facing) {
        List<BlockPos> positions = new ArrayList<>();

        Direction rightDir = facing.getClockWise();
        Direction upDir = Direction.UP;

        for (int vertical = 0; vertical < TUNNEL_HEIGHT; vertical++) {
            for (int horizontal = -1; horizontal <= 1; horizontal++) {
                BlockPos pos = center
                        .relative(upDir, vertical)
                        .relative(rightDir, horizontal);
                positions.add(pos);
            }
        }

        return positions;
    }

    private static void breakBlock(Level world, BlockPos pos, MinecartFurnace furnaceCart, MinecartAccess accessor) {
        BlockState state = world.getBlockState(pos);

        if (state.isAir() ||
                state.getBlock() == Blocks.BEDROCK ||
                state.getBlock() instanceof BaseRailBlock) {
            return;
        }

        if (state.getDestroySpeed(world, pos) < 0) {
            return;
        }

        List<ItemStack> drops = Block.getDrops(state, (ServerLevel) world, pos, world.getBlockEntity(pos));

        world.destroyBlock(pos, false);

        for (ItemStack drop : drops) {
            if (!tryStoreInChildren(furnaceCart, accessor, drop)) {
                Block.popResource(world, pos, drop);
            }
        }

        world.levelEvent(2001, pos, Block.getId(state));
    }

    private static RailShape getRailShapeForDirection(Level world, BlockPos pos, Direction facing) {
        BlockPos ahead = pos.relative(facing);
        BlockPos aheadUp = ahead.above();
        BlockPos aheadDown = ahead.below();

        if (world.getBlockState(aheadUp).isSolid() && !world.getBlockState(ahead).isSolid()) {
            return switch (facing) {
                case NORTH -> RailShape.ASCENDING_NORTH;
                case SOUTH -> RailShape.ASCENDING_SOUTH;
                case EAST -> RailShape.ASCENDING_EAST;
                case WEST -> RailShape.ASCENDING_WEST;
                default -> RailShape.NORTH_SOUTH;
            };
        }

        if (!world.getBlockState(aheadDown).isSolid()) {
            return switch (facing) {
                case NORTH -> RailShape.ASCENDING_SOUTH;
                case SOUTH -> RailShape.ASCENDING_NORTH;
                case EAST -> RailShape.ASCENDING_WEST;
                case WEST -> RailShape.ASCENDING_EAST;
                default -> RailShape.NORTH_SOUTH;
            };
        }

        return switch (facing) {
            case NORTH, SOUTH -> RailShape.NORTH_SOUTH;
            case EAST, WEST -> RailShape.EAST_WEST;
            default -> RailShape.NORTH_SOUTH;
        };
    }

    private static Block getRailBlockFromItem(ItemStack stack) {
        return switch (stack.getItem().toString()) {
            case "powered_rail" -> Blocks.POWERED_RAIL;
            case "detector_rail" -> Blocks.DETECTOR_RAIL;
            case "activator_rail" -> Blocks.ACTIVATOR_RAIL;
            default -> Blocks.RAIL;
        };
    }

    private static ItemStack findAndConsumeRail(MinecartFurnace parent) {
        List<AbstractMinecart> children = MinecartLinkData.getChildren(parent);

        for (AbstractMinecart child : children) {
            if (child instanceof MinecartChest chestCart) {
                for (int i = 0; i < chestCart.getContainerSize(); i++) {
                    ItemStack stack = chestCart.getItem(i);

                    if (stack.getItem() == Items.RAIL ||
                            stack.getItem() == Items.POWERED_RAIL ||
                            stack.getItem() == Items.DETECTOR_RAIL ||
                            stack.getItem() == Items.ACTIVATOR_RAIL) {

                        ItemStack result = stack.copy();
                        result.setCount(1);
                        stack.shrink(1);

                        if (stack.isEmpty()) {
                            chestCart.setItem(i, ItemStack.EMPTY);
                        }

                        return result;
                    }
                }
            }
        }

        return ItemStack.EMPTY;
    }

    private static ItemStack findAndConsumeBlock(MinecartFurnace parent) {
        List<AbstractMinecart> children = MinecartLinkData.getChildren(parent);

        for (AbstractMinecart child : children) {
            if (child instanceof MinecartChest chestCart) {
                for (int i = 0; i < chestCart.getContainerSize(); i++) {
                    ItemStack stack = chestCart.getItem(i);

                    if (!stack.isEmpty() && isValidGroundBlock(stack)) {
                        ItemStack result = stack.copy();
                        result.setCount(1);
                        stack.shrink(1);

                        if (stack.isEmpty()) {
                            chestCart.setItem(i, ItemStack.EMPTY);
                        }

                        return result;
                    }
                }
            }
        }

        return ItemStack.EMPTY;
    }

    private static boolean isValidGroundBlock(ItemStack stack) {
        Block block = Block.byItem(stack.getItem());

        return block == Blocks.STONE ||
                block == Blocks.COBBLESTONE ||
                block == Blocks.DEEPSLATE ||
                block == Blocks.COBBLED_DEEPSLATE ||
                block == Blocks.DIRT ||
                block == Blocks.NETHERRACK ||
                block == Blocks.BLACKSTONE ||
                block == Blocks.ANDESITE ||
                block == Blocks.DIORITE ||
                block == Blocks.GRANITE;
    }

    private static boolean tryStoreInChildren(MinecartFurnace parent, MinecartAccess accessor, ItemStack stack) {
        List<AbstractMinecart> children = MinecartLinkData.getChildren(parent);

        for (AbstractMinecart child : children) {
            if (child instanceof MinecartChest chestCart) {
                if (tryInsertIntoChest(chestCart, stack)) {
                    return true;
                }
            }
        }

        return false;
    }

    private static boolean tryInsertIntoChest(MinecartChest chest, ItemStack stack) {
        for (int i = 0; i < chest.getContainerSize(); i++) {
            ItemStack slotStack = chest.getItem(i);

            if (slotStack.isEmpty()) {
                chest.setItem(i, stack.copy());
                return true;
            } else if (ItemStack.isSameItemSameComponents(slotStack, stack)) {
                int space = slotStack.getMaxStackSize() - slotStack.getCount();
                if (space >= stack.getCount()) {
                    slotStack.grow(stack.getCount());
                    return true;
                } else if (space > 0) {
                    slotStack.grow(space);
                    stack.shrink(space);
                }
            }
        }

        return false;
    }
}