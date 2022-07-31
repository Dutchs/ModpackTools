package com.dutchs.modpacktools.handler;

import com.dutchs.modpacktools.client.KeyBinds;
import com.dutchs.modpacktools.debug.HUDManager;
import com.dutchs.modpacktools.util.CommandUtil;
import net.minecraft.client.Minecraft;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class ClientHandler {
    @SubscribeEvent
    public static void PlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        //NOTE: clears the hud after loading different world
        //if(Minecraft.getInstance().hasSingleplayerServer()){
        HUDManager.clearHUD();
        //}
    }
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onClientTick(final TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.START ) { return; }

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
