package com.dutchs.modpacktools.network;

import com.dutchs.modpacktools.Constants;
import com.dutchs.modpacktools.ModpackTools;
import com.dutchs.modpacktools.network.NetworkManager.INetworkPacket;
import com.dutchs.modpacktools.util.ItemStackUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public class InventoryPacket implements INetworkPacket {
    public enum InventoryType {
        Hand,
        Hotbar,
        Inventory,
        BlockInventory
    }

    private InventoryType inventoryType;
    private boolean includeNBT;

    public InventoryPacket() {
    }

    public InventoryPacket(@NotNull InventoryType type, boolean nbt) {
        inventoryType = type;
        includeNBT = nbt;
    }

    @Override
    public void encode(Object msg, FriendlyByteBuf packetBuffer) {
        InventoryPacket blockPacket = (InventoryPacket) msg;
        packetBuffer.writeEnum(blockPacket.inventoryType);
        packetBuffer.writeBoolean(blockPacket.includeNBT);
    }

    @Override
    public <MESSAGE> MESSAGE decode(FriendlyByteBuf packetBuffer) {
        InventoryPacket result = new InventoryPacket();
        result.inventoryType = packetBuffer.readEnum(InventoryType.class);
        result.includeNBT = packetBuffer.readBoolean();
        return (MESSAGE) result;
    }

    @Override
    public void handle(Object msg, Supplier<NetworkEvent.Context> contextSupplier) {
        contextSupplier.get().enqueueWork(() -> {
            ServerPlayer p = contextSupplier.get().getSender();
            if (p != null) {
                if(p.hasPermissions( 2)) {
                    InventoryPacket inventoryPacket = (InventoryPacket) msg;
                    InventoryType type = inventoryPacket.inventoryType;
                    boolean nbt = inventoryPacket.includeNBT;

                    String itemStacks = null;
                    if (type == InventoryType.Hand) {
                        itemStacks = ItemStackUtil.ItemStackPrinter(p.getMainHandItem(), nbt, false);
                    } else if (type == InventoryType.Hotbar) {
                        itemStacks = ItemStackUtil.ItemStackPrinter(p.getInventory().items.subList(0, 9), nbt, false);
                    } else if (type == InventoryType.Inventory) {
                        itemStacks = ItemStackUtil.ItemStackPrinter(p.getInventory().items.subList(9, 36), nbt, false);
                    }

                    ClientInventoryResultPacket result = new ClientInventoryResultPacket(type, itemStacks == null ? "" : itemStacks);
                    ModpackTools.NETWORK.toPlayer(result, p);
                } else {
                    p.sendMessage(new TextComponent("You lack permissions to run this command").withStyle(Constants.ERROR_FORMAT), Constants.MOD_SENDER_UUID);
                }
            }
        });
        contextSupplier.get().setPacketHandled(true);
    }
}
