package com.dutchs.modpacktools.debug;

import com.dutchs.modpacktools.util.RenderUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.server.IntegratedServer;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;

public class TPSOverlay {
    public static final IGuiOverlay TPS_HUD = (gui, pPoseStack, partialTicks, width, height) -> {
        if (HUDManager.RENDERTPS) {
            IntegratedServer integratedserver = Minecraft.getInstance().getSingleplayerServer();
            if (integratedserver != null) {
                RenderUtil.drawTPSOverlay(pPoseStack, integratedserver.getFrameTimer());
            } else {
                HUDManager.clearHUD();
            }
        }
    };
}
