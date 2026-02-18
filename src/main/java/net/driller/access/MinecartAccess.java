package net.driller.access;

import java.util.List;
import java.util.UUID;

public interface MinecartAccess {
    UUID driller$getLinkedParentUUID();

    void driller$setLinkedParentUUID(UUID uuid);

    boolean driller$isLinkedToParent();

    void driller$setLinkedToParent(boolean linked);

    List<UUID> driller$getLinkedChildren();

    void driller$addChild(UUID childUuid);

    void driller$removeChild(UUID childUuid);

    boolean isTunnelBorerEnabled();

    void setTunnelBorerEnabled(boolean enabled);
}
