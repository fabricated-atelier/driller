package net.driller.entity.callback;

import net.driller.entity.TrainConnection;
import net.minecraft.world.entity.vehicle.AbstractMinecart;

import java.util.Set;

/**
 * Register listener with {@link TrainConnection#registerListener(TrainConnectionListener) TrainConnection#registerListener}
 */
public interface TrainConnectionListener {
    default void onCartsAdded(TrainConnection connection, Set<AbstractMinecart> added) {
    }

    default void onCartsRemoved(TrainConnection connection, Set<AbstractMinecart> removed) {
    }

    default void beforeTrainDissolved(TrainConnection connection) {
    }
}
