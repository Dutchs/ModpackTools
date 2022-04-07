package com.dutchs.modpacktools.client;

import com.dutchs.modpacktools.debug.HUDManager;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.InputEvent;

public class KeyInputHandler {

    public static void onKeyInput(InputEvent.KeyInputEvent event) {
        if (KeyBinds.mapHUDFPS.consumeClick()) {
            HUDManager.RENDERFPS = !HUDManager.RENDERFPS;
        }

        Minecraft instance = Minecraft.getInstance();

        if (instance.hasSingleplayerServer()) {
            if (KeyBinds.mapHUDTPS.consumeClick()) {
                HUDManager.RENDERTPS = !HUDManager.RENDERTPS;
            }

            if (instance.player != null && instance.player.hasPermissions(2)) {
                if (KeyBinds.mapHUDChunk.consumeClick()) {
                    HUDManager.RENDERCHUNK = !HUDManager.RENDERCHUNK;
                }
                if (KeyBinds.mapHUDEntity.consumeClick()) {
                    HUDManager.RENDERENTITY = !HUDManager.RENDERENTITY;
                }
            }
        }
    }
}
