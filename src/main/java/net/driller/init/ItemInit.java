package net.driller.init;

import net.driller.DrillerMain;
import net.driller.item.ExtraMinecartItem;
import net.driller.item.WrenchItem;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.*;

public class ItemInit {

    public static final ResourceKey<CreativeModeTab> DRILLER_ITEM_GROUP = ResourceKey.create(Registries.CREATIVE_MODE_TAB, DrillerMain.identifierOf("item_group"));

    public static final Item WRENCH = register("wrench", new WrenchItem(new Item.Properties().stacksTo(1)));

    public static final Item DRILL_MINECART = register("drill_minecart", new ExtraMinecartItem(ExtraMinecartItem.Type.DRILL, new Item.Properties().stacksTo(1)));
    public static final Item PLACER_MINECART = register("placer_minecart", new ExtraMinecartItem(ExtraMinecartItem.Type.PLACER, new Item.Properties().stacksTo(1)));

    private static Item register(String id, Item item) {
        return register(DrillerMain.identifierOf(id), item);
    }

    private static Item register(ResourceLocation id, Item item) {
        ItemGroupEvents.modifyEntriesEvent(DRILLER_ITEM_GROUP).register(entries -> entries.accept(item));
        return Items.registerItem(id, item);
    }

    public static void init() {
        Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, DRILLER_ITEM_GROUP,
                CreativeModeTab.builder(CreativeModeTab.Row.TOP, 0).icon(() -> new ItemStack(WRENCH)).title(Component.translatable("item.driller.item_group")).build());
    }

}
