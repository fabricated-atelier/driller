package net.driller.event;

import net.driller.data.TrainConnectionsSavedData;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;

public class DrillerServerEvents implements ServerWorldEvents.Load {
    @Override
    public void onWorldLoad(MinecraftServer server, ServerLevel serverLevel) {
        TrainConnectionsSavedData.get(server).processPendingConnections(serverLevel);
    }
}
