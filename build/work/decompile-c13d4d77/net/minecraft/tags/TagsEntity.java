package net.minecraft.tags;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.world.entity.EntityTypes;

public final class TagsEntity {

    public static final TagKey<EntityTypes<?>> SKELETONS = create("skeletons");
    public static final TagKey<EntityTypes<?>> ZOMBIES = create("zombies");
    public static final TagKey<EntityTypes<?>> RAIDERS = create("raiders");
    public static final TagKey<EntityTypes<?>> UNDEAD = create("undead");
    public static final TagKey<EntityTypes<?>> BEEHIVE_INHABITORS = create("beehive_inhabitors");
    public static final TagKey<EntityTypes<?>> ARROWS = create("arrows");
    public static final TagKey<EntityTypes<?>> IMPACT_PROJECTILES = create("impact_projectiles");
    public static final TagKey<EntityTypes<?>> POWDER_SNOW_WALKABLE_MOBS = create("powder_snow_walkable_mobs");
    public static final TagKey<EntityTypes<?>> AXOLOTL_ALWAYS_HOSTILES = create("axolotl_always_hostiles");
    public static final TagKey<EntityTypes<?>> AXOLOTL_HUNT_TARGETS = create("axolotl_hunt_targets");
    public static final TagKey<EntityTypes<?>> FREEZE_IMMUNE_ENTITY_TYPES = create("freeze_immune_entity_types");
    public static final TagKey<EntityTypes<?>> FREEZE_HURTS_EXTRA_TYPES = create("freeze_hurts_extra_types");
    public static final TagKey<EntityTypes<?>> CAN_BREATHE_UNDER_WATER = create("can_breathe_under_water");
    public static final TagKey<EntityTypes<?>> FROG_FOOD = create("frog_food");
    public static final TagKey<EntityTypes<?>> FALL_DAMAGE_IMMUNE = create("fall_damage_immune");
    public static final TagKey<EntityTypes<?>> DISMOUNTS_UNDERWATER = create("dismounts_underwater");
    public static final TagKey<EntityTypes<?>> NON_CONTROLLING_RIDER = create("non_controlling_rider");
    public static final TagKey<EntityTypes<?>> DEFLECTS_ARROWS = create("deflects_arrows");
    public static final TagKey<EntityTypes<?>> DEFLECTS_TRIDENTS = create("deflects_tridents");
    public static final TagKey<EntityTypes<?>> CAN_TURN_IN_BOATS = create("can_turn_in_boats");

    private TagsEntity() {}

    private static TagKey<EntityTypes<?>> create(String s) {
        return TagKey.create(Registries.ENTITY_TYPE, new MinecraftKey(s));
    }
}
