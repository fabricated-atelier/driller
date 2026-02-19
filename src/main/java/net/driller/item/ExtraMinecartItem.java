package net.driller.item;

import net.driller.entity.MinecartDrill;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.*;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.RailShape;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class ExtraMinecartItem extends Item {
    private static final DispenseItemBehavior DISPENSE_ITEM_BEHAVIOR = new DefaultDispenseItemBehavior() {
        private final DefaultDispenseItemBehavior defaultDispenseItemBehavior = new DefaultDispenseItemBehavior();

        @Override
        public ItemStack execute(BlockSource blockSource, ItemStack itemStack) {
            Direction direction = blockSource.state().getValue(DispenserBlock.FACING);
            ServerLevel serverLevel = blockSource.level();
            Vec3 vec3 = blockSource.center();
            double d = vec3.x() + direction.getStepX() * 1.125;
            double e = Math.floor(vec3.y()) + direction.getStepY();
            double f = vec3.z() + direction.getStepZ() * 1.125;
            BlockPos blockPos = blockSource.pos().relative(direction);
            BlockState blockState = serverLevel.getBlockState(blockPos);
            RailShape railShape = blockState.getBlock() instanceof BaseRailBlock
                    ? blockState.getValue(((BaseRailBlock) blockState.getBlock()).getShapeProperty())
                    : RailShape.NORTH_SOUTH;
            double g;
            if (blockState.is(BlockTags.RAILS)) {
                if (railShape.isAscending()) {
                    g = 0.6;
                } else {
                    g = 0.1;
                }
            } else {
                if (!blockState.isAir() || !serverLevel.getBlockState(blockPos.below()).is(BlockTags.RAILS)) {
                    return this.defaultDispenseItemBehavior.dispense(blockSource, itemStack);
                }

                BlockState blockState2 = serverLevel.getBlockState(blockPos.below());
                RailShape railShape2 = blockState2.getBlock() instanceof BaseRailBlock
                        ? blockState2.getValue(((BaseRailBlock) blockState2.getBlock()).getShapeProperty())
                        : RailShape.NORTH_SOUTH;
                if (direction != Direction.DOWN && railShape2.isAscending()) {
                    g = -0.4;
                } else {
                    g = -0.9;
                }
            }

            AbstractMinecart abstractMinecart = createMinecart(serverLevel, d, e + g, f, ((ExtraMinecartItem) itemStack.getItem()).type, itemStack, null);
            serverLevel.addFreshEntity(abstractMinecart);
            itemStack.shrink(1);
            return itemStack;
        }

        @Override
        protected void playSound(BlockSource blockSource) {
            blockSource.level().levelEvent(1000, blockSource.pos(), 0);
        }
    };

    private final Type type;

    public ExtraMinecartItem(Type type, Item.Properties properties) {
        super(properties);
        this.type = type;
        DispenserBlock.registerBehavior(this, DISPENSE_ITEM_BEHAVIOR);
    }

    @Override
    public InteractionResult useOn(UseOnContext useOnContext) {
        Level level = useOnContext.getLevel();
        BlockPos blockPos = useOnContext.getClickedPos();
        BlockState blockState = level.getBlockState(blockPos);
        if (!blockState.is(BlockTags.RAILS)) {
            return InteractionResult.FAIL;
        } else {
            ItemStack itemStack = useOnContext.getItemInHand();
            if (level instanceof ServerLevel serverLevel) {
                RailShape railShape = blockState.getBlock() instanceof BaseRailBlock
                        ? blockState.getValue(((BaseRailBlock) blockState.getBlock()).getShapeProperty())
                        : RailShape.NORTH_SOUTH;
                double d = 0.0;
                if (railShape.isAscending()) {
                    d = 0.5;
                }

                AbstractMinecart abstractMinecart = createMinecart(
                        serverLevel, blockPos.getX() + 0.5, blockPos.getY() + 0.0625 + d, blockPos.getZ() + 0.5, this.type, itemStack, useOnContext.getPlayer());
                serverLevel.addFreshEntity(abstractMinecart);
                serverLevel.gameEvent(GameEvent.ENTITY_PLACE, blockPos, GameEvent.Context.of(useOnContext.getPlayer(), serverLevel.getBlockState(blockPos.below())));
            }

            itemStack.shrink(1);
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
    }

    public static AbstractMinecart createMinecart(
            ServerLevel serverLevel, double d, double e, double f, Type type, ItemStack itemStack, @Nullable Player player
    ) {
        AbstractMinecart abstractMinecart = (switch (type) {
            case DRILL -> new MinecartDrill(serverLevel, d, e, f);
            case PLACER -> new MinecartDrill(serverLevel, d, e, f); // TODO
            default -> new Minecart(serverLevel, d, e, f);
        });
        EntityType.createDefaultStackConfig(serverLevel, itemStack, player).accept(abstractMinecart);
        return abstractMinecart;
    }

    public static enum Type {
        DRILL,
        PLACER
    }
}

