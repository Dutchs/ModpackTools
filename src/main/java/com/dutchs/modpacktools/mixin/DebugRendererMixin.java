package com.dutchs.modpacktools.mixin;

import com.dutchs.modpacktools.debug.ChunkDebugRender;
import com.dutchs.modpacktools.debug.HUDManager;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.debug.DebugRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DebugRenderer.class)
public class DebugRendererMixin {
    private static final ChunkDebugRender chunkRender = new ChunkDebugRender();

    @Inject(method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource$BufferSource;DDD)V", at = @At(value = "TAIL"), remap = true, require = 1)
    public void render(PoseStack pPoseStack, MultiBufferSource.BufferSource pBufferSource, double pCamX, double pCamY, double pCamZ, CallbackInfo info) {
        if (HUDManager.RENDERCHUNK) {
            chunkRender.render(pPoseStack, pBufferSource, pCamX, pCamY, pCamZ);
        }
    }
}
