package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL.TypeReference;

public class DataConverterTypes {

    public static final TypeReference LEVEL = () -> {
        return "level";
    };
    public static final TypeReference PLAYER = () -> {
        return "player";
    };
    public static final TypeReference CHUNK = () -> {
        return "chunk";
    };
    public static final TypeReference HOTBAR = () -> {
        return "hotbar";
    };
    public static final TypeReference OPTIONS = () -> {
        return "options";
    };
    public static final TypeReference STRUCTURE = () -> {
        return "structure";
    };
    public static final TypeReference STATS = () -> {
        return "stats";
    };
    public static final TypeReference SAVED_DATA_COMMAND_STORAGE = () -> {
        return "saved_data/command_storage";
    };
    public static final TypeReference SAVED_DATA_FORCED_CHUNKS = () -> {
        return "saved_data/chunks";
    };
    public static final TypeReference SAVED_DATA_MAP_DATA = () -> {
        return "saved_data/map_data";
    };
    public static final TypeReference SAVED_DATA_MAP_INDEX = () -> {
        return "saved_data/idcounts";
    };
    public static final TypeReference SAVED_DATA_RAIDS = () -> {
        return "saved_data/raids";
    };
    public static final TypeReference SAVED_DATA_RANDOM_SEQUENCES = () -> {
        return "saved_data/random_sequences";
    };
    public static final TypeReference SAVED_DATA_STRUCTURE_FEATURE_INDICES = () -> {
        return "saved_data/structure_feature_indices";
    };
    public static final TypeReference SAVED_DATA_SCOREBOARD = () -> {
        return "saved_data/scoreboard";
    };
    public static final TypeReference ADVANCEMENTS = () -> {
        return "advancements";
    };
    public static final TypeReference POI_CHUNK = () -> {
        return "poi_chunk";
    };
    public static final TypeReference ENTITY_CHUNK = () -> {
        return "entity_chunk";
    };
    public static final TypeReference BLOCK_ENTITY = () -> {
        return "block_entity";
    };
    public static final TypeReference ITEM_STACK = () -> {
        return "item_stack";
    };
    public static final TypeReference BLOCK_STATE = () -> {
        return "block_state";
    };
    public static final TypeReference ENTITY_NAME = () -> {
        return "entity_name";
    };
    public static final TypeReference ENTITY_TREE = () -> {
        return "entity_tree";
    };
    public static final TypeReference ENTITY = () -> {
        return "entity";
    };
    public static final TypeReference BLOCK_NAME = () -> {
        return "block_name";
    };
    public static final TypeReference ITEM_NAME = () -> {
        return "item_name";
    };
    public static final TypeReference GAME_EVENT_NAME = () -> {
        return "game_event_name";
    };
    public static final TypeReference UNTAGGED_SPAWNER = () -> {
        return "untagged_spawner";
    };
    public static final TypeReference STRUCTURE_FEATURE = () -> {
        return "structure_feature";
    };
    public static final TypeReference OBJECTIVE = () -> {
        return "objective";
    };
    public static final TypeReference TEAM = () -> {
        return "team";
    };
    public static final TypeReference RECIPE = () -> {
        return "recipe";
    };
    public static final TypeReference BIOME = () -> {
        return "biome";
    };
    public static final TypeReference MULTI_NOISE_BIOME_SOURCE_PARAMETER_LIST = () -> {
        return "multi_noise_biome_source_parameter_list";
    };
    public static final TypeReference WORLD_GEN_SETTINGS = () -> {
        return "world_gen_settings";
    };

    public DataConverterTypes() {}
}
