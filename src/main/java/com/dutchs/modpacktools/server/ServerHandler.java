package com.dutchs.modpacktools.server;

import com.dutchs.modpacktools.ModpackTools;
import com.dutchs.modpacktools.debug.GCManager;
import com.dutchs.modpacktools.debug.HUDManager;
import net.minecraft.client.Minecraft;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class ServerHandler {
    public static final GCManager GC_MANAGER = new GCManager();

    @SubscribeEvent
    public static void ServerTick(TickEvent.ServerTickEvent event) {
        if(event.phase == TickEvent.Phase.END && HUDManager.RENDERTPS)
            GC_MANAGER.Tick();
    }
}
