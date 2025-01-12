package com.dutchs.modpacktools.command;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;

public class TagKeyArgument implements ArgumentType<ResourceLocation> {
    private final TagType type;

    private TagKeyArgument(final TagType type) {
        this.type = type;
    }

    public static TagKeyArgument block() {
        return new TagKeyArgument(TagType.BLOCK);
    }

    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> pContext, SuggestionsBuilder pBuilder) {
        return pContext.getSource() instanceof SharedSuggestionProvider ? SharedSuggestionProvider.suggestResource(((SharedSuggestionProvider)pContext.getSource()).registryAccess().registry(Registries.BLOCK).get().getTags().map(tag -> tag.getFirst().location()), pBuilder) : Suggestions.empty();
    }

    public static ResourceLocation getLocation(final CommandContext<?> context, final String name) {
        return context.getArgument(name, ResourceLocation.class);
    }

    public TagType getType() {
        return type;
    }

    @Override
    public ResourceLocation parse(final StringReader reader) throws CommandSyntaxException {
        return ResourceLocation.read(reader);
    }

    @Override
    public String toString() {
        return "string()";
    }

    @Override
    public Collection<String> getExamples() {
        return type.getExamples();
    }

    public enum TagType {
        BLOCK("word", "words_with_underscores"),;

        private final Collection<String> examples;

        TagType(final String... examples) {
            this.examples = Arrays.asList(examples);
        }

        public Collection<String> getExamples() {
            return examples;
        }
    }
}
