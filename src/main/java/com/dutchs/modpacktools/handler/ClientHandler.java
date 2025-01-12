package com.dutchs.modpacktools.handler;

import com.dutchs.modpacktools.debug.HUDManager;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class ClientHandler {
    @SubscribeEvent
    public static void ClientPlayerLoggedOut(ClientPlayerNetworkEvent.LoggingOut event) {
        HUDManager.clearHUD();
    }
}
