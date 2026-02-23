package net.driller.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.driller.DrillerMain;
import net.driller.entity.MinecartDrill;
import net.driller.entity.model.DrillModel;
import net.driller.entity.model.HoppersModel;
import net.driller.init.RenderInit;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MinecartRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
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

    @Unique
    private EntityModel<T> drill;
    @Unique
    private static final ResourceLocation DRILL_LOCATION = DrillerMain.identifierOf("textures/entity/drill.png");

    public MinecartRendererMixin(EntityRendererProvider.Context context) {
        super(context);
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void initMixin(EntityRendererProvider.Context context, ModelLayerLocation modelLayerLocation, CallbackInfo info) {
        this.hoppers = new HoppersModel<>(context.bakeLayer(RenderInit.HOPPERS_LAYER));
        this.drill = new DrillModel<>(context.bakeLayer(RenderInit.DRILL_LAYER));
    }

    @Inject(method = "render(Lnet/minecraft/world/entity/vehicle/AbstractMinecart;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;scale(FFF)V", ordinal = 1))
    private void renderMixin(T abstractMinecart, float f, float g, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, CallbackInfo info) {
        if (abstractMinecart.getMinecartType().equals(AbstractMinecart.Type.HOPPER)) {
            poseStack.pushPose();
            poseStack.translate(0f, -1f, 0f);
            poseStack.mulPose(Axis.YP.rotationDegrees(90.0F));
            this.hoppers.setupAnim(abstractMinecart, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F);
            this.hoppers.renderToBuffer(poseStack, multiBufferSource.getBuffer(this.hoppers.renderType(HOPPERS_LOCATION)), i, OverlayTexture.NO_OVERLAY);
            poseStack.popPose();
        } else if (abstractMinecart instanceof MinecartDrill minecartDrill) {
            poseStack.pushPose();
            poseStack.translate(0f, -1.875f, 0f);

            Direction drillFacing = minecartDrill.getAbsoluteDrillFacing();
            float facingAngle = switch (drillFacing) {
                case SOUTH -> -90.0F;
                case WEST -> 90.0F;
                case NORTH -> 90.0F;
                case EAST -> -90.0F;
                default -> 0.0F;
            };
            poseStack.mulPose(Axis.YP.rotationDegrees(facingAngle));

            this.drill.setupAnim(abstractMinecart, 0.0F, 0.0F, minecartDrill.getDrillRotation(), 0.0F, 0.0F);
            this.drill.renderToBuffer(poseStack, multiBufferSource.getBuffer(this.drill.renderType(DRILL_LOCATION)), i, OverlayTexture.NO_OVERLAY);
            poseStack.popPose();
        }
    }

}
