package com.dutchs.modpacktools.mixin;

import com.dutchs.modpacktools.debug.HUDManager;
import com.dutchs.modpacktools.layer.EntityLayer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.DebugScreenOverlay;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = DebugScreenOverlay.class)
public class DebugScreenOverlayMixin {
    @Inject(method = "logFrameDuration", at = @At("HEAD"))
    private void logFrameDurationMixin(long pFrameDuration, CallbackInfo ci) {
        HUDManager.FPSHUD.logFrameDuration(pFrameDuration);
    }
}
