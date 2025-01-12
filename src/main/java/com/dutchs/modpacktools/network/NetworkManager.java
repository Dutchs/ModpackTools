package com.dutchs.modpacktools.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;

import net.minecraftforge.event.network.CustomPayloadEvent;
import net.minecraftforge.network.*;

import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.function.Supplier;

public class NetworkManager {

    private final SimpleChannel channel;
    private final HashSet<Class<? extends INetworkPacket>> packets;
    private int packetIndex = 0;

    public NetworkManager(String channelName) {
        channel = ChannelBuilder.named(ResourceLocation.withDefaultNamespace(channelName)).clientAcceptedVersions((status, i) ->true).serverAcceptedVersions((status, i)->true).networkProtocolVersion(1).simpleChannel();
        packets = new HashSet<>();
    }

    @SafeVarargs
    public final void registerPackets(Class<? extends INetworkPacket>... handledPacketClasses) {
        for (Class<? extends INetworkPacket> packetClass : handledPacketClasses) {
            try {
                INetworkPacket instance = packetClass.getDeclaredConstructor().newInstance();
                channel.messageBuilder(packetClass).decoder(instance::decode).encoder(instance::encode).consumerNetworkThread(instance::handle).add();
                //channel.registerMessage(packetIndex++, packetClass, instance::encode, instance::decode, instance::handle);
                packets.add(packetClass);
            } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
                e.printStackTrace();
            }
        }
    }

    public void toServer(INetworkPacket packet) {
        validatePacketClass(packet.getClass());
//        channel.sendToServer(packet);
        channel.send(packet, PacketDistributor.SERVER.noArg());
    }

    public void toPlayer(INetworkPacket packet, ServerPlayer player) {
        validatePacketClass(packet.getClass());
        channel.send(packet, PacketDistributor.PLAYER.with(player));
    }

    public void toAllPlayers(INetworkPacket packet) {
        validatePacketClass(packet.getClass());
        channel.send(packet, PacketDistributor.ALL.noArg());
    }

    public void toAllAround(INetworkPacket packet, PacketDistributor.TargetPoint tp) {
        validatePacketClass(packet.getClass());
        channel.send(packet, PacketDistributor.NEAR.with(tp));
    }

    public void toAllInDimension(INetworkPacket packet, ResourceKey<Level> dimension) {
        validatePacketClass(packet.getClass());
        channel.send(packet, PacketDistributor.DIMENSION.with(dimension));
    }

    private void validatePacketClass(Class<? extends INetworkPacket> clazz) {
        if (!packets.contains(clazz)) {
            throw new RuntimeException("Invalid Packet: " + clazz);
        }
    }

    public interface INetworkPacket {

        void encode(Object msg, FriendlyByteBuf packetBuffer);

        <MESSAGE> MESSAGE decode(FriendlyByteBuf packetBuffer);
        void handle(Object msg, CustomPayloadEvent.Context context);
    }

}