package net.driller.datagen;

import net.driller.init.EntityInit;
import net.driller.init.ItemInit;
import net.driller.init.TagsInit;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;

import java.util.concurrent.CompletableFuture;

public class DrillerTagsGenerator {
    public static class ItemTags extends FabricTagProvider.ItemTagProvider {
        public ItemTags(FabricDataOutput output, CompletableFuture<HolderLookup.Provider> completableFuture) {
            super(output, completableFuture);
        }

        @Override
        protected void addTags(HolderLookup.Provider provider) {
            getOrCreateTagBuilder(TagsInit.ItemTags.WRENCHES)
                    .add(ItemInit.WRENCH)
                    .addOptionalTag(ResourceLocation.fromNamespaceAndPath("c", "wrench"))
                    .addOptionalTag(ResourceLocation.fromNamespaceAndPath("c", "wrenches"));
        }
    }

    public static class EntityTags extends FabricTagProvider.EntityTypeTagProvider {
        public EntityTags(FabricDataOutput output, CompletableFuture<HolderLookup.Provider> completableFuture) {
            super(output, completableFuture);
        }

        @Override
        protected void addTags(HolderLookup.Provider provider) {
            getOrCreateTagBuilder(TagsInit.EntityTags.CARTS)
                    .add(EntityType.MINECART, EntityType.CHEST_MINECART, EntityType.HOPPER_MINECART, EntityType.FURNACE_MINECART,
                            EntityType.TNT_MINECART, EntityType.COMMAND_BLOCK_MINECART, EntityType.SPAWNER_MINECART,
                            EntityInit.DRILL_MINECART);
        }
    }


    public static void registerAll(FabricDataGenerator.Pack pack) {
        pack.addProvider(ItemTags::new);
        pack.addProvider(EntityTags::new);
    }
}
