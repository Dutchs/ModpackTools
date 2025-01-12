package com.dutchs.modpacktools.client;

import com.dutchs.modpacktools.debug.EntityOverlay;
import com.dutchs.modpacktools.debug.FPSOverlay;
import com.dutchs.modpacktools.debug.TPSOverlay;
import com.dutchs.modpacktools.gui.RecipeMakerScreen;
import com.dutchs.modpacktools.registry.ContainerRegistry;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

public class SetupClient {

    @SubscribeEvent
    public static void OnRegisterGuiOverlaysEvent(RegisterGuiOverlaysEvent event) {
        event.registerAboveAll("tpshud", TPSOverlay.TPS_HUD);
        event.registerAboveAll( "fpshud", FPSOverlay.FPS_HUD);
        event.registerAboveAll("entityhud", EntityOverlay.ENTITY_HUD);
    }

    @SubscribeEvent
    public static void onRegisterKeyMappingsEvent(RegisterKeyMappingsEvent event) {
        KeyBinds.init(event);
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() ->
                MenuScreens.register(ContainerRegistry.RECIPE_MAKER.get(), RecipeMakerScreen::new));
    }
}
