package com.dutchs.modpacktools.util;

import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RecipeUtil {
    private static final Gson GSON = (new GsonBuilder()).setPrettyPrinting().create();

    private static void addNBT(ItemStack result, JsonObject resultJSON) {
        StringBuilder sb = new StringBuilder();
        CompoundTag nbt = result.getTag();
        if (nbt != null) {
            if (nbt.contains("Damage", 99)) {
                int dmg = nbt.getInt("Damage");
                if (dmg == 0) {
                    nbt.remove("Damage");
                }
            }
            if (!nbt.isEmpty()) {
                List<Tag> printNBT = List.of(nbt);
                for (Tag element : printNBT) {
                    sb.append(element.toString());
                }
                resultJSON.addProperty("nbt", sb.toString());
            }
        }
    }

    private static List<String> createPattern(Map<Item, Character> reverseKeys, List<ItemStack> recipe) {
        List<String> result = new ArrayList<>();

        //Rows
        List<ItemStack> top = recipe.subList(0, 3);
        List<ItemStack> middle = recipe.subList(3, 6);
        List<ItemStack> bottom = recipe.subList(6, 9);
        boolean topNonEmpty = !top.stream().allMatch(i -> i.getItem() == Items.AIR);
        boolean middleNonEmpty = !middle.stream().allMatch(i -> i.getItem() == Items.AIR);
        boolean bottomNonEmpty = !bottom.stream().allMatch(i -> i.getItem() == Items.AIR);

        //Columns
        List<ItemStack> left = List.of(top.get(0), middle.get(0), bottom.get(0));
        List<ItemStack> centre = List.of(top.get(1), middle.get(1), bottom.get(1));
        List<ItemStack> right = List.of(top.get(2), middle.get(2), bottom.get(2));
        boolean leftNonEmpty = !left.stream().allMatch(i -> i.getItem() == Items.AIR);
        boolean centreNonEmpty = !centre.stream().allMatch(i -> i.getItem() == Items.AIR);
        boolean rightNonEmpty = !right.stream().allMatch(i -> i.getItem() == Items.AIR);

        if (topNonEmpty)
            formatRow(result, reverseKeys, top, leftNonEmpty, centreNonEmpty, rightNonEmpty);

        if (middleNonEmpty || (topNonEmpty && bottomNonEmpty))
            formatRow(result, reverseKeys, middle, leftNonEmpty, centreNonEmpty, rightNonEmpty);

        if (bottomNonEmpty)
            formatRow(result, reverseKeys, bottom, leftNonEmpty, centreNonEmpty, rightNonEmpty);

        return result;
    }

    private static void formatRow(List<String> result, Map<Item, Character> reverseKeys, List<ItemStack> row, boolean leftNonEmpty, boolean centreNonEmpty, boolean rightNonEmpty) {
        StringBuilder sb = new StringBuilder();
        boolean hasValue = false;
        if (leftNonEmpty) {
            hasValue = true;
            sb.append(reverseKeys.get(row.get(0).getItem()));
        }
        if (centreNonEmpty || (leftNonEmpty && rightNonEmpty)) {
            hasValue = true;
            sb.append(reverseKeys.get(row.get(1).getItem()));
        }
        if (rightNonEmpty) {
            hasValue = true;
            sb.append(reverseKeys.get(row.get(2).getItem()));
        }
        if (hasValue) {
            result.add(sb.toString());
        }
    }

    public static String createShapedJSON(ItemStack result, List<ItemStack> recipe) {
        JsonObject recipeJSON = new JsonObject();
        recipeJSON.addProperty("type", "minecraft:crafting_shaped");

        JsonObject keysJSON = new JsonObject();
        Map<Character, Item> keys = Maps.newLinkedHashMap();
        Map<Item, Character> reverseKeys = Maps.newLinkedHashMap();
        List<Item> filteredRecipe = recipe.stream().map(ItemStack::getItem).filter(item -> item != Items.AIR).distinct().toList();
        for (int i = 0; i < filteredRecipe.size(); i++) {
            keys.put(Character.forDigit(i + 1, 10), filteredRecipe.get(i));
            reverseKeys.put(filteredRecipe.get(i), Character.forDigit(i + 1, 10));
        }
        for (Map.Entry<Character, Item> entry : keys.entrySet()) {
            keysJSON.add(String.valueOf(entry.getKey()), Ingredient.of(entry.getValue()).toJson());
        }

        reverseKeys.put(Items.AIR, ' ');
        List<String> pattern = createPattern(reverseKeys, recipe);
        JsonArray patternJSON = new JsonArray();
        for (String s : pattern) {
            patternJSON.add(s);
        }

        recipeJSON.add("pattern", patternJSON);
        recipeJSON.add("key", keysJSON);

        JsonObject resultJSON = new JsonObject();
        resultJSON.addProperty("item", result.getItem().getRegistryName().toString());
        addNBT(result, resultJSON);
        if (result.getCount() > 1) {
            resultJSON.addProperty("count", result.getCount());
        }
        recipeJSON.add("result", resultJSON);

        return GSON.toJson(recipeJSON);
    }

    public static String createShapelessJSON(ItemStack result, List<ItemStack> recipe) {
        JsonObject recipeJSON = new JsonObject();
        recipeJSON.addProperty("type", "minecraft:crafting_shapeless");

        JsonArray ingredientsJSON = new JsonArray();
        for (ItemStack recipeStack : recipe) {
            Item recipeItem = recipeStack.getItem();
            if (recipeItem != Items.AIR) {
                ingredientsJSON.add(Ingredient.of(recipeStack).toJson());
            }
        }
        recipeJSON.add("ingredients", ingredientsJSON);

        JsonObject resultJSON = new JsonObject();
        resultJSON.addProperty("item", result.getItem().getRegistryName().toString());
        addNBT(result, resultJSON);
        if (result.getCount() > 1) {
            resultJSON.addProperty("count", result.getCount());
        }
        recipeJSON.add("result", resultJSON);

        return GSON.toJson(recipeJSON);
    }

//    private static String formatShapedInput(List<ItemStack> input){
//        return String.join(", ", input.stream().map(i -> i.getItem().getRegistryName().toString()).toList());
//    }

    public static String formatResult(ItemStack result) {
        return result.getItem().getRegistryName().toString() + (result.getCount() > 1 ? "(" + result.getCount() + ")" : "");
    }

    public static String formatInput(List<ItemStack> input) {
        Map<String, Integer> itemCounter = ItemStackUtil.ItemStackCounter(input);

        List<String> countedInputs = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : itemCounter.entrySet()) {
            countedInputs.add(entry.getKey() + (entry.getValue() > 1 ? "(" + entry.getValue() + ")" : ""));
        }

        return String.join(", ", countedInputs);
    }
}
