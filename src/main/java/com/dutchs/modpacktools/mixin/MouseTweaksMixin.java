package com.dutchs.modpacktools.mixin;

import com.dutchs.modpacktools.gui.RecipeMakerScreen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import yalter.mousetweaks.handlers.GuiContainerHandler;

@Mixin(GuiContainerHandler.class)
public class MouseTweaksMixin {
    @Shadow(remap = false)
    private AbstractContainerScreen<?> screen;

    public MouseTweaksMixin() {
    }

//    public boolean isWheelTweakDisabled() {
//        return this.screen.getClass().isAnnotationPresent(MouseTweaksDisableWheelTweak.class);
//    }
    @Inject(method = {"isWheelTweakDisabled"}, at = {@At("RETURN")}, remap = false, cancellable = true, require = 0)
    private void isWheelTweakDisabled_ModpackTools(CallbackInfoReturnable<Boolean> ci) {
        if(this.screen != null){
            if(this.screen instanceof RecipeMakerScreen){
                ci.setReturnValue(Boolean.TRUE);
            }
        }
    }
}


