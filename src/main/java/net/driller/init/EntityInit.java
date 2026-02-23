package net.driller.init;

import net.driller.DrillerMain;
import net.driller.entity.MinecartDrill;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

public class EntityInit {

    public static final EntityType<MinecartDrill> DRILL_MINECART = register(
            "drill_minecart",
            EntityType.Builder.<MinecartDrill>of(MinecartDrill::new, MobCategory.MISC).sized(0.98F, 0.7F).passengerAttachments(0.1875F).clientTrackingRange(8));

    private static <T extends Entity> EntityType<T> register(String string, EntityType.Builder<T> builder) {
        return Registry.register(BuiltInRegistries.ENTITY_TYPE, DrillerMain.identifierOf(string), builder.build(string));
    }

    public static final ResourceKey<DamageType> DRILL = ResourceKey.create(Registries.DAMAGE_TYPE, DrillerMain.identifierOf("drill"));

    public static void init() {
    }

}
