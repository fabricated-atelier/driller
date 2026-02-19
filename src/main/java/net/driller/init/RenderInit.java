package net.driller.init;

import net.driller.DrillerMain;
import net.driller.entity.model.HoppersModel;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.MinecartRenderer;

@Environment(EnvType.CLIENT)
public class RenderInit {

    public static final ModelLayerLocation HOPPERS_LAYER = new ModelLayerLocation(DrillerMain.identifierOf("hoppers"), "hoppers");

    public static void init() {
        EntityModelLayerRegistry.registerModelLayer(HOPPERS_LAYER, HoppersModel::getTexturedModelData);

        EntityRendererRegistry.register(EntityInit.DRILL_MINECART, context -> new MinecartRenderer<>(context, ModelLayers.FURNACE_MINECART));
    }
}
