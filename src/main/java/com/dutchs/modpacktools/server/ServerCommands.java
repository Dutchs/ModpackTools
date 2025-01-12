package com.dutchs.modpacktools.server;

import com.dutchs.modpacktools.ConfigHandler;
import com.dutchs.modpacktools.Constants;
import com.dutchs.modpacktools.ModpackTools;
import com.dutchs.modpacktools.client.ClientCommands;
import com.dutchs.modpacktools.command.CustomDimensionArgument;
import com.dutchs.modpacktools.network.EntityPacket;
import com.dutchs.modpacktools.util.CommandUtil;
import com.dutchs.modpacktools.util.ComponentUtil;
import com.google.common.collect.Maps;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.StringUtil;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.event.RegisterClientCommandsEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

public class ServerCommands {
    @SubscribeEvent
    public static void registerCommandsEvent(RegisterCommandsEvent event) {
        LiteralCommandNode<CommandSourceStack> rootCommand = event.getDispatcher().register(
                Commands.literal("mt")
                        .then(Commands.literal("entity").then(Commands.argument("entitytype", ResourceLocationArgument.id())
                                        .suggests((ctx, builder) -> SharedSuggestionProvider.suggest(ForgeRegistries.ENTITY_TYPES.getKeys().stream().map(ResourceLocation::toString), builder))
                                        .then(Commands.argument("dim", CustomDimensionArgument.dimension())
                                                .executes(ctx -> ENTITY_Command(ctx.getSource().getPlayer(), ctx.getArgument("entitytype", ResourceLocation.class), CustomDimensionArgument.getDimension(ctx, "dim"), 10))
                                                .then(Commands.argument("limit", IntegerArgumentType.integer())
                                                        .executes(ctx -> ENTITY_Command(ctx.getSource().getPlayer(), ctx.getArgument("entitytype", ResourceLocation.class), CustomDimensionArgument.getDimension(ctx, "dim"), IntegerArgumentType.getInteger(ctx, "limit")))
                                                )
                                        )
                                        .executes(ctx -> ENTITY_Command(ctx.getSource().getPlayer(), ctx.getArgument("entitytype", ResourceLocation.class), null, 10)))
                                .executes(ctx -> ENTITY_Command(ctx.getSource().getPlayer(), null, null, 10))
                        )
        );
    }

