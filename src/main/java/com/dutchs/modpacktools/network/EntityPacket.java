package com.dutchs.modpacktools.network;

import com.dutchs.modpacktools.Constants;
import com.dutchs.modpacktools.util.CommandUtil;
import com.dutchs.modpacktools.util.ComponentUtil;
import com.google.common.collect.Maps;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
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
            ServerPlayer p = contextSupplier.get().getSender();
            if (p != null) {
                if (!p.hasPermissions(2)) {
                    p.sendMessage(new TextComponent("You lack permissions to run this command").withStyle(Constants.ERROR_FORMAT), Constants.MOD_SENDER_UUID);
                    return;
                }

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
                    names = ForgeRegistries.ENTITIES.getKeys().stream().filter(n -> Objects.equals(n, finalPType)).collect(Collectors.toSet());
                    if (names.isEmpty()) //path match
                    {
                        names = ForgeRegistries.ENTITIES.getKeys().stream().filter(n -> Objects.equals(n.getPath(), finalPType.getPath())).collect(Collectors.toSet());
                    }
                } else {
                    names = ForgeRegistries.ENTITIES.getKeys();
                }

                if (names.isEmpty()) {
                    p.sendMessage(new TextComponent("Invalid EntityType: " + pType).withStyle(Constants.ERROR_FORMAT), Constants.MOD_SENDER_UUID);
                    return;
                }

                MinecraftServer server = p.getServer();
                if (server != null) {
                    ServerLevel world = server.getLevel(dim); //TODO: DimensionManager so we can hotload? DimensionManager.getWorld(sender.getServer(), dim, false, false);
                    if (world == null) {
                        p.sendMessage(new TextComponent("Invalid dimension: " + dim.location()).withStyle(Constants.ERROR_FORMAT), Constants.MOD_SENDER_UUID);
                        return;
                    }

                    Map<ResourceLocation, MutablePair<Integer, Map<BlockPos, Tuple<Integer, List<String>>>>> list = Maps.newHashMap();
                    world.getEntities().getAll().forEach(e -> {
                        MutablePair<Integer, Map<BlockPos, Tuple<Integer, List<String>>>> info = list.computeIfAbsent(e.getType().getRegistryName(), k -> MutablePair.of(0, Maps.newHashMap()));
                        info.left++;
                        Tuple<Integer, List<String>> right = info.right.getOrDefault(e.blockPosition(), new Tuple<>(0, new ArrayList<>()));
                        right.setA(right.getA() + 1);
                        if (e instanceof ItemEntity itemEntity) {
                            ResourceLocation itemID = itemEntity.getItem().getItem().getRegistryName();
                            right.getB().add(itemID != null ? itemID.toString() : "null");
                        } else if (e instanceof FallingBlockEntity blockEntity) {
                            ResourceLocation blockID = blockEntity.getBlockState().getBlock().getRegistryName();
                            right.getB().add(blockID != null ? blockID.toString() : "null");
                        }

                        info.right.put(e.blockPosition(), right);
                    });

                    if (names.size() == 1) {
                        ResourceLocation name = names.iterator().next();

                        Pair<Integer, Map<BlockPos, Tuple<Integer, List<String>>>> info = list.get(name);
                        if (info == null) {
                            p.sendMessage(new TextComponent("Entity of type: " + name + " does not currently exist in: " + dim.location()).withStyle(Constants.ERROR_FORMAT), Constants.MOD_SENDER_UUID);
                            return;
                        }

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
                            header.append(ComponentUtil.withRunCommand(new TextComponent("Show All").withStyle(Constants.RUN_COMMAND_FORMAT), String.format("/mt entity %s %s -1", pType, dim.location())));
                        }
                        p.sendMessage(header, Constants.MOD_SENDER_UUID);

                        int i = 0;
                        for (Map.Entry<BlockPos, Tuple<Integer, List<String>>> e : toSort) {
                            if (limit != -1 && limit-- == 0) break;

                            ResourceKey<Level> senderDim = p.getLevel().dimension();
                            String tpCommand = CommandUtil.createTPCommandBetweenDimensions("@p", senderDim, dim, e.getKey());

                            MutableComponent entry = new TextComponent(String.format(" %d: ", e.getValue().getA()));
                            entry.append(ComponentUtil.withTeleportCommand(new TextComponent(String.format("%d %d %d", e.getKey().getX(), e.getKey().getY(), e.getKey().getZ())).withStyle(Constants.RUN_COMMAND_FORMAT), tpCommand));
                            if (e.getValue().getB().size() > 0)
                                entry.append(new TextComponent(String.format(" - %s", String.join(", ", e.getValue().getB()))).withStyle(Constants.CHAT_FORMAT));
                            p.sendMessage(entry, Constants.MOD_SENDER_UUID);
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

                        if (info.size() == 0) {
                            p.sendMessage(new TextComponent("Entity of type: " + pTypePath + " does not currently exist in: " + dim.location()).withStyle(Constants.ERROR_FORMAT), Constants.MOD_SENDER_UUID);
                            return;
                        }

                        int count = info.stream().mapToInt(Pair::getRight).sum();
                        p.sendMessage(new TextComponent("Total: " + count), Constants.MOD_SENDER_UUID);
                        for (Pair<ResourceLocation, Integer> i : info) {
                            MutableComponent entry = new TextComponent(String.format(" %d: ", i.getValue()));
                            entry.append(ComponentUtil.withRunCommand(new TextComponent(i.getKey().toString()).withStyle(Constants.RUN_COMMAND_FORMAT), "/mt entity " + i.getKey()));
                            p.sendMessage(entry, Constants.MOD_SENDER_UUID);
                        }
                    }
                }
            }
        });
        contextSupplier.get().setPacketHandled(true);
    }
}
