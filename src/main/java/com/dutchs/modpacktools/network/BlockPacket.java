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
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import oshi.software.os.unix.freebsd.FreeBsdFileSystem;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class BlockPacket implements INetworkPacket {
    private BlockPos blockPos;
    private boolean inventory;
    private boolean includeNBT;
    private boolean removeInventory;
    private boolean asJSON;

    public BlockPacket() {
    }

    public BlockPacket(@NotNull BlockPos b, boolean inv, boolean nbt, boolean remove, boolean json) {
        blockPos = b;
        inventory = inv;
        includeNBT = nbt;
        removeInventory = remove;
        asJSON = json;
    }

    @Override
    public void encode(Object msg, FriendlyByteBuf packetBuffer) {
        BlockPacket blockPacket = (BlockPacket) msg;
        packetBuffer.writeBlockPos(blockPacket.blockPos);
        packetBuffer.writeBoolean(blockPacket.inventory);
        packetBuffer.writeBoolean(blockPacket.includeNBT);
        packetBuffer.writeBoolean(blockPacket.removeInventory);
        packetBuffer.writeBoolean(blockPacket.asJSON);
    }

    @Override
    public <MESSAGE> MESSAGE decode(FriendlyByteBuf packetBuffer) {
        BlockPacket result = new BlockPacket();
        result.blockPos = packetBuffer.readBlockPos();
        result.inventory = packetBuffer.readBoolean();
        result.includeNBT = packetBuffer.readBoolean();
        result.removeInventory = packetBuffer.readBoolean();
        result.asJSON = packetBuffer.readBoolean();
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
                    boolean nbt = blockPacket.includeNBT;
                    Level level = p.getCommandSenderWorld();
                    if (LevelUtil.hasChunkFromBlockPos(level, pos)) {
                        BlockEntity blockEntity = level.getBlockEntity(pos);
                        BlockState blockState = level.getBlockState(pos);

                        if(inv) {

                            if(blockEntity != null) {
                                CompoundTag tags = blockEntity.saveWithoutMetadata();
                                String lootTable = BlockUtil.getLootTable(tags);
                                if(lootTable != null) {
                                    p.sendSystemMessage(ComponentUtil.formatKeyValueWithCopy("Evaluated LootTable", lootTable));
                                }
                            }

                            List<ItemStack> stacks = new ArrayList<>();
                            int slots = BlockUtil.getContainerContents(blockEntity, stacks);
                            if (slots > 0) {
                                String itemStacks = ItemStackUtil.ItemStackPrinter(stacks, nbt, false, blockPacket.asJSON);
                                ClientInventoryResultPacket resultPacket = new ClientInventoryResultPacket(InventoryPacket.InventoryType.BlockInventory, itemStacks == null ? "" : itemStacks);
                                ModpackTools.NETWORK.toPlayer(resultPacket, p);
                                if(blockPacket.removeInventory) {
                                    BlockUtil.clearContainerContents(blockEntity);
                                }
                            } else {
                                p.sendSystemMessage(Component.literal("Block (" + BlockUtil.getBlockStateRegisteryName(blockState) + ") at: " + pos.toShortString() + " is not a Container").withStyle(Constants.ERROR_FORMAT));
                            }
                        } else {
                            String type = "";
                            String blockEntityType = "";
                            String clazz = "";
                            CompoundTag tags = new CompoundTag();

                            if (blockEntity != null) {
                                if (blockEntity instanceof Container)
                                    type = "Container";

                                ResourceLocation typesKey = ForgeRegistries.BLOCK_ENTITY_TYPES.getKey(blockEntity.getType());
                                if(typesKey != null) {
                                    blockEntityType = typesKey.toString();
                                }

                                clazz = blockEntity.getClass().getSimpleName();

                                if(nbt)
                                    tags = blockEntity.saveWithoutMetadata();

                            }

                            ClientBlockResultPacket result = new ClientBlockResultPacket(BlockUtil.getBlockStateRegisteryName(blockState).toString(), pos, type, blockEntityType, clazz, tags);
                            ModpackTools.NETWORK.toPlayer(result, p);
                        }
                    } else {
                        p.sendSystemMessage(Component.literal("Can't fetch data from unloaded chunks").withStyle(Constants.ERROR_FORMAT));
                    }
                } else {
                    p.sendSystemMessage(Component.literal("You lack permissions to run this command").withStyle(Constants.ERROR_FORMAT));
                }
            }
        });
        contextSupplier.get().setPacketHandled(true);
    }
}
