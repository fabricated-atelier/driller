package net.driller.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.driller.DrillerMain;
import net.driller.entity.model.HoppersModel;
import net.driller.init.RenderInit;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.MinecartModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MinecartRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(MinecartRenderer.class)
public abstract class MinecartRendererMixin<T extends AbstractMinecart> extends EntityRenderer<T> {

    @Unique
    private EntityModel<T> hoppers;
    @Unique
    private static final ResourceLocation HOPPERS_LOCATION = DrillerMain.identifierOf("textures/entity/hoppers.png");

    public MinecartRendererMixin(EntityRendererProvider.Context context) {
        super(context);
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void initMixin(EntityRendererProvider.Context context, ModelLayerLocation modelLayerLocation, CallbackInfo info) {
        this.hoppers = new HoppersModel<>(context.bakeLayer(RenderInit.HOPPERS_LAYER));
    }

    @Inject(method = "render(Lnet/minecraft/world/entity/vehicle/AbstractMinecart;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V", at = @At(value = "INVOKE",target = "Lcom/mojang/blaze3d/vertex/PoseStack;scale(FFF)V",ordinal = 1))
    private void renderMixin(T abstractMinecart, float f, float g, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, CallbackInfo info) {
        if (abstractMinecart.getMinecartType().equals(AbstractMinecart.Type.HOPPER)) {
            poseStack.pushPose();
            poseStack.translate(0f,-1f,0f);
            poseStack.mulPose(Axis.YP.rotationDegrees(90.0F));
            this.hoppers.setupAnim(abstractMinecart, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F);
            this.hoppers.renderToBuffer(poseStack, multiBufferSource.getBuffer(this.hoppers.renderType(HOPPERS_LOCATION)), i, OverlayTexture.NO_OVERLAY);
            poseStack.popPose();
        }
    }

}
