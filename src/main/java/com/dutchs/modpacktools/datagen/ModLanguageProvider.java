package com.dutchs.modpacktools.datagen;

import com.dutchs.modpacktools.Constants;
import com.dutchs.modpacktools.client.KeyBinds;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.common.data.LanguageProvider;

public class ModLanguageProvider extends LanguageProvider {

    public ModLanguageProvider(DataGenerator gen, String locale) {
        super(gen, Constants.MODID, locale);
    }

    @Override
    protected void addTranslations() {
        add(KeyBinds.KEY_CATEGORIES_HUD, "MT HUD");
        add(KeyBinds.KEY_HUD_ENTITY, "Entity");
        add(KeyBinds.KEY_HUD_TPS, "TPS");
        add(KeyBinds.KEY_HUD_FPS, "FPS");
        add(KeyBinds.KEY_HUD_CHUNK, "Chunk");

        add(KeyBinds.KEY_CATEGORIES_CMD, "MT Commands");
        add(KeyBinds.KEY_CMD_HAND, "Hand");
        add(KeyBinds.KEY_CMD_HOT, "Hot");
        add(KeyBinds.KEY_CMD_INV, "Inv");
        add(KeyBinds.KEY_CMD_BLOCKINV, "BlockInv");
        add(KeyBinds.KEY_CMD_BLOCK, "Block");
        add(KeyBinds.KEY_CMD_RECIPE, "RecipeMaker");
        add(KeyBinds.KEY_CMD_ENTITY, "Entity");
    }
}