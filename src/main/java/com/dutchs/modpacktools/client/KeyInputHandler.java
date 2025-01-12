package com.dutchs.modpacktools.client;

import com.dutchs.modpacktools.ConfigHandler;
import com.dutchs.modpacktools.debug.HUDManager;
import com.dutchs.modpacktools.util.CommandUtil;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class KeyInputHandler {

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onKeyInput(InputEvent.Key event) {
        if (KeyBinds.mapHUDFPS.consumeClick()) {
            HUDManager.RENDERFPS = !HUDManager.RENDERFPS;
        }

        Minecraft instance = Minecraft.getInstance();

        if (instance.hasSingleplayerServer()) {
            if (KeyBinds.mapHUDTPS.consumeClick()) {
                HUDManager.RENDERTPS = !HUDManager.RENDERTPS;
            }

            if (instance.player != null && instance.player.hasPermissions(2)) {
                if (KeyBinds.mapHUDEntity.consumeClick()) {
                    HUDManager.RENDERENTITY = !HUDManager.RENDERENTITY;
                }
            }
        }

        if (KeyBinds.mapCMDHand.consumeClick()) {
            CommandUtil.SendHandCommand(true);
        }
        if (KeyBinds.mapCMDHandNONBT.consumeClick()) {
            CommandUtil.SendHandCommand(false);
        }
        if (KeyBinds.mapCMDHot.consumeClick()) {
            CommandUtil.SendHotCommand(true);
        }
        if (KeyBinds.mapCMDHotNONBT.consumeClick()) {
            CommandUtil.SendHotCommand(false);
        }
        if (KeyBinds.mapCMDInv.consumeClick()) {
            CommandUtil.SendInvCommand(true);
        }
        if (KeyBinds.mapCMDInvNONBT.consumeClick()) {
            CommandUtil.SendInvCommand(false);
        }
        if (KeyBinds.mapCMDBlockInv.consumeClick()) {
            CommandUtil.SendBlockInvCommand(true, false, ConfigHandler.printAsJSON);
        }
        if (KeyBinds.mapCMDBlockInvNONBT.consumeClick()) {
            CommandUtil.SendBlockInvCommand(false, false, ConfigHandler.printAsJSON);
        }
        if (KeyBinds.mapCMDBlock.consumeClick()) {
            CommandUtil.SendBlockCommand(true);
        }
        if (KeyBinds.mapCMDBlockNONBT.consumeClick()) {
            CommandUtil.SendBlockCommand(false);
        }
        if (KeyBinds.mapCMDRecipe.consumeClick()) {
            CommandUtil.SendRecipeMakerCommand();
        }
        if (KeyBinds.mapCMDEntity.consumeClick()) {
            CommandUtil.SendEntityCommand(null, null, -1);
        }
    }
}
