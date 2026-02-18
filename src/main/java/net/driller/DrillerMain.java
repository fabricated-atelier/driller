package net.driller;

import net.driller.init.EventInit;
import net.driller.init.ItemInit;
import net.fabricmc.api.ModInitializer;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DrillerMain implements ModInitializer {

    public static final String MOD_ID = "driller";

    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        EventInit.init();
        ItemInit.init();
    }

    public static ResourceLocation identifierOf(String id) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, id);
    }
}