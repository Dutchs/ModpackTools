package com.dutchs.modpacktools.handler;

import com.dutchs.modpacktools.debug.HUDManager;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class ClientHandler {
    @SubscribeEvent
    public static void PlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        //NOTE: clears the hud after loading different world
        //if(Minecraft.getInstance().hasSingleplayerServer()){
        HUDManager.clearHUD();
        //}
    }
}
