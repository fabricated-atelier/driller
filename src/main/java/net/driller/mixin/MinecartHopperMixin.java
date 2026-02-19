package net.driller.mixin;

import net.driller.access.HopperAccess;
import net.driller.mixin.access.AbstractMinecartContainerAccessor;
import net.minecraft.client.gui.screens.inventory.HopperScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.*;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.AbstractMinecartContainer;
import net.minecraft.world.entity.vehicle.MinecartHopper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.PoweredRailBlock;
import net.minecraft.world.level.block.RailBlock;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.RailShape;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MinecartHopper.class)
public abstract class MinecartHopperMixin extends AbstractMinecartContainer implements HopperAccess {

    @Unique
    private final SimpleContainer hoppersInventory = new SimpleContainer(2);

    @Unique
    private static final EntityDataAccessor<Boolean> LEFT_HOPPER = SynchedEntityData.defineId(MinecartHopper.class, EntityDataSerializers.BOOLEAN);
    @Unique
    private static final EntityDataAccessor<Boolean> RIGHT_HOPPER = SynchedEntityData.defineId(MinecartHopper.class, EntityDataSerializers.BOOLEAN);


    public MinecartHopperMixin(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(LEFT_HOPPER, false);
        builder.define(RIGHT_HOPPER, false);
    }

    @Inject(method = "getDefaultDisplayOffset", at = @At("RETURN"), cancellable = true)
    private void getDefaultDisplayOffsetMixin(CallbackInfoReturnable<Integer> info) {
        int offset = info.getReturnValue();
        if (!this.hoppersInventory.getItem(0).isEmpty()) {
            offset += 1;
        }
        if (!this.hoppersInventory.getItem(1).isEmpty()) {
            offset += 1;
        }
        info.setReturnValue(offset);
    }

    @Inject(method = "getContainerSize", at = @At("RETURN"), cancellable = true)
    private void getContainerSizeMixin(CallbackInfoReturnable<Integer> info) {
        int size = info.getReturnValue();
        if (!this.hoppersInventory.getItem(0).isEmpty()) {
            size += 5;
        }
        if (!this.hoppersInventory.getItem(1).isEmpty()) {
            size += 5;
        }
        info.setReturnValue(size);
    }

    @Inject(method = "suckInItems", at = @At("RETURN"), cancellable = true)
    private void suckInItemsMixin(CallbackInfoReturnable<Boolean> info) {
        if (info.getReturnValue()) {
            return;
        }
        boolean sucked = false;

        if (this.hasLeftHopper()) {
            sucked = suckFromSide(getLeftBoundingBox());
        }

        if (!sucked && this.hasRightHopper()) {
            sucked = suckFromSide(getRightBoundingBox());
        }

        if (sucked) {
            info.setReturnValue(true);
        }
    }

    @Unique
    private boolean suckFromSide(AABB area) {
        for (ItemEntity itemEntity : this.level()
                .getEntitiesOfClass(ItemEntity.class, area, EntitySelector.ENTITY_STILL_ALIVE)) {
            if (HopperBlockEntity.addItem(this, itemEntity)) {
                return true;
            }
        }
        return false;
    }

    @Unique
    private AABB getLeftBoundingBox() {
        return getSideBoundingBox(true);
    }

    @Unique
    private AABB getRightBoundingBox() {
        return getSideBoundingBox(false);
    }

    @Unique
    private AABB getSideBoundingBox(boolean left) {
        BlockPos railPos = this.blockPosition();
        BlockState railState = this.level().getBlockState(railPos);
        Direction.Axis railAxis = getRailAxis(railState);

        AABB base = this.getBoundingBox();
        double reach = 1.25;

        if (railAxis == Direction.Axis.X) {
            if (left) {
                return new AABB(base.minX, base.minY, base.minZ - reach, base.maxX, base.maxY, base.minZ);
            } else {
                return new AABB(base.minX, base.minY, base.maxZ, base.maxX, base.maxY, base.maxZ + reach);
            }
        } else {
            if (left) {
                return new AABB(base.minX - reach, base.minY, base.minZ, base.minX, base.maxY, base.maxZ);
            } else {
                return new AABB(base.maxX, base.minY, base.minZ, base.maxX + reach, base.maxY, base.maxZ);
            }
        }
    }

    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    private void addAdditionalSaveDataMixin(CompoundTag compoundTag, CallbackInfo info) {
        compoundTag.put("HoppersInventory", this.hoppersInventory.createTag(this.registryAccess()));
    }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    private void readAdditionalSaveDataMixin(CompoundTag compoundTag, CallbackInfo info) {
        if (compoundTag.contains("HoppersInventory", 9)) {
            this.hoppersInventory.fromTag(compoundTag.getList("HoppersInventory", 10), this.registryAccess());
        }
        this.entityData.set(LEFT_HOPPER, !this.hoppersInventory.getItem(0).isEmpty());
        this.entityData.set(RIGHT_HOPPER, !this.hoppersInventory.getItem(1).isEmpty());

        System.out.println("TEST TEST "+this.getContainerSize());

        if (this instanceof AbstractMinecartContainerAccessor abstractMinecartContainerAccessor) {
            NonNullList<ItemStack> nonNullList = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
            for (int i = 0; i < this.getContainerSize(); i++) {
                nonNullList.set(i, abstractMinecartContainerAccessor.driller$getItemStacks().get(i));
            }
            abstractMinecartContainerAccessor.driller$setItemStacks(nonNullList);
        }
    }

    @Override
    public void chestVehicleDestroyed(DamageSource damageSource, Level level, Entity entity) {
        super.chestVehicleDestroyed(damageSource, level, entity);

        if (!level.isClientSide()) {
            Containers.dropContents(level, entity, this.hoppersInventory);
        }
    }

