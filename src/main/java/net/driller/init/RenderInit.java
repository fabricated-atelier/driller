package net.driller.init;

import net.driller.DrillerMain;
import net.driller.entity.model.DrillCartModel;
import net.driller.entity.model.DrillModel;
import net.driller.entity.model.HoppersModel;
import net.driller.entity.render.DrillCartRenderer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.MinecartRenderer;

@Environment(EnvType.CLIENT)
public class RenderInit {

    public static final ModelLayerLocation DRILL_LAYER = new ModelLayerLocation(DrillerMain.identifierOf("drill"), "drill");
    public static final ModelLayerLocation HOPPERS_LAYER = new ModelLayerLocation(DrillerMain.identifierOf("hoppers"), "hoppers");

    public static void init() {
        EntityModelLayerRegistry.registerModelLayer(HOPPERS_LAYER, HoppersModel::getTexturedModelData);
//        EntityModelLayerRegistry.registerModelLayer(DRILL_LAYER, DrillCartModel::getTexturedModelData);
        EntityModelLayerRegistry.registerModelLayer(DRILL_LAYER, DrillModel::getTexturedModelData);

//        EntityRendererRegistry.register(EntityInit.DRILL_MINECART, context -> new MinecartRenderer<>(context, DRILL_LAYER));
        EntityRendererRegistry.register(EntityInit.DRILL_MINECART, context -> new MinecartRenderer<>(context, ModelLayers.MINECART));
    }
}
