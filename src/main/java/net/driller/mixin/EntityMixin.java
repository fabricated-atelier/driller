package net.driller.mixin;

import net.driller.entity.TrainConnection;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public abstract class EntityMixin {
    @Shadow
    public abstract @Nullable MinecraftServer getServer();

    @Inject(method = "remove", at = @At("HEAD"))
    private void onRemove(Entity.RemovalReason removalReason, CallbackInfo ci) {
        if (getServer() == null) return;
        if (!(((Entity) (Object) this) instanceof AbstractMinecart cart)) return;
        TrainConnection.getConnection(cart).ifPresent(connection -> connection.removeCart(cart, true));
    }

    @Inject(method = "startSeenByPlayer", at = @At("HEAD"))
    private void cartStartSeenByPlayer(ServerPlayer serverPlayer, CallbackInfo ci) {
        if (!((Entity) (Object) this instanceof AbstractMinecart cart)) return;
        TrainConnection.getConnection(cart).ifPresent(connection -> connection.addTrackingCart(serverPlayer));
    }

    @Inject(method = "stopSeenByPlayer", at = @At("HEAD"))
    private void cartStopSeenByPlayer(ServerPlayer serverPlayer, CallbackInfo ci) {
        if (!((Entity) (Object) this instanceof AbstractMinecart cart)) return;
        TrainConnection.getConnection(cart).ifPresent(connection -> connection.stopTrackingCart(serverPlayer));
    }
}
