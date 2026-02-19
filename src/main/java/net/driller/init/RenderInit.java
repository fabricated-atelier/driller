package net.driller.init;

import net.driller.DrillerMain;
import net.driller.entity.model.HoppersModel;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.minecraft.client.model.geom.ModelLayerLocation;

@Environment(EnvType.CLIENT)
public class RenderInit {

    public static final ModelLayerLocation HOPPERS_LAYER = new ModelLayerLocation(DrillerMain.identifierOf("hoppers"), "hoppers");

    public static void init() {
        EntityModelLayerRegistry.registerModelLayer(HOPPERS_LAYER, HoppersModel::getTexturedModelData);
    }
}
