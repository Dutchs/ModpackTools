package com.dutchs.modpacktools.datagen;

import com.dutchs.modpacktools.Constants;
import com.dutchs.modpacktools.client.KeyBinds;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.common.data.LanguageProvider;

public class ModLanguageProvider extends LanguageProvider {

    public ModLanguageProvider(DataGenerator gen, String locale) {
        super(gen.getPackOutput(), Constants.MODID, locale);
    }

    @Override
    protected void addTranslations() {
        add(KeyBinds.KEY_CATEGORIES_HUD, "MT HUD");
        add(KeyBinds.KEY_HUD_ENTITY, "Entity");
        add(KeyBinds.KEY_HUD_TPS, "TPS");
        add(KeyBinds.KEY_HUD_FPS, "FPS");

        add(KeyBinds.KEY_CATEGORIES_CMD, "MT Commands");
        add(KeyBinds.KEY_CMD_HAND, "Hand");
        add(KeyBinds.KEY_CMD_HAND_NONBT, "Hand(no NBT)");
        add(KeyBinds.KEY_CMD_HOT, "Hotbar");
        add(KeyBinds.KEY_CMD_HOT_NONBT, "Hotbar(no NBT)");
        add(KeyBinds.KEY_CMD_INV, "Inventory");
        add(KeyBinds.KEY_CMD_INV_NONBT, "Inventory(no NBT)");
        add(KeyBinds.KEY_CMD_BLOCKINV, "Block Inventory");
        add(KeyBinds.KEY_CMD_BLOCKINV_NONBT, "Block Inventory(no NBT)");
        add(KeyBinds.KEY_CMD_BLOCK, "Block");
        add(KeyBinds.KEY_CMD_BLOCK_NONBT, "Block(no NBT)");
        add(KeyBinds.KEY_CMD_ENTITY, "Entity");
        add(KeyBinds.KEY_CMD_RECIPE, "RecipeMaker");
    }
}