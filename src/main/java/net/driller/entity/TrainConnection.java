package net.driller.entity;

import net.driller.data.TrainConnectionsSavedData;
import net.driller.entity.callback.TrainConnectionListener;
import net.driller.init.GameRuleInit;
import net.driller.util.NbtKeys;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.entity.vehicle.AbstractMinecartContainer;
import net.minecraft.world.entity.vehicle.MinecartChest;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import oshi.util.tuples.Pair;

import java.util.*;
import java.util.function.Predicate;

@SuppressWarnings("UnusedReturnValue")
public class TrainConnection {
    private final LinkedList<AbstractMinecart> carts;
    private final HashSet<TrainConnectionListener> connectionListeners;
    private final Comparator<AbstractMinecartContainer> containerInstanceOrder =
            Comparator.comparing(c -> (c instanceof MinecartChest) ? 0 : 1);
    private final TreeSet<AbstractMinecartContainer> containers = new TreeSet<>(containerInstanceOrder.thenComparingInt(Entity::hashCode));


    public TrainConnection(ServerLevel level) {
        this(level, new ArrayList<>());
    }

    TrainConnection(ServerLevel level, List<AbstractMinecart> carts) {
        this.carts = new LinkedList<>();
        this.connectionListeners = new HashSet<>();
        this.connectionListeners.add(TrainConnectionsSavedData.get(level.getServer()));
        carts.forEach(cart -> this.addCart(cart, true));
    }

    public static Optional<TrainConnection> getConnection(AbstractMinecart cart) {
        if (!(cart.level() instanceof ServerLevel serverLevel)) return Optional.empty();
        return TrainConnectionsSavedData.get(serverLevel.getServer()).getCachedConnection(cart);
    }

    public List<AbstractMinecart> getCarts() {
        return Collections.unmodifiableList(carts);
    }

    public Optional<AbstractMinecart> getCartBefore(AbstractMinecart cart) {
        int parentIndex = this.carts.indexOf(cart) - 1;
        if (parentIndex < 0) return Optional.empty();
        return Optional.of(this.carts.get(parentIndex));
    }

    public Optional<AbstractMinecart> getCartAfter(AbstractMinecart cart) {
        int index = this.carts.indexOf(cart);
        if (index == -1 || index + 1 > this.carts.size() - 1) return Optional.empty();
        return Optional.of(this.carts.get(index + 1));
    }

    public Optional<ItemStack> requestItemStack(Predicate<ItemStack> request) {
        for (AbstractMinecartContainer container : this.containers) {
            for (int i = 0; i < container.getContainerSize(); i++) {
                ItemStack stack = container.getItem(i);
                if (request.test(stack)) return Optional.of(stack);
            }
        }
        return Optional.empty();
    }

    public boolean registerListener(TrainConnectionListener listener) {
        return this.connectionListeners.add(listener);
    }

    public boolean removeListener(TrainConnectionListener listener) {
        return this.connectionListeners.remove(listener);
    }

    public static TrainConnection mergeAndDissolveOld(ServerLevel level, TrainConnection firstPart, TrainConnection lastPart) {
        List<AbstractMinecart> mergedCarts = new ArrayList<>(firstPart.carts);
        mergedCarts.addAll(lastPart.carts);
        TrainConnection newConnection = new TrainConnection(level, mergedCarts);
        firstPart.connectionListeners.forEach(newConnection::registerListener);
        lastPart.connectionListeners.forEach(newConnection::registerListener);
        firstPart.dissolve();
        lastPart.dissolve();
        return newConnection;
    }

    /**
     * @param splitBefore first cart of the second connection of the split
     */
    @Nullable
    public Pair<TrainConnection, TrainConnection> splitAndDissolveOld(ServerLevel level, AbstractMinecart splitBefore) {
        if (!this.carts.contains(splitBefore)) return null;
        TrainConnection first = new TrainConnection(level);
        TrainConnection last = new TrainConnection(level);
        boolean addToFirst = true;
        for (AbstractMinecart cart : this.carts) {
            if (cart.equals(splitBefore)) {
                addToFirst = false;
            }
            if (addToFirst) {
                first.addCart(cart, true);
            } else {
                last.addCart(cart, true);
            }
        }
        this.dissolve();
        return new Pair<>(first, last);
    }

    public void dissolve() {
        this.connectionListeners.forEach(listener -> listener.beforeTrainDissolved(this));
        this.carts.forEach(cart -> this.removeCart(cart, false));
        this.connectionListeners.forEach(this::removeListener);
    }

    public static int getMaxTrainLength(Level level) {
        return level.getGameRules().getInt(GameRuleInit.MAX_TRAIN_LENGTH);
    }

    public void onMaxLengthChanged(int newMaxLength) {
        List<AbstractMinecart> oldList = new ArrayList<>(this.carts);
        int oldSize = oldList.size();
        if (oldSize <= newMaxLength) return;
        int leftIndex = 0;
        int rightIndex = oldSize - 1;
        int toBeRemoved = oldSize - newMaxLength;

        while (toBeRemoved > 0) {
            removeCart(oldList.get(leftIndex++), true);
            toBeRemoved--;
            if (toBeRemoved > 0) {
                removeCart(oldList.get(rightIndex--), true);
            }
        }
    }

    public boolean addCart(AbstractMinecart cart, boolean notifyListeners) {
        if (this.carts.size() + 1 > getMaxTrainLength(cart.level())) {
            return false;
        }
        this.carts.add(cart);
        if (cart instanceof AbstractMinecartContainer container) {
            this.containers.add(container);
        }
        if (notifyListeners) {
            this.connectionListeners.forEach(listener -> listener.onCartsAdded(this, Set.of(cart)));
        }
        return true;
    }

    public boolean removeCart(AbstractMinecart cart, boolean notifyListeners) {
        boolean removed = this.carts.remove(cart);
        if (removed) {
            if (cart instanceof AbstractMinecartContainer container) {
                this.containers.remove(container);
            }
            if (notifyListeners) {
                this.connectionListeners.forEach(listener -> listener.onCartsRemoved(this, Set.of(cart)));
            }
        }
        return removed;
    }

    public void write(CompoundTag tag) {
        ListTag cartsTag = new ListTag();
        for (AbstractMinecart cart : this.carts) {
            CompoundTag cartTag = new CompoundTag();
            cartTag.putUUID(NbtKeys.UUID, cart.getUUID());
            cartsTag.add(cartTag);
        }
        tag.put(NbtKeys.TRAIN_CARTS, cartsTag);
    }

    public static List<UUID> read(CompoundTag tag) {
        ListTag cartsTag = tag.getList(NbtKeys.TRAIN_CARTS, ListTag.TAG_COMPOUND);
        List<UUID> carts = new ArrayList<>();
        for (int i = 0; i < cartsTag.size(); i++) {
            CompoundTag entryTag = cartsTag.getCompound(i);
            carts.add(entryTag.getUUID(NbtKeys.UUID));
        }
        return carts;
    }
}
