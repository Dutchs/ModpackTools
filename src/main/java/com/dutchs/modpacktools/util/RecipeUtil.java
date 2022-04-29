package com.dutchs.modpacktools.util;

import net.minecraft.data.structures.NbtToSnbt;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.SnbtPrinterTagVisitor;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RecipeUtil {
    public static String makeShapelessJSONRecipe(ItemStack result, List<ItemStack> recipe) {
        List<ItemStack> noAirRecipe = recipe.stream().filter(i -> i.getItem() != Items.AIR).toList();
        StringBuilder sb = new StringBuilder();
        sb.append("{\n  \"type\": \"minecraft:crafting_shapeless\",\n  \"ingredients\": [\n");
        for (int i = 0; i < noAirRecipe.size(); i++) {
            formatIngredient(sb, noAirRecipe.get(i));
            if (i < noAirRecipe.size() - 1) {
                sb.append(",");
            }
            sb.append("\n");
        }
        sb.append("  ],\n");
        RecipeUtil.formatResult(sb, result);
        sb.append("}");
        return sb.toString();
    }

    public static String makeShapedJSONRecipe(ItemStack result, List<ItemStack> recipe) {
        List<String> noAirRecipe = recipe.stream().filter(i -> i.getItem() != Items.AIR).map(i -> i.getItem().getRegistryName().toString()).toList();
        List<String> uniqueNoAirRecipe = noAirRecipe.stream().distinct().toList();

        StringBuilder sb = new StringBuilder();
        sb.append("{\n  \"type\": \"minecraft:crafting_shaped\",\n");

        int key = 0;
        Map<String, String> pattern = new HashMap<>();
        pattern.put(Items.AIR.getRegistryName().toString(), " ");
        for (String r : uniqueNoAirRecipe) {
            pattern.put(r, String.valueOf(++key));
        }

        sb.append("  \"pattern\": [\n");

        List<ItemStack> top = recipe.subList(0, 3);
        List<ItemStack> middle = recipe.subList(3, 6);
        List<ItemStack> bottom = recipe.subList(6, 9);
        boolean topNonEmpty = !top.stream().allMatch(i -> i.getItem() == Items.AIR);
        boolean middleNonEmpty = !middle.stream().allMatch(i -> i.getItem() == Items.AIR);
        boolean bottomNonEmpty = !bottom.stream().allMatch(i -> i.getItem() == Items.AIR);

        List<ItemStack> left = List.of(top.get(0), middle.get(0), bottom.get(0));
        List<ItemStack> centre = List.of(top.get(1), middle.get(1), bottom.get(1));
        List<ItemStack> right = List.of(top.get(2), middle.get(2), bottom.get(2));
        boolean leftNonEmpty = !left.stream().allMatch(i -> i.getItem() == Items.AIR);
        boolean centreNonEmpty = !centre.stream().allMatch(i -> i.getItem() == Items.AIR);
        boolean rightNonEmpty = !right.stream().allMatch(i -> i.getItem() == Items.AIR);

        if (topNonEmpty) {
            sb.append("    \"");
            if (leftNonEmpty)
                sb.append(pattern.get(top.get(0).getItem().getRegistryName().toString()));
            if (centreNonEmpty || (leftNonEmpty && rightNonEmpty))
                sb.append(pattern.get(top.get(1).getItem().getRegistryName().toString()));
            if (rightNonEmpty)
                sb.append(pattern.get(top.get(2).getItem().getRegistryName().toString()));
            sb.append("\"");
            if (middleNonEmpty || bottomNonEmpty) {
                sb.append(",");
            }
            sb.append("\n");
        }

        if (middleNonEmpty || (topNonEmpty && bottomNonEmpty)) {
            sb.append("    \"");
            if (leftNonEmpty)
                sb.append(pattern.get(middle.get(0).getItem().getRegistryName().toString()));
            if (centreNonEmpty || (leftNonEmpty && rightNonEmpty))
                sb.append(pattern.get(middle.get(1).getItem().getRegistryName().toString()));
            if (rightNonEmpty)
                sb.append(pattern.get(middle.get(2).getItem().getRegistryName().toString()));
            sb.append("\"");
            if (bottomNonEmpty) {
                sb.append(",");
            }
            sb.append("\n");
        }

        if (bottomNonEmpty) {
            sb.append("    \"");
            if (leftNonEmpty)
                sb.append(pattern.get(bottom.get(0).getItem().getRegistryName().toString()));
            if (centreNonEmpty || (leftNonEmpty && rightNonEmpty))
                sb.append(pattern.get(bottom.get(1).getItem().getRegistryName().toString()));
            if (rightNonEmpty)
                sb.append(pattern.get(bottom.get(2).getItem().getRegistryName().toString()));
            sb.append("\"\n");
        }

        sb.append("  ],\n  \"key\": {\n");
        for (int i = 0; i < uniqueNoAirRecipe.size(); i++) {
            String resourceLocation = uniqueNoAirRecipe.get(i);
            sb.append("    \"");
            sb.append(pattern.get(resourceLocation));
            sb.append("\": {\n");

            sb.append("      \"item\": \"");
            sb.append(resourceLocation);
            sb.append("\"\n");

            if (i == uniqueNoAirRecipe.size() - 1) {
                sb.append("    }\n");
            } else {
                sb.append("    },\n");
            }
        }
        sb.append("  },\n");
        RecipeUtil.formatResult(sb, result);
        sb.append("}");
        return sb.toString();
    }

    private static void formatIngredient(StringBuilder sb, ItemStack stack) {
        sb.append("    {\n      \"item\": \"");
        sb.append(stack.getItem().getRegistryName());
        sb.append("\"");
        //TODO: This seems to not work, look into it later
//        CompoundTag nbt = stack.getTag();
//        if(nbt != null && !nbt.isEmpty()) {
//            sb.append(",\n      \"nbt\": ");
//            List<Tag> printNBT = List.of(nbt);
//            sb.append("\"");
//            for (Tag element : printNBT) {
//                sb.append(element.toString().replace("\"", "\\\""));
//            }
//            sb.append("\"");
//        }
        sb.append("\n    }");
    }

    private static void formatResult(StringBuilder sb, ItemStack stack) {
        sb.append("  \"result\": {\n    \"item\": \"");
        sb.append(stack.getItem().getRegistryName());
        sb.append("\"");
        if (stack.getCount() > 1) {
            sb.append(",\n    \"count\": ");
            sb.append(stack.getCount());
        }
        CompoundTag nbt = stack.getTag();
        if(nbt != null && !nbt.isEmpty()) {
            sb.append(",\n    \"nbt\": ");
            List<Tag> printNBT = List.of(nbt);
            sb.append("\"");
            for (Tag element : printNBT) {
                sb.append(element.toString().replace("\"", "\\\""));
            }
            sb.append("\"");
        }
        sb.append("\n  }\n");
    }
}
