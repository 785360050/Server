package net.minecraft.world.entity.monster;

import javax.annotation.Nullable;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.DifficultyDamageScaler;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.entity.EntityPose;
import net.minecraft.world.entity.EntitySize;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.EnumMobSpawn;
import net.minecraft.world.entity.GroupDataEntity;
import net.minecraft.world.entity.ai.attributes.AttributeProvider;
import net.minecraft.world.entity.ai.attributes.GenericAttributes;
import net.minecraft.world.level.World;
import net.minecraft.world.level.WorldAccess;
import org.joml.Vector3f;

public class EntityCaveSpider extends EntitySpider {

    public EntityCaveSpider(EntityTypes<? extends EntityCaveSpider> entitytypes, World world) {
        super(entitytypes, world);
    }

    public static AttributeProvider.Builder createCaveSpider() {
        return EntitySpider.createAttributes().add(GenericAttributes.MAX_HEALTH, 12.0D);
    }

    @Override
    public boolean doHurtTarget(Entity entity) {
        if (super.doHurtTarget(entity)) {
            if (entity instanceof EntityLiving) {
                byte b0 = 0;

                if (this.level().getDifficulty() == EnumDifficulty.NORMAL) {
                    b0 = 7;
                } else if (this.level().getDifficulty() == EnumDifficulty.HARD) {
                    b0 = 15;
                }

                if (b0 > 0) {
                    ((EntityLiving) entity).addEffect(new MobEffect(MobEffects.POISON, b0 * 20, 0), this);
                }
            }

            return true;
        } else {
            return false;
        }
    }

    @Nullable
    @Override
    public GroupDataEntity finalizeSpawn(WorldAccess worldaccess, DifficultyDamageScaler difficultydamagescaler, EnumMobSpawn enummobspawn, @Nullable GroupDataEntity groupdataentity, @Nullable NBTTagCompound nbttagcompound) {
        return groupdataentity;
    }

    @Override
    protected float getStandingEyeHeight(EntityPose entitypose, EntitySize entitysize) {
        return 0.45F;
    }

    @Override
    protected Vector3f getPassengerAttachmentPoint(Entity entity, EntitySize entitysize, float f) {
        return new Vector3f(0.0F, entitysize.height, 0.0F);
    }

    @Override
    protected float ridingOffset(Entity entity) {
        return entity.getBbWidth() <= this.getBbWidth() ? -0.21875F : 0.0F;
    }
}
