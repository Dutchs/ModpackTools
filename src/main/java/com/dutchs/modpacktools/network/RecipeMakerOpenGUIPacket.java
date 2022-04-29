package com.dutchs.modpacktools.network;

import com.dutchs.modpacktools.Constants;
import com.dutchs.modpacktools.gui.RecipeMakerMenuProvider;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkHooks;

import java.util.function.Supplier;

public class RecipeMakerOpenGUIPacket implements NetworkManager.INetworkPacket {

    public RecipeMakerOpenGUIPacket() {
    }

    @Override
    public void encode(Object msg, FriendlyByteBuf packetBuffer) {
        RecipeMakerOpenGUIPacket openGUIPacket = (RecipeMakerOpenGUIPacket) msg;
    }

    @Override
    public <MESSAGE> MESSAGE decode(FriendlyByteBuf packetBuffer) {
        RecipeMakerOpenGUIPacket result = new RecipeMakerOpenGUIPacket();
        return (MESSAGE) result;
    }

    @Override
    public void handle(Object msg, Supplier<NetworkEvent.Context> contextSupplier) {
        contextSupplier.get().enqueueWork(() -> {
            ServerPlayer p = contextSupplier.get().getSender();
            if (p != null) {
                if (p.hasPermissions(2)) {
                    //RecipeMakerOpenGUIPacket openGUIPacket = (RecipeMakerOpenGUIPacket) msg;

                    //I _probably_ don't need to handle this.
                    //if (p.containerMenu instanceof RecipeMakerMenu) {
                        //p.closeContainer();
                    //} else {
                    NetworkHooks.openGui(p, new RecipeMakerMenuProvider(), p.blockPosition());
                    //}
                } else {
                    p.sendMessage(new TextComponent("You lack permissions to run this command").withStyle(Constants.ERROR_FORMAT), Constants.MOD_SENDER_UUID);
                }
            }
        });
        contextSupplier.get().setPacketHandled(true);
    }
}
