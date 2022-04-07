package com.dutchs.modpacktools.network;

import com.dutchs.modpacktools.ModpackTools;
import com.dutchs.modpacktools.network.NetworkManager.INetworkPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ClientPollForPacket implements INetworkPacket {
    private PollFor pollFor;

    public enum PollFor {
        Block,
        BlockInv,
        BlockInvNBT
    }

    public ClientPollForPacket() {
    }

    public ClientPollForPacket(PollFor b) {
        pollFor = b;
    }

    @Override
    public void encode(Object msg, FriendlyByteBuf packetBuffer) {
        ClientPollForPacket blockPacket = (ClientPollForPacket) msg;
        packetBuffer.writeEnum(blockPacket.pollFor);
    }

    @Override
    public <MESSAGE> MESSAGE decode(FriendlyByteBuf packetBuffer) {
        ClientPollForPacket result = new ClientPollForPacket();
        result.pollFor = packetBuffer.readEnum(PollFor.class);
        return (MESSAGE) result;
    }

    @Override
    public void handle(Object msg, Supplier<NetworkEvent.Context> contextSupplier) {
        contextSupplier.get().enqueueWork(() -> {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> handleClient((ClientPollForPacket) msg, contextSupplier));
        });
        contextSupplier.get().setPacketHandled(true);
    }

    private void handleClient(ClientPollForPacket msg, Supplier<NetworkEvent.Context> contextSupplier) {
        Minecraft instance = Minecraft.getInstance();
        if (instance.player != null) {
            if (msg.pollFor == PollFor.Block || msg.pollFor == PollFor.BlockInv || msg.pollFor == PollFor.BlockInvNBT) {

                //HitResult lookingAt = Minecraft.getInstance().player.pick(20.0D, 0.0F, false);
                HitResult hit = instance.hitResult;
                if ((hit != null ? hit.getType() : null) == HitResult.Type.BLOCK) {
                    BlockPos pos = ((BlockHitResult) hit).getBlockPos();
                    ModpackTools.NETWORK.toServer(new BlockPacket(pos, msg.pollFor));
                }
            }
        }
    }
}
