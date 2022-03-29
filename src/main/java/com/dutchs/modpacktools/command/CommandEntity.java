package com.dutchs.modpacktools.command;

import com.dutchs.modpacktools.Constants;
import com.dutchs.modpacktools.util.CommandUtil;
import com.dutchs.modpacktools.util.ComponentUtil;
import com.google.common.collect.Maps;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.DimensionArgument;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;
import java.util.stream.Collectors;

public class CommandEntity {
    private static final DynamicCommandExceptionType INVALID_ENTITY_TYPE = new DynamicCommandExceptionType(entity -> new TextComponent("Invalid EntityType: " + entity));
    private static final DynamicCommandExceptionType INVALID_DIMENSION = new DynamicCommandExceptionType(dim -> new TextComponent("Invalid dimension: " + dim));
    private static final Dynamic2CommandExceptionType NO_ENTITIES = new Dynamic2CommandExceptionType((entity, dim) -> new TextComponent("Entity of type: " + entity + " does not currently exist in: " + dim));

    public static ArgumentBuilder<CommandSourceStack, ?> register(CommandDispatcher<CommandSourceStack> dispatcher) {
        return Commands.literal("entity")
                .requires(cs -> cs.hasPermission(2))
                .then(Commands.argument("entitytype", ResourceLocationArgument.id())
                        .suggests((ctx, builder) -> SharedSuggestionProvider.suggest(ForgeRegistries.ENTITIES.getKeys().stream().map(ResourceLocation::toString), builder))
                        .then(Commands.argument("dim", DimensionArgument.dimension())
                                .executes(ctx -> run(ctx.getSource(), ctx.getArgument("entitytype", ResourceLocation.class), DimensionArgument.getDimension(ctx, "dim").dimension(), 10))
                                .then(Commands.argument("limit", IntegerArgumentType.integer())
                                        .executes(ctx -> run(ctx.getSource(), ctx.getArgument("entitytype", ResourceLocation.class), DimensionArgument.getDimension(ctx, "dim").dimension(), IntegerArgumentType.getInteger(ctx, "limit")))))
                        .executes(ctx -> run(ctx.getSource(), ctx.getArgument("entitytype", ResourceLocation.class), ctx.getSource().getLevel().dimension(), 10)))

                .executes(ctx -> run(ctx.getSource(), null, ctx.getSource().getLevel().dimension(), 10));
    }

