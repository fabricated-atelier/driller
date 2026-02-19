package net.driller.entity.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.driller.access.HopperAccess;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.world.entity.Entity;

public class HoppersModel<T extends Entity> extends EntityModel<T> {

    private final ModelPart small_right_hopper;
    private final ModelPart small_left_hopper;

    public HoppersModel(ModelPart root) {
        this.small_right_hopper = root.getChild("small_right_hopper");
        this.small_left_hopper = root.getChild("small_left_hopper");
    }

    public static LayerDefinition getTexturedModelData() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition small_right_hopper = partdefinition.addOrReplaceChild("small_right_hopper", CubeListBuilder.create().texOffs(0, 11).addBox(4.0F, -8.0F, -5.0F, 1.0F, 4.0F, 10.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-5.0F, -4.0F, -5.0F, 10.0F, 1.0F, 10.0F, new CubeDeformation(0.0F))
                .texOffs(22, 11).addBox(-5.0F, -8.0F, -5.0F, 1.0F, 4.0F, 10.0F, new CubeDeformation(0.0F))
                .texOffs(24, 25).addBox(-4.0F, -8.0F, -5.0F, 8.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(24, 30).addBox(-4.0F, -8.0F, 4.0F, 8.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 25).addBox(-3.0F, -3.0F, -3.0F, 6.0F, 1.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-6.0F, 17.0F, 0.0F, 3.1416F, 0.0F, 1.5708F));

        PartDefinition small_left_hopper = partdefinition.addOrReplaceChild("small_left_hopper", CubeListBuilder.create().texOffs(0, 11).addBox(4.0F, -8.0F, -5.0F, 1.0F, 4.0F, 10.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-5.0F, -4.0F, -5.0F, 10.0F, 1.0F, 10.0F, new CubeDeformation(0.0F))
                .texOffs(22, 11).addBox(-5.0F, -8.0F, -5.0F, 1.0F, 4.0F, 10.0F, new CubeDeformation(0.0F))
                .texOffs(24, 25).addBox(-4.0F, -8.0F, -5.0F, 8.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(24, 30).addBox(-4.0F, -8.0F, 4.0F, 8.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 25).addBox(-3.0F, -3.0F, -3.0F, 6.0F, 1.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(6.0F, 17.0F, 0.0F, 0.0F, 0.0F, 1.5708F));

        return LayerDefinition.create(meshdefinition, 64, 64);
    }

    @Override
    public void setupAnim(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        if (entity instanceof HopperAccess hopperAccess) {
            this.small_left_hopper.visible = hopperAccess.hasLeftHopper();
            this.small_right_hopper.visible = hopperAccess.hasRightHopper();
        }
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int i, int j, int k) {
        small_right_hopper.render(poseStack, vertexConsumer, i, j, k);
        small_left_hopper.render(poseStack, vertexConsumer, i, j, k);
    }

}
