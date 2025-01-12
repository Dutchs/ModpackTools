package com.dutchs.modpacktools.network;

import com.dutchs.modpacktools.Constants;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.network.CustomPayloadEvent;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public class PrivilegedMessagePacket implements NetworkManager.INetworkPacket {
    private String message;

    public PrivilegedMessagePacket() {
    }

    public PrivilegedMessagePacket(@NotNull String msg) {
        message = msg;
    }

    @Override
    public void encode(Object msg, FriendlyByteBuf packetBuffer) {
        PrivilegedMessagePacket blockPacket = (PrivilegedMessagePacket) msg;
        packetBuffer.writeUtf(blockPacket.message);
    }

    @Override
    public <MESSAGE> MESSAGE decode(FriendlyByteBuf packetBuffer) {
        PrivilegedMessagePacket result = new PrivilegedMessagePacket();
        result.message = packetBuffer.readUtf();
        return (MESSAGE) result;
    }

    @Override
    public void handle(Object msg, CustomPayloadEvent.Context context) {
        context.enqueueWork(() -> {
            ServerPlayer p = context.getSender();
            if (p != null) {
                if (p.hasPermissions(2)) {
                    PrivilegedMessagePacket privilegedMessagePacket = (PrivilegedMessagePacket) msg;
                    p.sendSystemMessage(Component.literal(privilegedMessagePacket.message).withStyle(Constants.ERROR_FORMAT));
                } else {
                    p.sendSystemMessage(Component.literal("You lack permissions to run this command").withStyle(Constants.ERROR_FORMAT));
                }
            }
        });
        context.setPacketHandled(true);
    }
}
