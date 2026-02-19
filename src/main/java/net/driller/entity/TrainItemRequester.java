package net.driller.entity;

import net.minecraft.world.item.ItemStack;

import java.util.Optional;
import java.util.function.Predicate;

@FunctionalInterface
public interface TrainItemRequester {
    Optional<ItemStack> requestItem(Predicate<ItemStack> stack);
}
