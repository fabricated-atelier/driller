package net.driller.entity.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.driller.DrillerMain;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.MinecartModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.phys.Vec3;

// UNUSED
@Environment(EnvType.CLIENT)
public class DrillCartRenderer<T extends AbstractMinecart> extends EntityRenderer<T> {
    private static final ResourceLocation MINECART_LOCATION = DrillerMain.identifierOf("textures/entity/drill_cart.png");
    protected final EntityModel<T> model;

    public DrillCartRenderer(EntityRendererProvider.Context context, ModelLayerLocation modelLayerLocation) {
        super(context);
        this.shadowRadius = 0.7F;
        this.model = new MinecartModel<>(context.bakeLayer(modelLayerLocation));
    }

    @Override
    public void render(T abstractMinecart, float f, float g, PoseStack poseStack, MultiBufferSource multiBufferSource, int i) {
        super.render(abstractMinecart, f, g, poseStack, multiBufferSource, i);
        poseStack.pushPose();
        long l = abstractMinecart.getId() * 493286711L;
        l = l * l * 4392167121L + l * 98761L;
        float h = (((float) (l >> 16 & 7L) + 0.5F) / 8.0F - 0.5F) * 0.004F;
        float j = (((float) (l >> 20 & 7L) + 0.5F) / 8.0F - 0.5F) * 0.004F;
        float k = (((float) (l >> 24 & 7L) + 0.5F) / 8.0F - 0.5F) * 0.004F;
        poseStack.translate(h, j, k);
        double d = Mth.lerp(g, abstractMinecart.xOld, abstractMinecart.getX());
        double e = Mth.lerp(g, abstractMinecart.yOld, abstractMinecart.getY());
        double m = Mth.lerp(g, abstractMinecart.zOld, abstractMinecart.getZ());
        double n = 0.3F;
        Vec3 vec3 = abstractMinecart.getPos(d, e, m);
        float o = Mth.lerp(g, abstractMinecart.xRotO, abstractMinecart.getXRot());
        if (vec3 != null) {
            Vec3 vec32 = abstractMinecart.getPosOffs(d, e, m, 0.3F);
            Vec3 vec33 = abstractMinecart.getPosOffs(d, e, m, -0.3F);
            if (vec32 == null) {
                vec32 = vec3;
            }

            if (vec33 == null) {
                vec33 = vec3;
            }

            poseStack.translate(vec3.x - d, (vec32.y + vec33.y) / 2.0 - e, vec3.z - m);
            Vec3 vec34 = vec33.add(-vec32.x, -vec32.y, -vec32.z);
            if (vec34.length() != 0.0) {
                vec34 = vec34.normalize();
                f = (float) (Math.atan2(vec34.z, vec34.x) * 180.0 / Math.PI);
                o = (float) (Math.atan(vec34.y) * 73.0);
            }
        }

        poseStack.translate(0.0F, 1.5F, 0.0F);
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - f));
        poseStack.mulPose(Axis.ZP.rotationDegrees(-o));
        float p = abstractMinecart.getHurtTime() - g;
        float q = abstractMinecart.getDamage() - g;
        if (q < 0.0F) {
            q = 0.0F;
        }

        if (p > 0.0F) {
            poseStack.mulPose(Axis.XP.rotationDegrees(Mth.sin(p) * p * q / 10.0F * abstractMinecart.getHurtDir()));
        }

        poseStack.scale(-1.0F, -1.0F, 1.0F);
        this.model.setupAnim(abstractMinecart, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F);
        VertexConsumer vertexConsumer = multiBufferSource.getBuffer(this.model.renderType(this.getTextureLocation(abstractMinecart)));
        this.model.renderToBuffer(poseStack, vertexConsumer, i, OverlayTexture.NO_OVERLAY);
        poseStack.popPose();
    }

    @Override
    public ResourceLocation getTextureLocation(T abstractMinecart) {
        return MINECART_LOCATION;
    }

}

