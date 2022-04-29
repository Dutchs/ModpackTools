package com.dutchs.modpacktools.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.client.ClientRegistry;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.client.settings.KeyModifier;

public class KeyBinds {
    //HUD
    public static final String KEY_CATEGORIES_HUD = "modpacktools.hud.category";
    public static final String KEY_HUD_ENTITY = "modpacktools.hud.entity";
    public static final String KEY_HUD_TPS = "modpacktools.hud.tps";
    public static final String KEY_HUD_FPS = "modpacktools.hud.fps";
    public static final String KEY_HUD_CHUNK = "modpacktools.hud.chunk";

    public static KeyMapping mapHUDEntity;
    public static KeyMapping mapHUDTPS;
    public static KeyMapping mapHUDFPS;
    public static KeyMapping mapHUDChunk;

    //Commands
    public static final String KEY_CATEGORIES_CMD = "modpacktools.cmd.category";
    public static final String KEY_CMD_HAND = "modpacktools.cmd.hand";
    public static final String KEY_CMD_HOT = "modpacktools.cmd.hot";
    public static final String KEY_CMD_INV = "modpacktools.cmd.inv";
    public static final String KEY_CMD_BLOCKINV = "modpacktools.cmd.blockinv";
    public static final String KEY_CMD_BLOCK = "modpacktools.cmd.block";
    public static final String KEY_CMD_RECIPE = "modpacktools.cmd.recipe";
    public static final String KEY_CMD_ENTITY = "modpacktools.cmd.entity";

    public static KeyMapping mapCMDHand;
    public static KeyMapping mapCMDHot;
    public static KeyMapping mapCMDInv;
    public static KeyMapping mapCMDBlockInv;
    public static KeyMapping mapCMDBlock;
    public static KeyMapping mapCMDRecipe;
    public static KeyMapping mapCMDEntity;

    public static void init() {
        //HUD
        mapHUDEntity = new KeyMapping(KEY_HUD_ENTITY, KeyConflictContext.IN_GAME, KeyModifier.CONTROL, InputConstants.getKey("key.keyboard.e"), KEY_CATEGORIES_HUD);
        mapHUDTPS = new KeyMapping(KEY_HUD_TPS, KeyConflictContext.IN_GAME, KeyModifier.CONTROL, InputConstants.getKey("key.keyboard.t"), KEY_CATEGORIES_HUD);
        mapHUDFPS = new KeyMapping(KEY_HUD_FPS, KeyConflictContext.IN_GAME, KeyModifier.CONTROL, InputConstants.getKey("key.keyboard.f"), KEY_CATEGORIES_HUD);
        mapHUDChunk = new KeyMapping(KEY_HUD_CHUNK, KeyConflictContext.IN_GAME, KeyModifier.NONE, InputConstants.UNKNOWN, KEY_CATEGORIES_HUD);

        ClientRegistry.registerKeyBinding(mapHUDEntity);
        ClientRegistry.registerKeyBinding(mapHUDTPS);
        ClientRegistry.registerKeyBinding(mapHUDFPS);
        ClientRegistry.registerKeyBinding(mapHUDChunk);

        //Commands
        mapCMDHand = new KeyMapping(KEY_CMD_HAND, KeyConflictContext.IN_GAME, KeyModifier.NONE, InputConstants.UNKNOWN, KEY_CATEGORIES_CMD);
        mapCMDHot = new KeyMapping(KEY_CMD_HOT, KeyConflictContext.IN_GAME, KeyModifier.NONE, InputConstants.UNKNOWN, KEY_CATEGORIES_CMD);
        mapCMDInv = new KeyMapping(KEY_CMD_INV, KeyConflictContext.IN_GAME, KeyModifier.NONE, InputConstants.UNKNOWN, KEY_CATEGORIES_CMD);
        mapCMDBlockInv = new KeyMapping(KEY_CMD_BLOCKINV, KeyConflictContext.IN_GAME, KeyModifier.NONE, InputConstants.UNKNOWN, KEY_CATEGORIES_CMD);
        mapCMDBlock = new KeyMapping(KEY_CMD_BLOCK, KeyConflictContext.IN_GAME, KeyModifier.NONE, InputConstants.UNKNOWN, KEY_CATEGORIES_CMD);
        mapCMDRecipe = new KeyMapping(KEY_CMD_RECIPE, KeyConflictContext.IN_GAME, KeyModifier.CONTROL, InputConstants.getKey("key.keyboard.r"), KEY_CATEGORIES_CMD);
        mapCMDEntity = new KeyMapping(KEY_CMD_ENTITY, KeyConflictContext.IN_GAME, KeyModifier.NONE, InputConstants.UNKNOWN, KEY_CATEGORIES_CMD);

        ClientRegistry.registerKeyBinding(mapCMDHand);
        ClientRegistry.registerKeyBinding(mapCMDHot);
        ClientRegistry.registerKeyBinding(mapCMDInv);
        ClientRegistry.registerKeyBinding(mapCMDBlockInv);
        ClientRegistry.registerKeyBinding(mapCMDBlock);
        ClientRegistry.registerKeyBinding(mapCMDRecipe);
        ClientRegistry.registerKeyBinding(mapCMDEntity);
    }
}
