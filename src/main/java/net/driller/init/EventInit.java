package net.driller.init;

import net.driller.access.MinecartAccess;
import net.driller.util.MinecartLinkData;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.entity.vehicle.MinecartFurnace;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

import java.util.UUID;

public class EventInit {

    public static void init() {
        UseEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (player.getItemInHand(hand).is(ItemInit.WRENCH) && entity instanceof AbstractMinecart minecart) {

                if (world.isClientSide()) {
                    return InteractionResult.SUCCESS;
                }

                if (player.isShiftKeyDown()) {
                    if (MinecartLinkData.isLinked(minecart)) {
                        MinecartLinkData.unlink(minecart);
                        player.displayClientMessage(Component.literal("Verlinkung entfernt!"), true);
                    }
                    return InteractionResult.SUCCESS;
                }

                if (entity instanceof MinecartFurnace furnaceCart) {

                    MinecartAccess accessor = (MinecartAccess) furnaceCart;
                    if (!accessor.driller$getLinkedChildren().isEmpty()) {
                        accessor.setTunnelBorerEnabled(!accessor.isTunnelBorerEnabled());

                        player.displayClientMessage(Component.literal("Tunnel Borer: " + (accessor.isTunnelBorerEnabled() ? "AN" : "AUS")), true);

                        return InteractionResult.SUCCESS;
                    }
                }

                ItemStack stack = player.getItemInHand(hand);
                CustomData customData = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
                CompoundTag nbt = customData.copyTag();

                if (!nbt.contains("SelectedParentUUID")) {
                    nbt.putUUID("SelectedParentUUID", minecart.getUUID());
                    stack.set(DataComponents.CUSTOM_DATA, CustomData.of(nbt));
                    player.displayClientMessage(Component.literal("Parent Minecart ausgewählt!"), true);
                } else {
                    UUID parentUuid = nbt.getUUID("SelectedParentUUID");

                    if (parentUuid.equals(minecart.getUUID())) {
                        player.displayClientMessage(Component.literal("Kann nicht mit sich selbst verlinken!"), true);
                        return InteractionResult.FAIL;
                    }

                    Entity parentEntity = ((ServerLevel) world).getEntity(parentUuid);
                    if (parentEntity instanceof AbstractMinecart parentCart) {
                        MinecartLinkData.linkChildToParent(parentCart, minecart);
                        player.displayClientMessage(Component.literal("Minecarts verlinkt!"), true);
                    } else {
                        player.displayClientMessage(Component.literal("Parent nicht gefunden!"), true);
                        return InteractionResult.FAIL;
                    }

                    nbt.remove("SelectedParentUUID");
                }

                return InteractionResult.SUCCESS;
            }
            return InteractionResult.PASS;
        });
    }
}
