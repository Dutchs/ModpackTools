package com.dutchs.modpacktools.mixin;

import com.dutchs.modpacktools.ModpackTools;
import com.dutchs.modpacktools.debug.HUDManager;
import com.dutchs.modpacktools.layer.EntityLayer;
import com.dutchs.modpacktools.layer.FPSLayer;
import com.dutchs.modpacktools.layer.TPSLayer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.LayeredDraw;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner;
import net.minecraft.client.renderer.PanoramaRenderer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(value = Gui.class, priority = 1100)
public class GuiMixin {

    @Shadow @Final
    LayeredDraw layers;

    @Inject(method = "<init>(Lnet/minecraft/client/Minecraft;)V", at = @At("TAIL"), order = 1100)
    private void initGui(Minecraft pMinecraft, CallbackInfo ci) {
        layers.add(new EntityLayer());
        layers.add(new FPSLayer());
        layers.add(new TPSLayer(((Gui) (Object) this)));
    }
}
