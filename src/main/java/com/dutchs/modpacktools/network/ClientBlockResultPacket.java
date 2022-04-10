package com.dutchs.modpacktools.network;

import com.dutchs.modpacktools.network.NetworkManager.INetworkPacket;
import com.dutchs.modpacktools.util.BlockUtil;
import com.dutchs.modpacktools.util.ComponentUtil;
import com.dutchs.modpacktools.util.PlayerUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.StringUtil;
import net.minecraft.util.Tuple;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.function.Supplier;

public class ClientBlockResultPacket implements INetworkPacket {
    private String blockState;
    private BlockPos blockPos;
    private String blockType;
    private String blockClass;
    private CompoundTag blockNBT;

    public ClientBlockResultPacket() {
    }

    public ClientBlockResultPacket(@NotNull String state, @NotNull BlockPos pos, @NotNull String type, @NotNull String clazz, @NotNull CompoundTag nbt) {
        blockState = state;
        blockPos = pos;
        blockType = type;
        blockClass = clazz;
        blockNBT = nbt;
    }

    @Override
    public void encode(Object msg, FriendlyByteBuf packetBuffer) {
        ClientBlockResultPacket blockPacket = (ClientBlockResultPacket) msg;
        packetBuffer.writeUtf(blockPacket.blockState);
        packetBuffer.writeBlockPos(blockPacket.blockPos);
        packetBuffer.writeUtf(blockPacket.blockType);
        packetBuffer.writeUtf(blockPacket.blockClass);
        packetBuffer.writeNbt(blockPacket.blockNBT);
    }

    @Override
    public <MESSAGE> MESSAGE decode(FriendlyByteBuf packetBuffer) {
        ClientBlockResultPacket result = new ClientBlockResultPacket();
        result.blockState = packetBuffer.readUtf();
        result.blockPos = packetBuffer.readBlockPos();
        result.blockType = packetBuffer.readUtf();
        result.blockClass = packetBuffer.readUtf();
        result.blockNBT = packetBuffer.readNbt();
        return (MESSAGE) result;
    }

    @Override
    public void handle(Object msg, Supplier<NetworkEvent.Context> contextSupplier) {
        contextSupplier.get().enqueueWork(() -> {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> handleClient((ClientBlockResultPacket) msg, contextSupplier));
        });
        contextSupplier.get().setPacketHandled(true);
    }

    private void handleClient(ClientBlockResultPacket msg, Supplier<NetworkEvent.Context> contextSupplier) {
        ArrayList<Tuple<String, String>> keyValues = new ArrayList<>();
        keyValues.add(new Tuple<>("ID", msg.blockState));

        if (!StringUtil.isNullOrEmpty(msg.blockType))
            keyValues.add(new Tuple<>("Type", msg.blockType));

        if (!StringUtil.isNullOrEmpty(msg.blockClass))
            keyValues.add(new Tuple<>("Class", msg.blockClass));

        if (!msg.blockNBT.isEmpty()) {
            keyValues.add(new Tuple<>("NBT", msg.blockNBT.getAsString()));
            String lootTable = BlockUtil.getLootTable(msg.blockNBT);
            if (lootTable != null) {
                keyValues.add(new Tuple<>("LootTable", lootTable));
            }
        }
        //Minecraft.GetInstance().keyboardHandler.setClipboard(s);
        PlayerUtil.sendClientMessage(ComponentUtil.formatTitleKeyValueWithCopy("Block (" + msg.blockState + ") at: " + msg.blockPos.toShortString(), keyValues));
    }
}
