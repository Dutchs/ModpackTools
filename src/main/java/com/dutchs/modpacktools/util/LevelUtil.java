package com.dutchs.modpacktools.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class LevelUtil {
    public static int getContainerContents(Level level, BlockPos pos, @NotNull List<ItemStack> stacks) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        return BlockUtil.getContainerContents(blockEntity, stacks);
    }

    public static boolean hasChunkFromBlockPos(Level level, BlockPos pos) {
        return level.hasChunk(SectionPos.blockToSectionCoord(pos.getX()), SectionPos.blockToSectionCoord(pos.getZ()));
    }
}
