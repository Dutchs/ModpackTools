package com.dutchs.modpacktools.command;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

public class CustomDimensionArgument implements ArgumentType<ResourceLocation> {
    private static final Collection<String> EXAMPLES;

    public CustomDimensionArgument() {
    }

    public ResourceLocation parse(StringReader pReader) throws CommandSyntaxException {
        return ResourceLocation.read(pReader);
    }

    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> pContext, SuggestionsBuilder pBuilder) {
        return pContext.getSource() instanceof SharedSuggestionProvider ? SharedSuggestionProvider.suggestResource(((SharedSuggestionProvider)pContext.getSource()).levels().stream().map(ResourceKey::location), pBuilder) : Suggestions.empty();
    }

    public Collection<String> getExamples() {
        return EXAMPLES;
    }

    public static net.minecraft.commands.arguments.DimensionArgument dimension() {
        return new net.minecraft.commands.arguments.DimensionArgument();
    }

    public static ResourceKey<Level> getDimension(CommandContext<CommandSourceStack> pContext, String pName) throws CommandSyntaxException {
        ResourceLocation resourceLocation = (ResourceLocation)pContext.getArgument(pName, ResourceLocation.class);
        return ResourceKey.create(Registries.DIMENSION, resourceLocation);
    }

    static {
        EXAMPLES = (Collection) Stream.of(Level.OVERWORLD, Level.NETHER).map((p_88814_) -> p_88814_.location().toString()).collect(Collectors.toList());
        //ERROR_INVALID_VALUE = new DynamicCommandExceptionType((p_88812_) -> Component.translatable("argument.dimension.invalid", new Object[]{p_88812_}));
    }
}
