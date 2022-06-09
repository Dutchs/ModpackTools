package com.dutchs.modpacktools.util;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class BlockUtil {

    public static ResourceLocation getBlockStateRegisteryName(BlockState state) {
        return ForgeRegistries.BLOCKS.getKey(state.getBlock());
    }

    public static boolean hasItemHandlerCapability(BlockEntity blockEntity) {
        if (blockEntity != null) {
            return blockEntity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).isPresent();
        }
        return false;
    }

    public static int getContainerContents(BlockEntity blockEntity, @Nonnull List<ItemStack> stacks) {
        AtomicInteger maxSlots = new AtomicInteger();
        try {
            if (blockEntity != null && blockEntity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).isPresent()) {
                blockEntity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).ifPresent(capability -> {
                    maxSlots.set(capability.getSlots());
                    for (int i = 0; i < maxSlots.get(); i++) {
                        ItemStack stack = capability.getStackInSlot(i);
                        if (!stack.isEmpty()) {
                            stacks.add(stack);
                        }
                    }
                });
            } else if (blockEntity instanceof Container inventory) {
                maxSlots.set(inventory.getContainerSize());
                for (int i = 0; i < maxSlots.get(); i++) {
                    ItemStack stack = inventory.getItem(i);
                    if (!stack.isEmpty()) {
                        stacks.add(stack);
                    }
                }
            }
        } catch (RuntimeException e) { } //Ignored
        return maxSlots.get();
    }

    @Nullable
    public static String getLootTable(CompoundTag pTag) {
        if (pTag.contains("LootTable", 8)) {
            return pTag.getString("LootTable");
        } else {
            return null;
        }
    }
}