    @Override
    public InteractionResult interact(Player player, InteractionHand interactionHand) {
        if (!player.getItemInHand(interactionHand).is(Items.HOPPER)) {
            return super.interact(player, interactionHand);
        }

        if (!this.hoppersInventory.getItem(0).isEmpty() && !this.hoppersInventory.getItem(1).isEmpty()) {
            return super.interact(player, interactionHand);
        }

        if (!this.level().isClientSide()) {
            if (isPlayerOnRightSide(player)) {
                if (this.hoppersInventory.getItem(1).isEmpty()) {
                    this.hoppersInventory.setItem(1, player.getItemInHand(interactionHand).copy());
                    this.entityData.set(RIGHT_HOPPER, true);

                    if (this instanceof AbstractMinecartContainerAccessor abstractMinecartContainerAccessor) {
                        NonNullList<ItemStack> nonNullList = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);

                        for (int i = 0; i < this.getContainerSize(); i++) {
                            nonNullList.set(i, abstractMinecartContainerAccessor.driller$getItemStacks().get(i));
                        }
                        abstractMinecartContainerAccessor.driller$setItemStacks(nonNullList);
                    }

                    if (!player.isCreative()) {
                        player.getItemInHand(interactionHand).shrink(1);
                    }
                    return InteractionResult.SUCCESS;
                } else if (this.hoppersInventory.getItem(0).isEmpty()) {
                    this.hoppersInventory.setItem(0, player.getItemInHand(interactionHand).copy());
                    this.entityData.set(LEFT_HOPPER, true);

                    if (this instanceof AbstractMinecartContainerAccessor abstractMinecartContainerAccessor) {
                        NonNullList<ItemStack> nonNullList = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
                        for (int i = 0; i < this.getContainerSize(); i++) {
                            nonNullList.set(i, abstractMinecartContainerAccessor.driller$getItemStacks().get(i));
                        }
                        abstractMinecartContainerAccessor.driller$setItemStacks(nonNullList);
                    }

                    if (!player.isCreative()) {
                        player.getItemInHand(interactionHand).shrink(1);
                    }
                    return InteractionResult.SUCCESS;
                }
            } else if (this.hoppersInventory.getItem(0).isEmpty()) {
                this.hoppersInventory.setItem(0, player.getItemInHand(interactionHand).copy());
                this.entityData.set(LEFT_HOPPER, true);

                if (this instanceof AbstractMinecartContainerAccessor abstractMinecartContainerAccessor) {
                    NonNullList<ItemStack> nonNullList = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
                    for (int i = 0; i < this.getContainerSize(); i++) {
                        nonNullList.set(i, abstractMinecartContainerAccessor.driller$getItemStacks().get(i));
                    }
                    abstractMinecartContainerAccessor.driller$setItemStacks(nonNullList);
                }

                if (!player.isCreative()) {
                    player.getItemInHand(interactionHand).shrink(1);
                }
                return InteractionResult.SUCCESS;
            } else if (this.hoppersInventory.getItem(1).isEmpty()) {
                this.hoppersInventory.setItem(1, player.getItemInHand(interactionHand).copy());
                this.entityData.set(RIGHT_HOPPER, true);

                if (this instanceof AbstractMinecartContainerAccessor abstractMinecartContainerAccessor) {
                    NonNullList<ItemStack> nonNullList = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
                    for (int i = 0; i < this.getContainerSize(); i++) {
                        nonNullList.set(i, abstractMinecartContainerAccessor.driller$getItemStacks().get(i));
                    }
                    abstractMinecartContainerAccessor.driller$setItemStacks(nonNullList);
                }

                if (!player.isCreative()) {
                    player.getItemInHand(interactionHand).shrink(1);
                }
                return InteractionResult.SUCCESS;
            }
        }

        return InteractionResult.SUCCESS;
    }

    @Unique
    private boolean isPlayerOnRightSide(Player player) {
        BlockPos railPos = this.blockPosition();
        BlockState railState = this.level().getBlockState(railPos);

        Direction.Axis railAxis = getRailAxis(railState);

        double forwardX = 0;
        double forwardZ = 0;

        if (railAxis == Direction.Axis.X) {
            forwardX = 1;
            forwardZ = 0;
        } else {
            forwardX = 0;
            forwardZ = 1;
        }

        double dx = player.getX() - this.getX();
        double dz = player.getZ() - this.getZ();

        double cross = forwardX * dz - forwardZ * dx;
        return cross > 0;
    }

    @Unique
    private Direction.Axis getRailAxis(BlockState state) {
        if (state.hasProperty(RailBlock.SHAPE)) {
            RailShape shape = state.getValue(RailBlock.SHAPE);
            return isNorthSouth(shape) ? Direction.Axis.Z : Direction.Axis.X;
        }
        if (state.hasProperty(PoweredRailBlock.SHAPE)) {
            RailShape shape = state.getValue(PoweredRailBlock.SHAPE);
            return isNorthSouth(shape) ? Direction.Axis.Z : Direction.Axis.X;
        }
        return Direction.Axis.X;
    }

    @Unique
    private boolean isNorthSouth(RailShape shape) {
        return shape == RailShape.NORTH_SOUTH
                || shape == RailShape.ASCENDING_NORTH
                || shape == RailShape.ASCENDING_SOUTH;
    }

    @Override
    public boolean hasLeftHopper() {
        return this.entityData.get(LEFT_HOPPER);
    }

    @Override
    public boolean hasRightHopper() {
        return this.entityData.get(RIGHT_HOPPER);
    }
}