    public static int ENTITY_Command(@Nullable ServerPlayer p, @Nullable ResourceLocation pType, @Nullable ResourceKey<Level> dim, int limit) {
        if(p == null){
            return 0;
        }

        if (!p.hasPermissions(2)) {
            p.sendSystemMessage(Component.literal("You lack permissions to run this command").withStyle(Constants.ERROR_FORMAT));
            return 0;
        }

        if (dim == null) {
            dim = p.level().dimension();
        }

        Set<ResourceLocation> names;
        String pTypePath = pType != null ? pType.getPath() : "";

        if (pType != null) { //exact match
            names = ForgeRegistries.ENTITY_TYPES.getKeys().stream().filter(n -> Objects.equals(n, pType)).collect(Collectors.toSet());
            if (names.isEmpty()) //path match
            {
                names = ForgeRegistries.ENTITY_TYPES.getKeys().stream().filter(n -> Objects.equals(n.getPath(), pType.getPath())).collect(Collectors.toSet());
            }
        } else {
            names = ForgeRegistries.ENTITY_TYPES.getKeys();
        }

        if (names.isEmpty()) {
            p.sendSystemMessage(Component.literal("Invalid EntityType: " + pType).withStyle(Constants.ERROR_FORMAT));
            return 0;
        }

        MinecraftServer server = p.getServer();
        if (server != null) {
            ServerLevel world = server.getLevel(dim); //TODO: DimensionManager so we can hotload? DimensionManager.getWorld(sender.getServer(), dim, false, false);
            if (world == null) {
                p.sendSystemMessage(Component.literal("Invalid dimension: " + dim.location()).withStyle(Constants.ERROR_FORMAT));
                return 0;
            }

            Map<ResourceLocation, MutablePair<Integer, Map<BlockPos, Tuple<Integer, List<String>>>>> list = Maps.newHashMap();
            world.getEntities().getAll().forEach(e -> {
                MutablePair<Integer, Map<BlockPos, Tuple<Integer, List<String>>>> info = list.computeIfAbsent(ForgeRegistries.ENTITY_TYPES.getKey(e.getType()), k -> MutablePair.of(0, Maps.newHashMap()));
                info.left++;
                Tuple<Integer, List<String>> right = info.right.getOrDefault(e.blockPosition(), new Tuple<>(0, new ArrayList<>()));
                right.setA(right.getA() + 1);
                if (e instanceof ItemEntity itemEntity) {
                    ResourceLocation itemID = ForgeRegistries.ITEMS.getKey(itemEntity.getItem().getItem());
                    right.getB().add(itemID != null ? itemID.toString() : "null");
                } else if (e instanceof FallingBlockEntity blockEntity) {
                    ResourceLocation blockID = ForgeRegistries.BLOCKS.getKey( blockEntity.getBlockState().getBlock());
                    right.getB().add(blockID != null ? blockID.toString() : "null");
                }

                info.right.put(e.blockPosition(), right);
            });

            if (names.size() == 1) {
                ResourceLocation name = names.iterator().next();

                Pair<Integer, Map<BlockPos, Tuple<Integer, List<String>>>> info = list.get(name);
                if (info == null) {
                    p.sendSystemMessage(Component.literal("Entity of type: " + name + " does not currently exist in: " + dim.location()).withStyle(Constants.ERROR_FORMAT));
                    return 0;
                }

                List<Map.Entry<BlockPos, Tuple<Integer, List<String>>>> toSort = new ArrayList<>(info.getRight().entrySet());
                toSort.sort((a, b) -> {
                    if (Objects.equals(a.getValue(), b.getValue()))
                        return a.getKey().toString().compareTo(b.getKey().toString());
                    else
                        return b.getValue().getA() - a.getValue().getA();
                });

                MutableComponent header = Component.literal(String.format("Entity: %s Total: %d", name, info.getLeft())).withStyle(Constants.CHAT_FORMAT);
                if (limit != -1 && limit < toSort.size()) {
                    header.append(Component.literal(" - ").withStyle(Constants.CHAT_FORMAT));
                    header.append(ComponentUtil.withRunCommand(Component.literal("Show All").withStyle(Constants.RUN_COMMAND_FORMAT), String.format("/mt entity %s %s -1", pType, dim.location())));
                }
                p.sendSystemMessage(header);

                int i = 0;
                for (Map.Entry<BlockPos, Tuple<Integer, List<String>>> e : toSort) {
                    if (limit != -1 && limit-- == 0) break;

                    ResourceKey<Level> senderDim = p.level().dimension();
                    String tpCommand = CommandUtil.createTPCommand("@p", senderDim, dim, e.getKey());

                    MutableComponent entry = Component.literal(String.format(" %d: ", e.getValue().getA()));
                    entry.append(ComponentUtil.withTeleportCommand(Component.literal(String.format("%d %d %d", e.getKey().getX(), e.getKey().getY(), e.getKey().getZ())).withStyle(Constants.RUN_COMMAND_FORMAT), tpCommand));
                    if (!e.getValue().getB().isEmpty())
                        entry.append(Component.literal(String.format(" - %s", String.join(", ", e.getValue().getB()))).withStyle(Constants.CHAT_FORMAT));
                    p.sendSystemMessage(entry);
                }
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

                if (info.isEmpty()) {
                    p.sendSystemMessage(Component.literal("Entity of type: " + pTypePath + " does not currently exist in: " + dim.location()).withStyle(Constants.ERROR_FORMAT));
                    return 0;
                }

                int count = info.stream().mapToInt(Pair::getRight).sum();
                p.sendSystemMessage(Component.literal("Total: " + count));
                for (Pair<ResourceLocation, Integer> i : info) {
                    MutableComponent entry = Component.literal(String.format(" %d: ", i.getValue()));
                    entry.append(ComponentUtil.withRunCommand(Component.literal(i.getKey().toString()).withStyle(Constants.RUN_COMMAND_FORMAT), "/mt entity " + i.getKey()));
                    p.sendSystemMessage(entry);
                }
            }
        }
        return 0;
    }
}
