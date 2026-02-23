package net.driller.entity.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.world.entity.Entity;

@Environment(EnvType.CLIENT)
public class DrillCartModel<T extends Entity> extends HierarchicalModel<T> {
    private final ModelPart base;
    private final ModelPart front;
    private final ModelPart left;
    private final ModelPart right;
    private final ModelPart back;
    private final ModelPart bottom;
    private final ModelPart inner;
    private final ModelPart engine;
    private final ModelPart axel;
    private final ModelPart drill;
    private final ModelPart big_part;
    private final ModelPart big_mid_part;
    private final ModelPart mid_part;
    private final ModelPart small_part;

    public DrillCartModel(ModelPart modelPart) {
        this.base = modelPart.getChild("base");
        this.front = this.base.getChild("front");
        this.left = this.base.getChild("left");
        this.right = this.base.getChild("right");
        this.back = this.base.getChild("back");
        this.bottom = this.base.getChild("bottom");
        this.inner = this.base.getChild("inner");
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

        PartDefinition base = partdefinition.addOrReplaceChild("base", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));

        PartDefinition front = base.addOrReplaceChild("front", CubeListBuilder.create().texOffs(18, 38).addBox(-8.0F, -4.8333F, 1.6667F, 6.0F, 8.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(44, 14).addBox(-2.0F, 1.1667F, -0.3333F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(18, 38).mirror().addBox(2.0F, -4.8333F, 1.6667F, 6.0F, 8.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(44, 10).addBox(-2.0F, -4.8333F, 1.6667F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -6.1667F, -5.3333F, 0.0F, 3.1416F, 0.0F));

        PartDefinition left = base.addOrReplaceChild("left", CubeListBuilder.create().texOffs(0, 38).addBox(-8.0F, -9.0F, -1.0F, 16.0F, 8.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-7.0F, -2.0F, 1.0F, 0.0F, -1.5708F, 0.0F));

        PartDefinition right = base.addOrReplaceChild("right", CubeListBuilder.create().texOffs(0, 38).mirror().addBox(-8.0F, -9.0F, -1.0F, 16.0F, 8.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(7.0F, -2.0F, 1.0F, 0.0F, 1.5708F, 0.0F));

        PartDefinition back = base.addOrReplaceChild("back", CubeListBuilder.create().texOffs(0, 38).addBox(-8.0F, -9.0F, -1.0F, 16.0F, 8.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -2.0F, 10.0F));

        PartDefinition bottom = base.addOrReplaceChild("bottom", CubeListBuilder.create().texOffs(0, 0).addBox(-10.0F, -8.0F, -1.0F, 20.0F, 16.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -2.0F, 1.0F, 0.0F, -1.5708F, 1.5708F));

        PartDefinition inner = base.addOrReplaceChild("inner", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition cube_r1 = inner.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(36, 38).addBox(4.0F, -5.0F, -5.0F, 1.0F, 10.0F, 10.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -6.0F, -14.0F, 0.0F, -1.5708F, 0.0F));

        PartDefinition engine = inner.addOrReplaceChild("engine", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition cube_r2 = engine.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(0, 18).addBox(-5.0F, -5.0F, -5.0F, 10.0F, 10.0F, 10.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -8.0F, 3.0F, 0.0F, -1.5708F, 0.0F));

        PartDefinition axel = inner.addOrReplaceChild("axel", CubeListBuilder.create().texOffs(44, 0).addBox(-1.5F, -1.5F, -7.0F, 3.0F, 3.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -7.0F, -2.0F, 0.0F, 0.0F, 0.7854F));

        PartDefinition drill = inner.addOrReplaceChild("drill", CubeListBuilder.create(), PartPose.offset(0.0F, -6.0F, -9.0F));

        PartDefinition big_part = drill.addOrReplaceChild("big_part", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition big_r1 = big_part.addOrReplaceChild("big_r1", CubeListBuilder.create().texOffs(40, 18).addBox(-4.0F, -4.0F, -5.0F, 8.0F, 8.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.2618F));

        PartDefinition big_mid_part = big_part.addOrReplaceChild("big_mid_part", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, -5.0F));

        PartDefinition big_mid_part_r1 = big_mid_part.addOrReplaceChild("big_mid_part_r1", CubeListBuilder.create().texOffs(0, 48).addBox(-3.0F, -3.0F, -3.0F, 6.0F, 6.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.2618F));

        PartDefinition mid_part = big_mid_part.addOrReplaceChild("mid_part", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, -3.0F));

        PartDefinition mid_part_r1 = mid_part.addOrReplaceChild("mid_part_r1", CubeListBuilder.create().texOffs(40, 30).addBox(-2.0F, -2.0F, -3.0F, 4.0F, 4.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.2618F));

        PartDefinition small_part = mid_part.addOrReplaceChild("small_part", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, -3.0F));

        PartDefinition small_part_r1 = small_part.addOrReplaceChild("small_part_r1", CubeListBuilder.create().texOffs(54, 30).addBox(-1.0F, -1.0F, -2.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.2618F));

        return LayerDefinition.create(meshdefinition, 64, 64);
    }

    @Override
    public void setupAnim(T entity, float f, float g, float h, float i, float j) {
    }

    @Override
    public ModelPart root() {
        return this.base;
    }
}

