package net.driller.event;

import net.driller.access.MinecartAccess;
import net.driller.item.WrenchItem;
import net.driller.util.MinecartLinkData;
import net.driller.util.NbtKeys;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.entity.vehicle.MinecartFurnace;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class DrillerEntityEvents implements UseEntityCallback {
    @Override
    public InteractionResult interact(Player player, Level world, InteractionHand hand, Entity entity, @Nullable EntityHitResult hitResult) {
        ItemStack stack = player.getItemInHand(hand);
        if (!(stack.getItem() instanceof WrenchItem)) return InteractionResult.PASS;
        if (!(entity instanceof AbstractMinecart minecart)) return InteractionResult.PASS;
        if (world.isClientSide()) return InteractionResult.SUCCESS;

        if (player.isShiftKeyDown()) {
            if (MinecartLinkData.isLinked(minecart)) {
                MinecartLinkData.unlink(minecart);
                player.displayClientMessage(Component.translatable("entity.driller.connection.removed"), true);
            }
            return InteractionResult.SUCCESS;
        }

        if (entity instanceof MinecartFurnace furnaceCart) {
            MinecartAccess accessor = (MinecartAccess) furnaceCart;
            if (!accessor.driller$getLinkedChildren().isEmpty()) {
                accessor.driller$setDrillEnabled(!accessor.driller$isDrillEnabled());
                String translation = "entity.driller.drill." + (accessor.driller$isDrillEnabled() ? "on" : "off");
                player.displayClientMessage(Component.translatable(translation), true);
                return InteractionResult.SUCCESS;
            }
        }

        CustomData customData = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        CompoundTag nbt = customData.copyTag();

        if (!nbt.contains(NbtKeys.SELECTED_PARENT)) {
            nbt.putUUID(NbtKeys.SELECTED_PARENT, minecart.getUUID());
            stack.set(DataComponents.CUSTOM_DATA, CustomData.of(nbt));
            player.displayClientMessage(Component.translatable("entity.driller.connection.parent_selected"), true);
        } else {
            UUID parentUuid = nbt.getUUID(NbtKeys.SELECTED_PARENT);

            if (parentUuid.equals(minecart.getUUID())) {
                player.displayClientMessage(Component.translatable("entity.driller.connection.error_link_self"), true);
                return InteractionResult.FAIL;
            }

            Entity parentEntity = ((ServerLevel) world).getEntity(parentUuid);
            if (parentEntity instanceof AbstractMinecart parentCart) {
                MinecartLinkData.linkChildToParent(parentCart, minecart);
                player.displayClientMessage(Component.translatable("entity.driller.connection.added"), true);
            } else {
                player.displayClientMessage(Component.translatable("entity.driller.connection.error_no_parent"), true);
                return InteractionResult.FAIL;
            }

            nbt.remove(NbtKeys.SELECTED_PARENT);
        }

        return InteractionResult.SUCCESS;
    }
}
