package com.dutchs.modpacktools.util;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;

public class ItemStackUtil {

    @Nullable
    public static String ItemStackPrinter(ItemStack stack, boolean includeNBT, boolean includeEmpty) {
        StringBuilder line = null;

        if (stack != null && (!stack.isEmpty() || includeEmpty)) {
            line = new StringBuilder();
            line.append(Objects.requireNonNull(stack.getItem().getRegistryName()));
            if (includeNBT) {
                CompoundTag nbt = stack.getTag();
                if (nbt != null && !nbt.isEmpty()) {
                    List<Tag> printNBT = List.of(nbt);
                    for (Tag element : printNBT) {
                        line.append(element.toString());
                    }
                }
            }
        }

        return line != null ? line.toString() : null;
    }

    @Nullable
    public static String ItemStackPrinter(Iterable<ItemStack> itemStacks, boolean includeNBT, boolean includeEmpty) {
        StringBuilder line = new StringBuilder();

        boolean nothingToPrint = true;
        for (ItemStack stack : itemStacks) {
            String itemText = ItemStackPrinter(stack, includeNBT, includeEmpty);
            if (itemText != null) {
                if (nothingToPrint) {
                    nothingToPrint = false;
                } else {
                    line.append("\n");
                }
                line.append(itemText);
            }
        }

        return nothingToPrint ? null : line.toString();
    }
}
