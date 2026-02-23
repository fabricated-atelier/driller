package net.driller.entity;

import net.driller.init.EntityInit;
import net.driller.init.ItemInit;
import net.driller.util.DrillerLogic;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import java.util.List;

public class MinecartDrill extends AbstractMinecart {

    private static final EntityDataAccessor<Boolean> DATA_ID_DRILL_FLIPPED = SynchedEntityData.defineId(MinecartDrill.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DATA_ID_FUEL = SynchedEntityData.defineId(MinecartDrill.class, EntityDataSerializers.BOOLEAN);
    private int fuel;
    private float drillRotation = 0.0f;
    private static final Ingredient INGREDIENT = Ingredient.of(Items.COAL, Items.CHARCOAL);

    public MinecartDrill(EntityType<? extends MinecartDrill> entityType, Level level) {
        super(entityType, level);
    }

    public MinecartDrill(Level level, double d, double e, double f) {
        super(EntityInit.DRILL_MINECART, level, d, e, f);
    }

    @Override
    public Item getDropItem() {
        return ItemInit.DRILL_MINECART;
    }

    @Override
    public AbstractMinecart.Type getMinecartType() {
        return AbstractMinecart.Type.FURNACE;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_ID_FUEL, false);
        builder.define(DATA_ID_DRILL_FLIPPED, false);
    }

    public Direction getAbsoluteDrillFacing() {
        Direction cartFacing = DrillerLogic.getFacingDirection(this);
        return this.isDrillFlipped() ? cartFacing.getOpposite() : cartFacing;
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.level().isClientSide()) {
            if (this.fuel > 0) {
                this.fuel--;
                if (this.fuel % 3 == 0) {
                    this.damageEntitiesInFront();
                }
            }

            this.setHasFuel(this.fuel > 0);
        } else if (this.hasFuel()) {
            this.drillRotation += 0.2f;
        }

        if (this.hasFuel() && this.random.nextInt(4) == 0) {
            Direction dir = this.getAbsoluteDrillFacing();
            Direction sideDir = dir.getClockWise();
            double sideOffset = 0.27;
            double backOffset = 0.2;

            double backX = this.getX() - dir.getStepX() * backOffset;
            double backZ = this.getZ() - dir.getStepZ() * backOffset;

            double x1 = backX + sideDir.getStepX() * sideOffset;
            double z1 = backZ + sideDir.getStepZ() * sideOffset;

            double x2 = backX - sideDir.getStepX() * sideOffset;
            double z2 = backZ - sideDir.getStepZ() * sideOffset;

            this.level().addParticle(ParticleTypes.SMOKE, x1, this.getY() + 0.8, z1, 0.0, 0.0, 0.0);
            this.level().addParticle(ParticleTypes.SMOKE, x2, this.getY() + 0.8, z2, 0.0, 0.0, 0.0);
        }
    }

    @Override
    protected double getMaxSpeed() {
        return (this.isInWater() ? 3.0 : 4.0) / 20.0;
    }

    @Override
    public InteractionResult interact(Player player, InteractionHand interactionHand) {
        ItemStack itemStack = player.getItemInHand(interactionHand);
        if (itemStack.is(ItemInit.WRENCH)) {
            if (!this.level().isClientSide()) {
                this.setDrillFlipped(!this.isDrillFlipped());
            }
            return InteractionResult.sidedSuccess(this.level().isClientSide());
        }

        if (INGREDIENT.test(itemStack) && this.fuel + 3600 <= 32000) {
            itemStack.consume(1, player);
            this.fuel += 3600;
        }
        return InteractionResult.sidedSuccess(this.level().isClientSide());
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compoundTag) {
        super.addAdditionalSaveData(compoundTag);
        compoundTag.putShort("Fuel", (short) this.fuel);
        compoundTag.putBoolean("DrillFlipped", this.isDrillFlipped());

    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compoundTag) {
        super.readAdditionalSaveData(compoundTag);
        this.fuel = compoundTag.getShort("Fuel");
        if (compoundTag.contains("DrillFlipped")) {
            this.setDrillFlipped(compoundTag.getBoolean("DrillFlipped"));
        }
    }

    private void damageEntitiesInFront() {
        Direction dir = this.getAbsoluteDrillFacing();

        AABB drillZone = this.getBoundingBox().expandTowards(dir.getStepX() * 0.5, 0, dir.getStepZ() * 0.5).inflate(0.2);

        List<Entity> targets = this.level().getEntities(this, drillZone, entity -> entity instanceof LivingEntity);

        for (Entity target : targets) {
            target.hurt(source(this.level(), this), 4.0F);
            target.push(dir.getStepX() * 0.2, 0.1, dir.getStepZ() * 0.2);
        }
    }

    public boolean hasFuel() {
        return this.entityData.get(DATA_ID_FUEL);
    }

    public void setHasFuel(boolean bl) {
        this.entityData.set(DATA_ID_FUEL, bl);
    }

    @Override
    public BlockState getDefaultDisplayBlockState() {
        return super.getDefaultDisplayBlockState();
    }

    public float getDrillRotation() {
        return this.drillRotation;
    }

    public boolean isDrillFlipped() {
        return this.entityData.get(DATA_ID_DRILL_FLIPPED);
    }

    public void setDrillFlipped(boolean flipped) {
        this.entityData.set(DATA_ID_DRILL_FLIPPED, flipped);
    }

    private static DamageSource source(Level level, Entity cause) {
        return new DamageSource(level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(EntityInit.DRILL), cause);
    }
}