    public static int run(CommandSourceStack sender, ResourceLocation pType, ResourceKey<Level> dim, int limit) throws CommandSyntaxException {
        Set<ResourceLocation> names;
        String pTypePath = pType != null ? pType.getPath() : "";

        if (pType != null) { //exact match
            names = ForgeRegistries.ENTITIES.getKeys().stream().filter(n -> Objects.equals(n, pType)).collect(Collectors.toSet());
            if (names.isEmpty()) //path match
                names = ForgeRegistries.ENTITIES.getKeys().stream().filter(n -> Objects.equals(n.getPath(), pType.getPath())).collect(Collectors.toSet());
            //TODO: namespace match?
        } else {
            names = ForgeRegistries.ENTITIES.getKeys();
        }

        if (names.isEmpty())
            throw INVALID_ENTITY_TYPE.create(pType);

        ServerLevel world = sender.getServer().getLevel(dim); //TODO: DimensionManager so we can hotload? DimensionManager.getWorld(sender.getServer(), dim, false, false);
        if (world == null)
            throw INVALID_DIMENSION.create(dim.location());

        Map<ResourceLocation, MutablePair<Integer, Map<BlockPos, Tuple<Integer, List<String>>>>> list = Maps.newHashMap();
        world.getEntities().getAll().forEach(e -> {
            MutablePair<Integer, Map<BlockPos, Tuple<Integer, List<String>>>> info = list.computeIfAbsent(e.getType().getRegistryName(), k -> MutablePair.of(0, Maps.newHashMap()));
            info.left++;
            Tuple<Integer, List<String>> right = info.right.getOrDefault(e.blockPosition(), new Tuple<>(0, new ArrayList<>()));
            right.setA(right.getA() + 1);
            if (e instanceof ItemEntity item) {
                ResourceLocation itemID = item.getItem().getItem().getRegistryName();
                right.getB().add(itemID != null ? itemID.toString() : "null");
            }
            info.right.put(e.blockPosition(), right);
        });

        if (names.size() == 1) {
            ResourceLocation name = names.iterator().next();

            Pair<Integer, Map<BlockPos, Tuple<Integer, List<String>>>> info = list.get(name);
            if (info == null)
                throw NO_ENTITIES.create(name, dim.location());

            List<Map.Entry<BlockPos, Tuple<Integer, List<String>>>> toSort = new ArrayList<>(info.getRight().entrySet());
            toSort.sort((a, b) -> {
                if (Objects.equals(a.getValue(), b.getValue()))
                    return a.getKey().toString().compareTo(b.getKey().toString());
                else
                    return b.getValue().getA() - a.getValue().getA();
            });

            MutableComponent header = new TextComponent(String.format("Entity: %s Total: %d", name, info.getLeft())).withStyle(Constants.CHAT_FORMAT);
            if (limit != -1 && limit < toSort.size()) {
                header.append(new TextComponent(" - ").withStyle(Constants.CHAT_FORMAT));
                header.append(ComponentUtil.withRunCommand(new TextComponent("Show All").withStyle(Constants.RUN_COMMAND_FORMAT), String.format("/mt entity %s %s -1", pType, dim.location() )));
            }
            sender.sendSuccess(header, false);

            int i = 0;
            for (Map.Entry<BlockPos, Tuple<Integer, List<String>>> e : toSort) {
                if (limit != -1 && limit-- == 0) break;

                ResourceKey<Level> senderDim = sender.getLevel().dimension();
                String tpCommand = CommandUtil.createTPCommandBetweenDimensions("@p", senderDim, dim, e.getKey());

                MutableComponent entry = new TextComponent(String.format(" %d: ", e.getValue().getA()));
                entry.append(ComponentUtil.withSuggestCommand(new TextComponent(String.format("%d %d %d", e.getKey().getX(), e.getKey().getY(), e.getKey().getZ())).withStyle(Constants.SUGGEST_COMMAND_FORMAT), tpCommand));
                if(e.getValue().getB().size() > 0)
                    entry.append(new TextComponent(String.format(" - %s", String.join(", ", e.getValue().getB()))).withStyle(Constants.CHAT_FORMAT));
                sender.sendSuccess(entry, false);
            }
            return toSort.size();
        } else {
            List<Pair<ResourceLocation, Integer>> info = new ArrayList<>();
            Set<ResourceLocation> finalNames = names;
            list.forEach((key, value) -> {
                if (finalNames.contains(key)) {
                    Pair<ResourceLocation, Integer> of = Pair.of(key, value.left);
                    info.add(of);
                }
            });
            info.sort((a, b) -> {
                if (Objects.equals(a.getRight(), b.getRight()))
                    return a.getKey().toString().compareTo(b.getKey().toString());
                else
                    return b.getRight() - a.getRight();
            });

            if (info.size() == 0)
                throw NO_ENTITIES.create(pTypePath, dim.location());

            int count = info.stream().mapToInt(Pair::getRight).sum();
            sender.sendSuccess(new TextComponent(String.format("Total: %d", count)), false);
            for (Pair<ResourceLocation, Integer> i : info) {
                MutableComponent entry = new TextComponent(String.format(" %d: ", i.getValue()));
                entry.append(ComponentUtil.withSuggestCommand(new TextComponent(i.getKey().toString()).withStyle(Constants.SUGGEST_COMMAND_FORMAT), "/mt entity " + i.getKey()));
                sender.sendSuccess(entry, false);
            }
            return info.size();
        }
    }
}