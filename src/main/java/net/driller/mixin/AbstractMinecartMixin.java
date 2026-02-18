package net.driller.mixin;

import net.driller.access.MinecartAccess;
import net.driller.util.MinecartLinkData;
import net.driller.util.DrillerLogic;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.entity.vehicle.MinecartFurnace;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Mixin(AbstractMinecart.class)
public class AbstractMinecartMixin implements MinecartAccess {

    @Unique
    private UUID linkedParentUUID = null;

    @Unique
    private boolean isLinkedToParent = false;

    @Unique
    private List<UUID> linkedChildren = new ArrayList<>();

    @Unique
    private boolean tunnelBorerEnabled = false;

    @Override
    public boolean isTunnelBorerEnabled() {
        return tunnelBorerEnabled;
    }

    @Override
    public void setTunnelBorerEnabled(boolean enabled) {
        this.tunnelBorerEnabled = enabled;
    }

    @Override
    public UUID driller$getLinkedParentUUID() {
        return linkedParentUUID;
    }

    @Override
    public void driller$setLinkedParentUUID(UUID uuid) {
        this.linkedParentUUID = uuid;
    }

    @Override
    public boolean driller$isLinkedToParent() {
        return isLinkedToParent;
    }

    @Override
    public void driller$setLinkedToParent(boolean linked) {
        this.isLinkedToParent = linked;
    }

    @Override
    public List<UUID> driller$getLinkedChildren() {
        return linkedChildren;
    }

    @Override
    public void driller$addChild(UUID childUuid) {
        if (!linkedChildren.contains(childUuid)) {
            linkedChildren.add(childUuid);
        }
    }

