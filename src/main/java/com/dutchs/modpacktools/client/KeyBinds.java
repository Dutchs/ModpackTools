package com.dutchs.modpacktools.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.client.ClientRegistry;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.client.settings.KeyModifier;

public class KeyBinds {
    public static final String KEY_CATEGORIES_MPT = "modpacktools.hud.category";
    public static final String KEY_HUD_ENTITY = "modpacktools.hud.entity";
    public static final String KEY_HUD_TPS = "modpacktools.hud.tps";
    public static final String KEY_HUD_FPS = "modpacktools.hud.fps";
    public static final String KEY_HUD_CHUNK = "modpacktools.hud.chunk";

    public static KeyMapping mapHUDEntity;
    public static KeyMapping mapHUDTPS;
    public static KeyMapping mapHUDFPS;
    public static KeyMapping mapHUDChunk;

    public static void init() {
        mapHUDEntity = new KeyMapping(KEY_HUD_ENTITY, KeyConflictContext.IN_GAME, KeyModifier.CONTROL, InputConstants.getKey("key.keyboard.e"), KEY_CATEGORIES_MPT);
        mapHUDTPS = new KeyMapping(KEY_HUD_TPS, KeyConflictContext.IN_GAME, KeyModifier.CONTROL, InputConstants.getKey("key.keyboard.t"), KEY_CATEGORIES_MPT);
        mapHUDFPS = new KeyMapping(KEY_HUD_FPS, KeyConflictContext.IN_GAME, KeyModifier.CONTROL, InputConstants.getKey("key.keyboard.f"), KEY_CATEGORIES_MPT);
        mapHUDChunk = new KeyMapping(KEY_HUD_CHUNK, KeyConflictContext.IN_GAME, KeyModifier.CONTROL, InputConstants.getKey("key.keyboard.c"), KEY_CATEGORIES_MPT);

        ClientRegistry.registerKeyBinding(mapHUDEntity);
        ClientRegistry.registerKeyBinding(mapHUDTPS);
        ClientRegistry.registerKeyBinding(mapHUDFPS);
        ClientRegistry.registerKeyBinding(mapHUDChunk);
    }
}
