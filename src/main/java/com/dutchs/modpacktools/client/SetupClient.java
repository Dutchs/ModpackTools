package com.dutchs.modpacktools.client;

import com.dutchs.modpacktools.debug.EntityOverlay;
import com.dutchs.modpacktools.debug.FPSOverlay;
import com.dutchs.modpacktools.debug.TPSOverlay;
import com.dutchs.modpacktools.gui.RecipeMakerScreen;
import com.dutchs.modpacktools.registry.ContainerRegistry;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

public class SetupClient {

    @SubscribeEvent
    public static void onRegisterGuiOverlaysEvent(RegisterGuiOverlaysEvent event) {
        event.registerAbove(VanillaGuiOverlay.CHAT_PANEL.id(), "tpshud", TPSOverlay.TPS_HUD);
        event.registerAbove(VanillaGuiOverlay.CHAT_PANEL.id(), "fpshud", FPSOverlay.FPS_HUD);
        event.registerAbove(VanillaGuiOverlay.CHAT_PANEL.id(), "entityhud", EntityOverlay.ENTITY_HUD);
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        MenuScreens.register(ContainerRegistry.RECIPE_MAKER.get(), RecipeMakerScreen::new);
    }
}
