package net.driller.data;

import net.driller.DrillerMain;
import net.driller.entity.TrainConnection;
import net.driller.entity.callback.TrainConnectionListener;
import net.driller.util.NbtKeys;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class TrainConnectionsSavedData extends SavedData implements TrainConnectionListener {
    private final HashSet<TrainConnection> connections = new HashSet<>();
    private final HashSet<List<UUID>> pendingConnections = new HashSet<>();
    private final HashMap<AbstractMinecart, TrainConnection> cartConnectionsCache = new HashMap<>();

    public void processPendingConnections(ServerLevel level) {
        for (List<UUID> pendingConnection : pendingConnections) {
            TrainConnection connection = new TrainConnection(level);
            connection.registerListener(this);
            for (UUID entityUuid : pendingConnection) {
                if (!(level.getEntity(entityUuid) instanceof AbstractMinecart cart)) {
                    String warning = "Minecart UUID in Train connection not recognized: %s".formatted(entityUuid);
                    DrillerMain.LOGGER.warn(warning);
                    continue;
                }
                connection.addCart(cart, true);
            }
            this.connections.add(connection);
        }
        this.pendingConnections.clear();
        this.setDirty();
    }

    public Optional<TrainConnection> getCachedConnection(AbstractMinecart cart) {
        return Optional.ofNullable(cartConnectionsCache.get(cart));
    }

    @Override
    public void onCartsAdded(TrainConnection connection, Set<AbstractMinecart> added) {
        TrainConnectionListener.super.onCartsAdded(connection, added);
        added.forEach(cart -> cartConnectionsCache.put(cart, connection));
    }

    @Override
    public void onCartsRemoved(TrainConnection connection, Set<AbstractMinecart> removed) {
        TrainConnectionListener.super.onCartsRemoved(connection, removed);
        removed.forEach(cartConnectionsCache::remove);
    }

    @Override
    public void beforeTrainDissolved(TrainConnection connection) {
        TrainConnectionListener.super.beforeTrainDissolved(connection);
        connections.remove(connection);
        this.setDirty();
    }

    public Set<TrainConnection> getConnections() {
        return Collections.unmodifiableSet(connections);
    }

    @NotNull
    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider provider) {
        ListTag connectionsTag = new ListTag();
        for (TrainConnection entry : connections) {
            CompoundTag entryTag = new CompoundTag();
            entry.write(entryTag);
            connectionsTag.add(entryTag);
        }
        tag.put(NbtKeys.TRAIN_CONNECTIONS, connectionsTag);
        return tag;
    }

    private static TrainConnectionsSavedData load(CompoundTag tag, HolderLookup.Provider provider) {
        TrainConnectionsSavedData data = new TrainConnectionsSavedData();
        ListTag connectionsTag = tag.getList(NbtKeys.TRAIN_CONNECTIONS, Tag.TAG_COMPOUND);
        for (int i = 0; i < connectionsTag.size(); i++) {
            CompoundTag entryTag = connectionsTag.getCompound(i);
            data.pendingConnections.add(TrainConnection.read(entryTag));
        }
        return data;
    }

    public static TrainConnectionsSavedData get(MinecraftServer server) {
        DimensionDataStorage storage = server.overworld().getDataStorage();
        return storage.computeIfAbsent(new Factory<>(
                TrainConnectionsSavedData::new, TrainConnectionsSavedData::load, DataFixTypes.ENTITY_CHUNK
        ), DrillerMain.MOD_ID);
    }
}
