package net.driller.util;

import net.driller.access.MinecartAccess;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.entity.vehicle.MinecartChest;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MinecartLinkData {

    public static void setParent(AbstractMinecart child, UUID parentUuid) {
        ((MinecartAccess) child).driller$setLinkedParentUUID(parentUuid);
        ((MinecartAccess) child).driller$setLinkedToParent(true);
    }

    public static void linkChildToParent(AbstractMinecart parent, AbstractMinecart child) {
        ((MinecartAccess) child).driller$setLinkedParentUUID(parent.getUUID());
        ((MinecartAccess) child).driller$setLinkedToParent(true);

        ((MinecartAccess) parent).driller$addChild(child.getUUID());
    }

    @Nullable
    public static UUID getParentUuid(AbstractMinecart minecart) {
        return ((MinecartAccess) minecart).driller$getLinkedParentUUID();
    }

    public static boolean isLinked(AbstractMinecart minecart) {
        return ((MinecartAccess) minecart).driller$isLinkedToParent();
    }

    public static void unlink(AbstractMinecart minecart) {
        UUID parentUuid = getParentUuid(minecart);
        if (parentUuid != null && minecart.level() instanceof ServerLevel serverLevel) {
            Entity parentEntity = serverLevel.getEntity(parentUuid);
            if (parentEntity instanceof AbstractMinecart parentCart) {
                ((MinecartAccess) parentCart).driller$removeChild(minecart.getUUID());
            }
        }

        ((MinecartAccess) minecart).driller$setLinkedParentUUID(null);
        ((MinecartAccess) minecart).driller$setLinkedToParent(false);
    }

    public static List<AbstractMinecart> getChildren(AbstractMinecart parent) {
        List<AbstractMinecart> children = new ArrayList<>();
        List<UUID> childUuids = ((MinecartAccess) parent).driller$getLinkedChildren();

        if (parent.level() instanceof ServerLevel serverLevel) {
            for (UUID childUuid : childUuids) {
                Entity entity = serverLevel.getEntity(childUuid);
                if (entity instanceof AbstractMinecart childCart) {
                    children.add(childCart);
                }
            }
        }

        return children;
    }

    public static List<MinecartChest> getChestMinecarts(AbstractMinecart parent) {
        return getChildren(parent).stream()
                .filter(cart -> cart instanceof MinecartChest)
                .map(cart -> (MinecartChest) cart)
                .toList();
    }
}