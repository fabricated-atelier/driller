package net.driller.entity.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.driller.entity.MinecartDrill;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.world.entity.Entity;

public class DrillModel<T extends Entity> extends EntityModel<T> {

    private final ModelPart inner;
    private final ModelPart engine;
    private final ModelPart axel;
    private final ModelPart drill;
    private final ModelPart big_part;
    private final ModelPart big_mid_part;
    private final ModelPart mid_part;
    private final ModelPart small_part;

    public DrillModel(ModelPart root) {
        this.inner = root.getChild("inner");
        this.engine = this.inner.getChild("engine");
        this.axel = this.inner.getChild("axel");
        this.drill = this.inner.getChild("drill");
        this.big_part = this.drill.getChild("big_part");
        this.big_mid_part = this.big_part.getChild("big_mid_part");
        this.mid_part = this.big_mid_part.getChild("mid_part");
        this.small_part = this.mid_part.getChild("small_part");
    }

    public static LayerDefinition getTexturedModelData() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition inner = partdefinition.addOrReplaceChild("inner", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 24.0F, 0.0F, 0.0F, 0.0F, -3.1416F));

        PartDefinition cube_r1 = inner.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(0, 20).addBox(4.0F, -5.0F, -5.0F, 1.0F, 10.0F, 10.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -6.0F, -15.0F, 0.0F, -1.5708F, 0.0F));

        PartDefinition engine = inner.addOrReplaceChild("engine", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition cube_r2 = engine.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(0, 0).addBox(-5.0F, -5.0F, -5.0F, 10.0F, 10.0F, 10.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -8.0F, 3.0F, 0.0F, -1.5708F, 0.0F));

        PartDefinition axel = inner.addOrReplaceChild("axel", CubeListBuilder.create().texOffs(22, 32).addBox(-1.5F, -1.5F, -8.0F, 3.0F, 3.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -6.0F, -2.0F, 0.0F, 0.0F, 0.7854F));

        PartDefinition drill = inner.addOrReplaceChild("drill", CubeListBuilder.create(), PartPose.offset(0.0F, -6.0F, -10.0F));

        PartDefinition big_part = drill.addOrReplaceChild("big_part", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition big_r1 = big_part.addOrReplaceChild("big_r1", CubeListBuilder.create().texOffs(22, 20).addBox(-4.0F, -4.0F, -5.0F, 8.0F, 8.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.2618F));

        PartDefinition big_mid_part = big_part.addOrReplaceChild("big_mid_part", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, -5.0F));

        PartDefinition big_mid_part_r1 = big_mid_part.addOrReplaceChild("big_mid_part_r1", CubeListBuilder.create().texOffs(0, 40).addBox(-3.0F, -3.0F, -3.0F, 6.0F, 6.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.2618F));

        PartDefinition mid_part = big_mid_part.addOrReplaceChild("mid_part", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, -3.0F));

        PartDefinition mid_part_r1 = mid_part.addOrReplaceChild("mid_part_r1", CubeListBuilder.create().texOffs(40, 0).addBox(-2.0F, -2.0F, -3.0F, 4.0F, 4.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.2618F));

        PartDefinition small_part = mid_part.addOrReplaceChild("small_part", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, -3.0F));

        PartDefinition small_part_r1 = small_part.addOrReplaceChild("small_part_r1", CubeListBuilder.create().texOffs(40, 7).addBox(-1.0F, -1.0F, -2.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.2618F));

        return LayerDefinition.create(meshdefinition, 64, 64);
    }


    @Override
    public void setupAnim(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        float drillPos = ((MinecartDrill) entity).getDrillRotation();
        this.drill.zRot = drillPos;
        this.big_part.zRot = drillPos * 0.2f;
        this.mid_part.zRot = drillPos * 0.8f;
        this.small_part.zRot = drillPos * 1.5f;
        this.axel.zRot = drillPos;
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int i, int j, int k) {
        inner.render(poseStack, vertexConsumer, i, j, k);
    }

}
