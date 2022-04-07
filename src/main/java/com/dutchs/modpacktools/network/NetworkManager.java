package com.dutchs.modpacktools.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.function.Supplier;

public class NetworkManager {

    private final SimpleChannel channel;
    private final HashSet<Class<? extends INetworkPacket>> packets;
    private int packetIndex = 0;

    public NetworkManager(String channelName) {
        channel = NetworkRegistry.ChannelBuilder.named(new ResourceLocation(channelName)).clientAcceptedVersions(a -> true).serverAcceptedVersions(a -> true).networkProtocolVersion(() -> "1.0.0").simpleChannel();
        packets = new HashSet<>();
    }

    @SafeVarargs
    public final void registerPackets(Class<? extends INetworkPacket>... handledPacketClasses) {
        for (Class<? extends INetworkPacket> packetClass : handledPacketClasses) {
            try {
                INetworkPacket instance = packetClass.getDeclaredConstructor().newInstance();
                channel.registerMessage(packetIndex++, packetClass, instance::encode, instance::decode, instance::handle);
                packets.add(packetClass);
            } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
                e.printStackTrace();
            }
        }
    }

    public void toServer(INetworkPacket packet) {
        validatePacketClass(packet.getClass());
        channel.sendToServer(packet);
    }

    public void toPlayer(INetworkPacket packet, ServerPlayer player) {
        validatePacketClass(packet.getClass());
        channel.send(PacketDistributor.PLAYER.with(() -> player), packet);
    }

    public void toAllPlayers(INetworkPacket packet) {
        validatePacketClass(packet.getClass());
        channel.send(PacketDistributor.ALL.noArg(), packet);
    }

    public void toAllAround(INetworkPacket packet, PacketDistributor.TargetPoint tp) {
        validatePacketClass(packet.getClass());
        channel.send(PacketDistributor.NEAR.with(() -> tp), packet);
    }

    public void toAllInDimension(INetworkPacket packet, ResourceKey<Level> dimension) {
        validatePacketClass(packet.getClass());
        channel.send(PacketDistributor.DIMENSION.with(() -> dimension), packet);
    }

    private void validatePacketClass(Class<? extends INetworkPacket> clazz) {
        if (!packets.contains(clazz)) {
            throw new RuntimeException("Invalid Packet: " + clazz);
        }
    }

    public interface INetworkPacket {

        void encode(Object msg, FriendlyByteBuf packetBuffer);

        <MESSAGE> MESSAGE decode(FriendlyByteBuf packetBuffer);

        void handle(Object msg, Supplier<NetworkEvent.Context> contextSupplier);
    }

}