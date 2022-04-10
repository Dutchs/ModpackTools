package com.dutchs.modpacktools.command;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DimensionResourceArgument implements ArgumentType<ResourceLocation> {
    private static final Collection<String> EXAMPLES = Stream.of(Level.OVERWORLD, Level.NETHER, Level.END).map((p_88814_) -> p_88814_.location().toString()).collect(Collectors.toList());

    public ResourceLocation parse(StringReader p_88807_) throws CommandSyntaxException {
        return ResourceLocation.read(p_88807_);
    }

    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> pContext, SuggestionsBuilder pBuilder) {
        return pContext.getSource() instanceof SharedSuggestionProvider ? SharedSuggestionProvider.suggestResource(((SharedSuggestionProvider)pContext.getSource()).levels().stream().map(ResourceKey::location), pBuilder) : Suggestions.empty();
    }

    public Collection<String> getExamples() {
        return EXAMPLES;
    }

    public static DimensionResourceArgument dimensionResource() {
        return new DimensionResourceArgument();
    }

    public static ResourceKey<Level> getDimensionResource(CommandContext<CommandSourceStack> pContext, String pName) {
        ResourceLocation resourcelocation = pContext.getArgument(pName, ResourceLocation.class);
        return ResourceKey.create(Registry.DIMENSION_REGISTRY, resourcelocation);
    }
}