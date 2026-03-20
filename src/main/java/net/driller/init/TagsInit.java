package net.driller.init;

import net.driller.DrillerMain;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;

import java.util.ArrayList;
import java.util.List;

public class TagsInit {
    public static final List<TagKey<?>> ALL_TAGS = new ArrayList<>();

    public static void initialize() {
        ItemTags.initialize();
        EntityTags.initialize();
    }


    public static class ItemTags {
        public static final List<TagKey<Item>> ALL_ITEM_TAGS = new ArrayList<>();

        public static final TagKey<Item> WRENCHES = createTag("wrenches");

        @SuppressWarnings("SameParameterValue")
        private static TagKey<Item> createTag(String name) {
            TagKey<Item> tagKey = TagKey.create(Registries.ITEM, DrillerMain.identifierOf(name));
            ALL_ITEM_TAGS.add(tagKey);
            ALL_TAGS.add(tagKey);
            return tagKey;
        }

        public static void initialize() {
            // static initialisation
        }
    }

    public static class EntityTags {
        public static final List<TagKey<EntityType<?>>> ALL_ENTITY_TAGS = new ArrayList<>();

        public static final TagKey<EntityType<?>> CARTS = createTag("carts");

        @SuppressWarnings("SameParameterValue")
        private static TagKey<EntityType<?>> createTag(String name) {
            TagKey<EntityType<?>> tagKey = TagKey.create(Registries.ENTITY_TYPE, DrillerMain.identifierOf(name));
            ALL_ENTITY_TAGS.add(tagKey);
            ALL_TAGS.add(tagKey);
            return tagKey;
        }

        public static void initialize() {
            // static initialisation
        }
    }
}
