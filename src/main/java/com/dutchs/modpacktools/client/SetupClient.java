package com.dutchs.modpacktools.client;

import com.dutchs.modpacktools.debug.EntityOverlay;
import com.dutchs.modpacktools.debug.FPSOverlay;
import com.dutchs.modpacktools.debug.TPSOverlay;
import com.dutchs.modpacktools.gui.RecipeMakerScreen;
import com.dutchs.modpacktools.registry.ContainerRegistry;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraftforge.client.gui.ForgeIngameGui;
import net.minecraftforge.client.gui.OverlayRegistry;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

public class SetupClient {

    public static void init(FMLClientSetupEvent event) {
        OverlayRegistry.registerOverlayAbove(ForgeIngameGui.CHAT_PANEL_ELEMENT, "tpshud", TPSOverlay.TPS_HUD);
        OverlayRegistry.registerOverlayAbove(ForgeIngameGui.CHAT_PANEL_ELEMENT, "fpshud", FPSOverlay.FPS_HUD);
        OverlayRegistry.registerOverlayAbove(ForgeIngameGui.CHAT_PANEL_ELEMENT, "entityhud", EntityOverlay.ENTITY_HUD);
        MenuScreens.register(ContainerRegistry.RECIPE_MAKER, RecipeMakerScreen::new);
        KeyBinds.init();
    }
}
