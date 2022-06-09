package com.dutchs.modpacktools.util;

import com.google.common.collect.Maps;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ItemStackUtil {

    @Nullable
    public static String ItemStackPrinter(ItemStack stack, boolean includeNBT, boolean includeEmpty) {
        StringBuilder line = null;

        if (stack != null && (!stack.isEmpty() || includeEmpty)) {
            line = new StringBuilder();
            line.append(ForgeRegistries.ITEMS.getKey(stack.getItem()).toString());
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

    public static Map<String, Integer> ItemStackCounter(List<ItemStack> input) {
        Map<String, Integer> itemCounter = Maps.newLinkedHashMap();
        for (ItemStack stack : input) {
            Item item = stack.getItem();
            if (item != Items.AIR) {
                String itemName = ForgeRegistries.ITEMS.getKey(item).toString();
                itemCounter.merge(itemName, 1, Integer::sum);
            }
        }
        return itemCounter;
    }
}
