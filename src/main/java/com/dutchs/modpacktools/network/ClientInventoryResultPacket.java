package com.dutchs.modpacktools.network;

import com.dutchs.modpacktools.ConfigHandler;
import com.dutchs.modpacktools.util.ComponentUtil;
import com.dutchs.modpacktools.util.PlayerUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.StringUtil;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public class ClientInventoryResultPacket implements NetworkManager.INetworkPacket {
    private InventoryPacket.InventoryType inventoryType;
    private String inventoryItems;

    public ClientInventoryResultPacket() {
    }

    public ClientInventoryResultPacket(@NotNull InventoryPacket.InventoryType type, @NotNull String items) {
        inventoryType = type;
        inventoryItems = items;
    }

    @Override
    public void encode(Object msg, FriendlyByteBuf packetBuffer) {
        ClientInventoryResultPacket blockPacket = (ClientInventoryResultPacket) msg;
        packetBuffer.writeEnum(blockPacket.inventoryType);
        packetBuffer.writeUtf(blockPacket.inventoryItems);
    }

    @Override
    public <MESSAGE> MESSAGE decode(FriendlyByteBuf packetBuffer) {
        ClientInventoryResultPacket result = new ClientInventoryResultPacket();
        result.inventoryType = packetBuffer.readEnum(InventoryPacket.InventoryType.class);
        result.inventoryItems = packetBuffer.readUtf();
        return (MESSAGE) result;
    }

    @Override
    public void handle(Object msg, Supplier<NetworkEvent.Context> contextSupplier) {
        contextSupplier.get().enqueueWork(() -> {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> handleClient((ClientInventoryResultPacket) msg, contextSupplier));
        });
        contextSupplier.get().setPacketHandled(true);
    }

    private void handleClient(ClientInventoryResultPacket msg, Supplier<NetworkEvent.Context> contextSupplier) {
        if (!StringUtil.isNullOrEmpty(msg.inventoryItems)) {
            String newLinesItems = msg.inventoryItems.replace("\n", System.lineSeparator());

            if(ConfigHandler.autoCopyItems) {
                Minecraft.getInstance().keyboardHandler.setClipboard(newLinesItems);
            }
            PlayerUtil.sendClientMessage(ComponentUtil.formatTitleContentWithCopy(msg.inventoryType.toString(), msg.inventoryItems, newLinesItems));
        } else {
            PlayerUtil.sendClientMessage(ComponentUtil.formatTitleContent(msg.inventoryType.toString(), "Nothing to print"));
        }
    }
}
