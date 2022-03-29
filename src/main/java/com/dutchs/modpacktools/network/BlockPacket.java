package com.dutchs.modpacktools.network;

import com.dutchs.modpacktools.Constants;
import com.dutchs.modpacktools.network.NetworkManager.INetworkPacket;
import com.dutchs.modpacktools.util.BlockUtil;
import com.dutchs.modpacktools.util.ComponentUtil;
import com.dutchs.modpacktools.util.ItemStackUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.StringUtil;
import net.minecraft.util.Tuple;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class BlockPacket implements INetworkPacket {
    private BlockPos blockPos;
    private ClientPollForPacket.PollFor pollFor;

    public BlockPacket() {
    }

    public BlockPacket(BlockPos b, ClientPollForPacket.PollFor p) {
        blockPos = b;
        pollFor = p;
    }

    @Override
    public void encode(Object msg, FriendlyByteBuf packetBuffer) {
        BlockPacket blockPacket = (BlockPacket) msg;
        packetBuffer.writeBlockPos(blockPacket.blockPos);
        packetBuffer.writeEnum(blockPacket.pollFor);
    }

    @Override
    public <MESSAGE> MESSAGE decode(FriendlyByteBuf packetBuffer) {
        BlockPacket result = new BlockPacket();
        result.blockPos = packetBuffer.readBlockPos();
        result.pollFor = packetBuffer.readEnum(ClientPollForPacket.PollFor.class);
        return (MESSAGE) result;
    }

    @Override
    public void handle(Object msg, Supplier<NetworkEvent.Context> contextSupplier) {
        contextSupplier.get().enqueueWork(() -> {
            ServerPlayer p = contextSupplier.get().getSender();
            if (p != null) {
                BlockPacket blockPacket = (BlockPacket) msg;
                BlockPos pos = blockPacket.blockPos;
                ClientPollForPacket.PollFor pollFor = blockPacket.pollFor;
                Level level = p.getCommandSenderWorld();

                if (level.hasChunk(SectionPos.blockToSectionCoord(pos.getX()), SectionPos.blockToSectionCoord(pos.getZ()))) {
                    BlockEntity blockEntity = level.getBlockEntity(pos);
                    BlockState blockState = level.getBlockState(pos);

                    if ((pollFor == ClientPollForPacket.PollFor.BlockInv || pollFor == ClientPollForPacket.PollFor.BlockInvNBT)) {
                        List<ItemStack> stacks = new ArrayList<>();
                        //TODO: check for loottable, and print the old (soon to be defunct) loottable
                        int slots = BlockUtil.getContainerContents(blockEntity, stacks);
                        if (slots > 0) {
                            String itemStacks = ItemStackUtil.ItemStackPrinter(stacks, blockPacket.pollFor == ClientPollForPacket.PollFor.BlockInvNBT, false);
                            if (itemStacks != null) {
                                p.sendMessage(ComponentUtil.formatTitleContentWithCopy("Block (" + BlockUtil.getBlockStateRegisteryName(blockState) + ") at: " + pos.toShortString(), itemStacks), Constants.MOD_SENDER_UUID);
                            } else {
                                p.sendMessage(new TextComponent("Block (" + BlockUtil.getBlockStateRegisteryName(blockState) + ") at: " + pos.toShortString() + " is empty"), Constants.MOD_SENDER_UUID);
                            }
                        } else {
                            p.sendMessage(new TextComponent("Block (" + BlockUtil.getBlockStateRegisteryName(blockState) + ") at: " + pos.toShortString() + " is not a Container"), Constants.MOD_SENDER_UUID);
                        }
                    } else {
                        //Print block info
                        ArrayList<Tuple<String, String>> keyValues = new ArrayList<>();

                        if (blockEntity != null) {
                            if (blockEntity instanceof Container) {
                                keyValues.add(new Tuple<>("Type", "Container"));
                            }
                            keyValues.add(new Tuple<>("Class", blockEntity.getClass().getSimpleName()));

                            CompoundTag nbt = blockEntity.saveWithoutMetadata();
                            if (!nbt.isEmpty()) {
//                                List<Tag> printNBT = List.of(nbt);
//                                for (Tag element : printNBT) {
//                                    line.append(element.toString());
//                                }
                                keyValues.add(new Tuple<>("NBT", nbt.getAsString()));
                            }
                        }

                        if (blockEntity instanceof RandomizableContainerBlockEntity randBlockEntity) {
                            String lootTable = BlockUtil.getLootTable(randBlockEntity.saveWithoutMetadata());
                            if (!StringUtil.isNullOrEmpty(lootTable)) {
                                keyValues.add(new Tuple<>("LootTable", lootTable));
                            }
                        }

                        p.sendMessage(ComponentUtil.formatKeyValueWithCopy("Block (" + BlockUtil.getBlockStateRegisteryName(blockState) + ") at: " + pos.toShortString(), keyValues), Constants.MOD_SENDER_UUID);
                    }
                }
            }
        });
        contextSupplier.get().setPacketHandled(true);
    }
}
