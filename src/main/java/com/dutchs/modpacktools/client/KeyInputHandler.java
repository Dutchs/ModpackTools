package com.dutchs.modpacktools.client;

import com.dutchs.modpacktools.debug.HUDManager;
import com.dutchs.modpacktools.util.CommandUtil;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class KeyInputHandler {

    @SubscribeEvent(priority = EventPriority.HIGHEST)
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

        if (KeyBinds.mapCMDHand.consumeClick()) {
            CommandUtil.SendHandCommand(true);
        }
        if (KeyBinds.mapCMDHot.consumeClick()) {
            CommandUtil.SendHotCommand(true);
        }
        if (KeyBinds.mapCMDInv.consumeClick()) {
            CommandUtil.SendInvCommand(true);
        }
        if (KeyBinds.mapCMDBlockInv.consumeClick()) {
            CommandUtil.SendBlockInvCommand(true);
        }
        if (KeyBinds.mapCMDBlock.consumeClick()) {
            CommandUtil.SendBlockCommand(true);
        }
        if (KeyBinds.mapCMDRecipe.consumeClick()) {
            CommandUtil.SendRecipeMakerCommand();
        }
        if (KeyBinds.mapCMDEntity.consumeClick()) {
            CommandUtil.SendEntityCommand(null, null, -1);
        }
    }
}