    @Override
    public void driller$removeChild(UUID childUuid) {
        linkedChildren.remove(childUuid);
    }

    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    private void addAdditionalSaveDataMixin(CompoundTag nbt, CallbackInfo info) {
        if (linkedParentUUID != null) {
            nbt.putUUID("LinkedParentUUID", linkedParentUUID);
        }
        nbt.putBoolean("IsLinked", isLinkedToParent);

        if (!linkedChildren.isEmpty()) {
            ListTag childrenList = new ListTag();
            for (UUID childUuid : linkedChildren) {
                CompoundTag childTag = new CompoundTag();
                childTag.putUUID("ChildUUID", childUuid);
                childrenList.add(childTag);
            }
            nbt.put("LinkedChildren", childrenList);
        }

        nbt.putBoolean("TunnelBorerEnabled", tunnelBorerEnabled);
    }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    private void readAdditionalSaveDataMixin(CompoundTag nbt, CallbackInfo info) {
        if (nbt.contains("LinkedParentUUID")) {
            this.linkedParentUUID = nbt.getUUID("LinkedParentUUID");
        }
        this.isLinkedToParent = nbt.getBoolean("IsLinked");

        this.linkedChildren.clear();
        if (nbt.contains("LinkedChildren")) {
            ListTag childrenList = nbt.getList("LinkedChildren", 10); // 10 = CompoundTag
            for (int i = 0; i < childrenList.size(); i++) {
                CompoundTag childTag = childrenList.getCompound(i);
                this.linkedChildren.add(childTag.getUUID("ChildUUID"));
            }
        }

        this.tunnelBorerEnabled = nbt.getBoolean("TunnelBorerEnabled");
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void tickMixin(CallbackInfo info) {
        AbstractMinecart thisCart = (AbstractMinecart) (Object) this;

        if (thisCart.level().isClientSide()) {
            return;
        }

        if (thisCart instanceof MinecartFurnace) {
            DrillerLogic.processTunnelBoring(thisCart);
        }

        if (!MinecartLinkData.isLinked(thisCart)) {
            return;
        }

        UUID parentUuid = MinecartLinkData.getParentUuid(thisCart);
        if (parentUuid == null) {
            return;
        }

        Entity parentEntity = ((ServerLevel) thisCart.level()).getEntity(parentUuid);

        if (parentEntity instanceof AbstractMinecart parentCart) {
            followParent(thisCart, parentCart);
        } else {
            MinecartLinkData.unlink(thisCart);
        }
    }

    @Inject(method = "isPushable", at = @At("HEAD"), cancellable = true)
    private void isPushableMixin(CallbackInfoReturnable<Boolean> info) {
        AbstractMinecart thisCart = (AbstractMinecart) (Object) this;

        if (MinecartLinkData.isLinked(thisCart) && MinecartLinkData.getParentUuid(thisCart) != null) {
            info.setReturnValue(false);
        }
    }

    @Inject(method = "canCollideWith", at = @At("HEAD"), cancellable = true)
    private void canCollideWithMixin(Entity other, CallbackInfoReturnable<Boolean> info) {
        AbstractMinecart thisCart = (AbstractMinecart) (Object) this;

        if (MinecartLinkData.isLinked(thisCart) && MinecartLinkData.getParentUuid(thisCart) != null) {
            info.setReturnValue(false);
        }
    }

    @Unique
    private void followParent(AbstractMinecart child, AbstractMinecart parent) {
        double idealDistance = 1.6;
        double springStrength = 0.1;
        double damping = 0.8;

        Vec3 toParent = parent.position().subtract(child.position());
        double distance = toParent.length();

        double force = (distance - idealDistance) * springStrength;
        Vec3 direction = toParent.normalize();
        Vec3 springForce = direction.scale(force);

        Vec3 currentVelocity = child.getDeltaMovement().scale(damping);

        Vec3 newVelocity = currentVelocity.add(springForce);
        child.setDeltaMovement(newVelocity);

        float targetYaw = parent.getYRot();
        float currentYaw = child.getYRot();
        float newYaw = currentYaw + (targetYaw - currentYaw) * 0.2f;
        child.setYRot(newYaw);
    }

    // VERSION THREE
//    private void followParent(AbstractMinecart child, AbstractMinecart parent) {
//        double distance = 2.5;
//
//        float yaw = parent.getYRot();
//        double yawRad = Math.toRadians(yaw);
//
//        double offsetX = -Math.sin(yawRad) * distance;
//        double offsetZ = Math.cos(yawRad) * distance;
//
//        Vec3 targetPos = parent.position().add(offsetX, 0, offsetZ);
//
//        double lerpFactor = 0.3;
//        Vec3 newPos = child.position().lerp(targetPos, lerpFactor);
//
//        Vec3 movement = newPos.subtract(child.position());
//        child.setDeltaMovement(movement);
//
//        child.setYRot(parent.getYRot());
//        child.setXRot(parent.getXRot());
//    }

    // VERSION TWO
//    @Unique
//    private void followParent(AbstractMinecart child, AbstractMinecart parent) {
//        BlockPos childPos = child.blockPosition();
//        BlockState childState = child.level().getBlockState(childPos);
//
//        if (childState.getBlock() instanceof BaseRailBlock) {
//            Vec3 parentVelocity = parent.getDeltaMovement();
//
//            double speedFactor = 0.95;
//            child.setDeltaMovement(parentVelocity.scale(speedFactor));
//
//            child.setYRot(parent.getYRot());
//            child.setXRot(parent.getXRot());
//        } else {
//            followParentDirect(child, parent);
//        }
//    }
//
//    @Unique
//    private void followParentDirect(AbstractMinecart child, AbstractMinecart parent) {
//        double targetDistance = 2.0;
//
//        Vec3 toChild = child.position().subtract(parent.position());
//        double currentDistance = toChild.length();
//
//        if (Math.abs(currentDistance - targetDistance) > 0.5) {
//            Vec3 direction = toChild.normalize();
//            Vec3 targetPos = parent.position().add(direction.scale(targetDistance));
//
//            Vec3 movement = targetPos.subtract(child.position()).scale(0.2);
//            child.setDeltaMovement(movement);
//        } else {
//            child.setDeltaMovement(parent.getDeltaMovement());
//        }
//        child.setYRot(parent.getYRot());
//    }

    // Version ONE
//    @Unique
//    private void followParent(AbstractMinecart child, AbstractMinecart parent) {
//        double distance = 2.0;
//
//        Vec3 direction = parent.position().subtract(child.position()).normalize();
//
//        Vec3 targetPos = parent.position().subtract(direction.scale(distance));
//
//        Vec3 movement = targetPos.subtract(child.position()).scale(0.3); // 0.3 = Geschwindigkeit
//
//        child.setDeltaMovement(movement);
//
//        child.setYRot(parent.getYRot());
//    }
}
