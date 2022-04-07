package com.dutchs.modpacktools.debug;

import com.dutchs.modpacktools.util.RenderUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.server.IntegratedServer;
import net.minecraftforge.client.gui.IIngameOverlay;

public class FPSOverlay {
    public static final IIngameOverlay FPS_HUD = (gui, pPoseStack, partialTicks, width, height) -> {
        if (HUDManager.RENDERFPS) {
            RenderUtil.drawFPSOverlay(pPoseStack);
        }
    };
}
