package net.minecraft.commands.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import java.util.Arrays;
import java.util.Collection;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.commands.CommandListenerWrapper;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.world.item.crafting.CraftingManager;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.storage.loot.LootDataManager;
import net.minecraft.world.level.storage.loot.LootDataType;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class ArgumentMinecraftKeyRegistered implements ArgumentType<MinecraftKey> {

    private static final Collection<String> EXAMPLES = Arrays.asList("foo", "foo:bar", "012");
    private static final DynamicCommandExceptionType ERROR_UNKNOWN_ADVANCEMENT = new DynamicCommandExceptionType((object) -> {
        return IChatBaseComponent.translatableEscape("advancement.advancementNotFound", object);
    });
    private static final DynamicCommandExceptionType ERROR_UNKNOWN_RECIPE = new DynamicCommandExceptionType((object) -> {
        return IChatBaseComponent.translatableEscape("recipe.notFound", object);
    });
    private static final DynamicCommandExceptionType ERROR_UNKNOWN_PREDICATE = new DynamicCommandExceptionType((object) -> {
        return IChatBaseComponent.translatableEscape("predicate.unknown", object);
    });
    private static final DynamicCommandExceptionType ERROR_UNKNOWN_ITEM_MODIFIER = new DynamicCommandExceptionType((object) -> {
        return IChatBaseComponent.translatableEscape("item_modifier.unknown", object);
    });

    public ArgumentMinecraftKeyRegistered() {}

    public static ArgumentMinecraftKeyRegistered id() {
        return new ArgumentMinecraftKeyRegistered();
    }

    public static AdvancementHolder getAdvancement(CommandContext<CommandListenerWrapper> commandcontext, String s) throws CommandSyntaxException {
        MinecraftKey minecraftkey = getId(commandcontext, s);
        AdvancementHolder advancementholder = ((CommandListenerWrapper) commandcontext.getSource()).getServer().getAdvancements().get(minecraftkey);

        if (advancementholder == null) {
            throw ArgumentMinecraftKeyRegistered.ERROR_UNKNOWN_ADVANCEMENT.create(minecraftkey);
        } else {
            return advancementholder;
        }
    }

    public static RecipeHolder<?> getRecipe(CommandContext<CommandListenerWrapper> commandcontext, String s) throws CommandSyntaxException {
        CraftingManager craftingmanager = ((CommandListenerWrapper) commandcontext.getSource()).getServer().getRecipeManager();
        MinecraftKey minecraftkey = getId(commandcontext, s);

        return (RecipeHolder) craftingmanager.byKey(minecraftkey).orElseThrow(() -> {
            return ArgumentMinecraftKeyRegistered.ERROR_UNKNOWN_RECIPE.create(minecraftkey);
        });
    }

    public static LootItemCondition getPredicate(CommandContext<CommandListenerWrapper> commandcontext, String s) throws CommandSyntaxException {
        MinecraftKey minecraftkey = getId(commandcontext, s);
        LootDataManager lootdatamanager = ((CommandListenerWrapper) commandcontext.getSource()).getServer().getLootData();
        LootItemCondition lootitemcondition = (LootItemCondition) lootdatamanager.getElement(LootDataType.PREDICATE, minecraftkey);

        if (lootitemcondition == null) {
            throw ArgumentMinecraftKeyRegistered.ERROR_UNKNOWN_PREDICATE.create(minecraftkey);
        } else {
            return lootitemcondition;
        }
    }

    public static LootItemFunction getItemModifier(CommandContext<CommandListenerWrapper> commandcontext, String s) throws CommandSyntaxException {
        MinecraftKey minecraftkey = getId(commandcontext, s);
        LootDataManager lootdatamanager = ((CommandListenerWrapper) commandcontext.getSource()).getServer().getLootData();
        LootItemFunction lootitemfunction = (LootItemFunction) lootdatamanager.getElement(LootDataType.MODIFIER, minecraftkey);

        if (lootitemfunction == null) {
            throw ArgumentMinecraftKeyRegistered.ERROR_UNKNOWN_ITEM_MODIFIER.create(minecraftkey);
        } else {
            return lootitemfunction;
        }
    }

    public static MinecraftKey getId(CommandContext<CommandListenerWrapper> commandcontext, String s) {
        return (MinecraftKey) commandcontext.getArgument(s, MinecraftKey.class);
    }

    public MinecraftKey parse(StringReader stringreader) throws CommandSyntaxException {
        return MinecraftKey.read(stringreader);
    }

    public Collection<String> getExamples() {
        return ArgumentMinecraftKeyRegistered.EXAMPLES;
    }
}
