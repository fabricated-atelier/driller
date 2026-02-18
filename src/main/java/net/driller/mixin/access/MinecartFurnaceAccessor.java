package net.driller.mixin.access;

import net.minecraft.world.entity.vehicle.MinecartFurnace;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(MinecartFurnace.class)
public interface MinecartFurnaceAccessor {

    @Accessor("fuel")
    int getFuel();

    @Accessor("fuel")
    void setFuel(int fuel);
}
