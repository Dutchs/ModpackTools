package com.dutchs.modpacktools.network;

import com.dutchs.modpacktools.Constants;
import com.dutchs.modpacktools.server.ServerCommands;
import com.dutchs.modpacktools.util.CommandUtil;
import com.dutchs.modpacktools.util.ComponentUtil;
import com.google.common.collect.Maps;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
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
            ServerPlayer p = contextSupplier.get().getSender();
            if(p != null){
                EntityPacket entityPacket = (EntityPacket) msg;
                ResourceLocation pType = null;
                if (!StringUtil.isNullOrEmpty(entityPacket.entityType)) {
                    pType = new ResourceLocation(entityPacket.entityType);
                }

                ResourceKey<Level> dim;
                if (!StringUtil.isNullOrEmpty(entityPacket.dimension)) {
                    dim = ResourceKey.create(Registries.DIMENSION, new ResourceLocation(entityPacket.dimension));
                } else {
                    dim = p.level().dimension();
                }

                int limit = entityPacket.entityLimit;

                ServerCommands.ENTITY_Command(p, pType, dim, limit);
            }
        });
        contextSupplier.get().setPacketHandled(true);
    }
}
