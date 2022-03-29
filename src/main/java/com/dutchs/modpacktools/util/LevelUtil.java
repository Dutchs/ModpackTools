package com.dutchs.modpacktools.util;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.items.CapabilityItemHandler;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class LevelUtil {
    public static int getContainerContents(Level level, BlockPos pos, @NotNull List<ItemStack> stacks){
        BlockEntity blockEntity = level.getBlockEntity(pos);
        return BlockUtil.getContainerContents(blockEntity, stacks);
    }
}
