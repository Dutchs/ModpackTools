package com.dutchs.modpacktools.network;

import com.dutchs.modpacktools.ConfigHandler;
import com.dutchs.modpacktools.Constants;
import com.dutchs.modpacktools.ModpackTools;
import com.dutchs.modpacktools.util.CommandUtil;
import com.dutchs.modpacktools.util.ComponentUtil;
import com.google.common.collect.Maps;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
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
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class EntityPacket implements NetworkManager.INetworkPacket {
    private String entityType;
    private String dimension;
    private int entityLimit;

    public EntityPacket() {
    }

    public EntityPacket(String pType, String dim, int limit) {
        entityType = pType;
        dimension = dim;
        entityLimit = limit;
    }

    @Override
    public void encode(Object msg, FriendlyByteBuf packetBuffer) {
        EntityPacket blockPacket = (EntityPacket) msg;
        packetBuffer.writeUtf(blockPacket.entityType);
        packetBuffer.writeUtf(blockPacket.dimension);
        packetBuffer.writeInt(blockPacket.entityLimit);
    }

    @Override
    public <MESSAGE> MESSAGE decode(FriendlyByteBuf packetBuffer) {
        EntityPacket result = new EntityPacket();
        result.entityType = packetBuffer.readUtf();
        result.dimension = packetBuffer.readUtf();
        result.entityLimit = packetBuffer.readInt();
        return (MESSAGE) result;
    }

    @Override
    public void handle(Object msg, Supplier<NetworkEvent.Context> contextSupplier) {
        contextSupplier.get().enqueueWork(() -> {
            int lastStage = 0;

            ServerPlayer p = contextSupplier.get().getSender();
            if (p == null) {
                lastStage = 1;
            } else {
                if (!p.hasPermissions(2)) {
                    lastStage = 3;
                    p.sendSystemMessage(Component.literal("You lack permissions to run this command").withStyle(Constants.ERROR_FORMAT));
                } else {
                    EntityPacket entityPacket = (EntityPacket) msg;
                    ResourceLocation pType = null;
                    if (!StringUtil.isNullOrEmpty(entityPacket.entityType)) {
                        pType = new ResourceLocation(entityPacket.entityType);
                    }

                    ResourceKey<Level> dim;
                    if (!StringUtil.isNullOrEmpty(entityPacket.dimension)) {
                        dim = ResourceKey.create(Registry.DIMENSION_REGISTRY, new ResourceLocation(entityPacket.dimension));
                    } else {
                        dim = p.getLevel().dimension();
                    }
                    int limit = entityPacket.entityLimit;

                    Set<ResourceLocation> names;
                    String pTypePath = pType != null ? pType.getPath() : "";

                    if (pType != null) { //exact match
                        ResourceLocation finalPType = pType;
                        names = ForgeRegistries.ENTITY_TYPES.getKeys().stream().filter(n -> Objects.equals(n, finalPType)).collect(Collectors.toSet());
                        if (names.isEmpty()) //path match
                        {
                            names = ForgeRegistries.ENTITY_TYPES.getKeys().stream().filter(n -> Objects.equals(n.getPath(), finalPType.getPath())).collect(Collectors.toSet());
                        }
                    } else {
                        names = ForgeRegistries.ENTITY_TYPES.getKeys();
                    }

                    if (names.isEmpty()) {
                        lastStage = 5;
                        p.sendSystemMessage(Component.literal("Invalid EntityType: " + pType).withStyle(Constants.ERROR_FORMAT));
                    } else {
                        lastStage = 6;
                        MinecraftServer server = p.getServer();
                        if (server == null) {
                            p.sendSystemMessage(Component.literal("ServerPlayer.getServer() returned NULL").withStyle(Constants.ERROR_FORMAT));
                        } else {
                            ServerLevel world = server.getLevel(dim); //TODO: DimensionManager so we can hotload? DimensionManager.getWorld(sender.getServer(), dim, false, false);
                            if (world == null) {
                                lastStage = 8;
                                p.sendSystemMessage(Component.literal("Invalid dimension: " + dim.location()).withStyle(Constants.ERROR_FORMAT));
                            } else {
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
                                        ResourceLocation blockID = ForgeRegistries.BLOCKS.getKey(blockEntity.getBlockState().getBlock());//.getRegistryName();
                                        right.getB().add(blockID != null ? blockID.toString() : "null");
                                    }

                                    info.right.put(e.blockPosition(), right);
                                });

                                if (names.size() == 1) {
                                    ResourceLocation name = names.iterator().next();
                                    Pair<Integer, Map<BlockPos, Tuple<Integer, List<String>>>> info = list.get(name);
                                    if (info == null) {
                                        lastStage = 11;
                                        p.sendSystemMessage(Component.literal("Entity of type: " + name + " does not currently exist in: " + dim.location()).withStyle(Constants.ERROR_FORMAT));
                                    } else {
                                        lastStage = 12;
                                        List<Map.Entry<BlockPos, Tuple<Integer, List<String>>>> toSort = new ArrayList<>(info.getRight().entrySet());
                                        toSort.sort((a, b) -> {
                                            if (Objects.equals(a.getValue(), b.getValue()))
                                                return a.getKey().toString().compareTo(b.getKey().toString());
                                            else
                                                return b.getValue().getA() - a.getValue().getA();
                                        });

                                        MutableComponent header = Component.literal(String.format("Entity: %s Total: %d", name, info.getLeft())).withStyle(Constants.CHAT_FORMAT);
                                        if (limit > -1 && limit < toSort.size()) {
                                            header.append(Component.literal(" - ").withStyle(Constants.CHAT_FORMAT));
                                            header.append(ComponentUtil.withRunCommand(Component.literal("Show All").withStyle(Constants.RUN_COMMAND_FORMAT), String.format("/mt entity %s %s -1", pType, dim.location())));
                                        }
                                        p.sendSystemMessage(header);
                                        int i = 0;
                                        for (Map.Entry<BlockPos, Tuple<Integer, List<String>>> e : toSort) {
                                            if (limit > -1 && limit-- <= 0) break;

                                            ResourceKey<Level> senderDim = p.getLevel().dimension();
                                            String tpCommand = CommandUtil.createTPCommandBetweenDimensions("@p", senderDim, dim, e.getKey());

                                            MutableComponent entry = Component.literal(String.format(" %d: ", e.getValue().getA()));
                                            entry.append(ComponentUtil.withTeleportCommand(Component.literal(String.format("%d %d %d", e.getKey().getX(), e.getKey().getY(), e.getKey().getZ())).withStyle(Constants.RUN_COMMAND_FORMAT), tpCommand));
                                            if (e.getValue().getB().size() > 0)
                                                entry.append(Component.literal(String.format(" - %s", String.join(", ", e.getValue().getB()))).withStyle(Constants.CHAT_FORMAT));
                                            p.sendSystemMessage(entry);
                                        }
                                    }
                                } else {
                                    lastStage = 13;
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

                                    if (info.size() == 0) {
                                        p.sendSystemMessage(Component.literal("Entity of type: " + pTypePath + " does not currently exist in: " + dim.location()).withStyle(Constants.ERROR_FORMAT));
                                    } else {
                                        int count = info.stream().mapToInt(Pair::getRight).sum();
                                        p.sendSystemMessage(Component.literal("Total: " + count));
                                        for (Pair<ResourceLocation, Integer> i : info) {
                                            MutableComponent entry = Component.literal(String.format(" %d: ", i.getValue()));
                                            entry.append(ComponentUtil.withRunCommand(Component.literal(i.getKey().toString()).withStyle(Constants.RUN_COMMAND_FORMAT), "/mt entity " + i.getKey()));
                                            p.sendSystemMessage(entry);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            if(ConfigHandler.printDebug) {
                ModpackTools.logInfo("Last Point hit: " + lastStage);
            }
        });
        contextSupplier.get().setPacketHandled(true);
    }
}
