package net.minecraft.world.entity.monster;

import net.minecraft.core.BlockPosition;
import net.minecraft.sounds.SoundEffect;
import net.minecraft.sounds.SoundEffects;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.entity.EntitySize;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.EnumMobSpawn;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.World;
import net.minecraft.world.level.WorldAccess;
import org.joml.Vector3f;

public class EntityZombieHusk extends EntityZombie {

    public EntityZombieHusk(EntityTypes<? extends EntityZombieHusk> entitytypes, World world) {
        super(entitytypes, world);
    }

    public static boolean checkHuskSpawnRules(EntityTypes<EntityZombieHusk> entitytypes, WorldAccess worldaccess, EnumMobSpawn enummobspawn, BlockPosition blockposition, RandomSource randomsource) {
        return checkMonsterSpawnRules(entitytypes, worldaccess, enummobspawn, blockposition, randomsource) && (EnumMobSpawn.isSpawner(enummobspawn) || worldaccess.canSeeSky(blockposition));
    }

    @Override
    protected boolean isSunSensitive() {
        return false;
    }

    @Override
    protected SoundEffect getAmbientSound() {
        return SoundEffects.HUSK_AMBIENT;
    }

    @Override
    protected SoundEffect getHurtSound(DamageSource damagesource) {
        return SoundEffects.HUSK_HURT;
    }

    @Override
    protected SoundEffect getDeathSound() {
        return SoundEffects.HUSK_DEATH;
    }

    @Override
    protected SoundEffect getStepSound() {
        return SoundEffects.HUSK_STEP;
    }

    @Override
    public boolean doHurtTarget(Entity entity) {
        boolean flag = super.doHurtTarget(entity);

        if (flag && this.getMainHandItem().isEmpty() && entity instanceof EntityLiving) {
            float f = this.level().getCurrentDifficultyAt(this.blockPosition()).getEffectiveDifficulty();

            ((EntityLiving) entity).addEffect(new MobEffect(MobEffects.HUNGER, 140 * (int) f), this);
        }

        return flag;
    }

    @Override
    protected boolean convertsInWater() {
        return true;
    }

    @Override
    protected void doUnderWaterConversion() {
        this.convertToZombieType(EntityTypes.ZOMBIE);
        if (!this.isSilent()) {
            this.level().levelEvent((EntityHuman) null, 1041, this.blockPosition(), 0);
        }

    }

    @Override
    protected ItemStack getSkull() {
        return ItemStack.EMPTY;
    }

    @Override
    protected Vector3f getPassengerAttachmentPoint(Entity entity, EntitySize entitysize, float f) {
        return new Vector3f(0.0F, entitysize.height + 0.125F * f, 0.0F);
    }
}
