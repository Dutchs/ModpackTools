package com.dutchs.modpacktools.network;

import com.dutchs.modpacktools.Constants;
import com.dutchs.modpacktools.ModpackTools;
import com.dutchs.modpacktools.network.NetworkManager.INetworkPacket;
import com.dutchs.modpacktools.util.BlockUtil;
import com.dutchs.modpacktools.util.ComponentUtil;
import com.dutchs.modpacktools.util.ItemStackUtil;
import com.dutchs.modpacktools.util.LevelUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.network.NetworkEvent;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class BlockPacket implements INetworkPacket {
    private BlockPos blockPos;
    private boolean inventory;

    public BlockPacket() {
    }

    public BlockPacket(@NotNull BlockPos b, boolean inv) {
        blockPos = b;
        inventory = inv;
    }

    @Override
    public void encode(Object msg, FriendlyByteBuf packetBuffer) {
        BlockPacket blockPacket = (BlockPacket) msg;
        packetBuffer.writeBlockPos(blockPacket.blockPos);
        packetBuffer.writeBoolean(blockPacket.inventory);
    }

    @Override
    public <MESSAGE> MESSAGE decode(FriendlyByteBuf packetBuffer) {
        BlockPacket result = new BlockPacket();
        result.blockPos = packetBuffer.readBlockPos();
        result.inventory = packetBuffer.readBoolean();
        return (MESSAGE) result;
    }

    @Override
    public void handle(Object msg, Supplier<NetworkEvent.Context> contextSupplier) {
        contextSupplier.get().enqueueWork(() -> {
            ServerPlayer p = contextSupplier.get().getSender();
            if (p != null) {
                if(p.hasPermissions( 2)) {
                    BlockPacket blockPacket = (BlockPacket) msg;
                    BlockPos pos = blockPacket.blockPos;
                    boolean inv = blockPacket.inventory;
                    Level level = p.getCommandSenderWorld();
                    if (LevelUtil.hasChunkFromBlockPos(level, pos)) {
                        BlockEntity blockEntity = level.getBlockEntity(pos);
                        BlockState blockState = level.getBlockState(pos);

                        if(inv) {
                            List<ItemStack> stacks = new ArrayList<>();
                            if(blockEntity != null) {
                                CompoundTag nbt = blockEntity.saveWithoutMetadata();
                                String lootTable = BlockUtil.getLootTable(nbt);
                                if(lootTable != null) {
                                    p.sendMessage(ComponentUtil.formatKeyValueWithCopy("Evaluated LootTable", lootTable), Constants.MOD_SENDER_UUID);
                                }
                            }

                            int slots = BlockUtil.getContainerContents(blockEntity, stacks);
                            if (slots > 0) {
                                String itemStacks = ItemStackUtil.ItemStackPrinter(stacks, true, false);
                                ClientInventoryResultPacket resultPacket = new ClientInventoryResultPacket(InventoryPacket.InventoryType.BlockInventory, itemStacks == null ? "" : itemStacks);
                                ModpackTools.NETWORK.toPlayer(resultPacket, p);
                            } else {
                                p.sendMessage(new TextComponent("Block (" + BlockUtil.getBlockStateRegisteryName(blockState) + ") at: " + pos.toShortString() + " is not a Container").withStyle(Constants.ERROR_FORMAT), Constants.MOD_SENDER_UUID);
                            }
                        } else {
                            String type = "";
                            String clazz = "";
                            CompoundTag nbt = new CompoundTag();

                            if (blockEntity != null) {
                                if (blockEntity instanceof Container)
                                    type = "Container";

                                clazz = blockEntity.getClass().getSimpleName();
                                nbt = blockEntity.saveWithoutMetadata();
                            }

                            ClientBlockResultPacket result = new ClientBlockResultPacket(BlockUtil.getBlockStateRegisteryName(blockState).toString(), pos, type, clazz, nbt);
                            ModpackTools.NETWORK.toPlayer(result, p);
                        }
                    } else {
                        p.sendMessage(new TextComponent("Can't fetch data from unloaded chunks").withStyle(Constants.ERROR_FORMAT), Constants.MOD_SENDER_UUID);
                    }
                } else {
                    p.sendMessage(new TextComponent("You lack permissions to run this command").withStyle(Constants.ERROR_FORMAT), Constants.MOD_SENDER_UUID);
                }
            }
        });
        contextSupplier.get().setPacketHandled(true);
    }
}
