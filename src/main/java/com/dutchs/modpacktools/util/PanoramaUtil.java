package com.dutchs.modpacktools.util;

import com.dutchs.modpacktools.ModpackTools;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.io.File;

public class PanoramaUtil {
    public Component grabPanoramixScreenshot(File pGameDirectory, int pRes) {
        Minecraft minecraft = Minecraft.getInstance();
        int pWidth = pRes;
        int pHeight = pRes;

        int i = minecraft.getWindow().getWidth();
        int j = minecraft.getWindow().getHeight();
        RenderTarget rendertarget = new TextureTarget(pWidth, pHeight, true, Minecraft.ON_OSX);
        float f = minecraft.player.getXRot();
        float f1 = minecraft.player.getYRot();
        float f2 = minecraft.player.xRotO;
        float f3 = minecraft.player.yRotO;
        minecraft.gameRenderer.setRenderBlockOutline(false);

        MutableComponent mutablecomponent;
        try {
            minecraft.gameRenderer.setPanoramicMode(true);
            minecraft.levelRenderer.graphicsChanged();
            minecraft.getWindow().setWidth(pWidth);
            minecraft.getWindow().setHeight(pHeight);

            for(int k = 0; k < 6; ++k) {
                switch (k) {
                    case 0:
                        minecraft.player.setYRot(f1);
                        minecraft.player.setXRot(0.0F);
                        break;
                    case 1:
                        minecraft.player.setYRot((f1 + 90.0F) % 360.0F);
                        minecraft.player.setXRot(0.0F);
                        break;
                    case 2:
                        minecraft.player.setYRot((f1 + 180.0F) % 360.0F);
                        minecraft.player.setXRot(0.0F);
                        break;
                    case 3:
                        minecraft.player.setYRot((f1 - 90.0F) % 360.0F);
                        minecraft.player.setXRot(0.0F);
                        break;
                    case 4:
                        minecraft.player.setYRot(f1);
                        minecraft.player.setXRot(-90.0F);
                        break;
                    case 5:
                    default:
                        minecraft.player.setYRot(f1);
                        minecraft.player.setXRot(90.0F);
                }

                minecraft.player.yRotO = minecraft.player.getYRot();
                minecraft.player.xRotO = minecraft.player.getXRot();
                rendertarget.bindWrite(true);
                minecraft.gameRenderer.renderLevel(1.0F, 0L, new PoseStack());

                try {
                    Thread.sleep(10L);
                } catch (InterruptedException var18) {
                }

                Screenshot.grab(pGameDirectory, "panorama_" + k + ".png", rendertarget, (p_231415_) -> {
                });
            }

            Component component = Component.literal(pGameDirectory.getName()).withStyle(ChatFormatting.UNDERLINE).withStyle((p_231426_) -> p_231426_.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_FILE, pGameDirectory.getAbsolutePath())));
            MutableComponent var13 = Component.translatable("screenshot.success", new Object[]{component});
            return var13;
        } catch (Exception exception) {
            ModpackTools.logError("Couldn't save image", exception);
            mutablecomponent = Component.translatable("screenshot.failure", new Object[]{exception.getMessage()});
        } finally {
            minecraft.player.setXRot(f);
            minecraft.player.setYRot(f1);
            minecraft.player.xRotO = f2;
            minecraft.player.yRotO = f3;
            minecraft.gameRenderer.setRenderBlockOutline(true);
            minecraft.getWindow().setWidth(i);
            minecraft.getWindow().setHeight(j);
            rendertarget.destroyBuffers();
            minecraft.gameRenderer.setPanoramicMode(false);
            minecraft.levelRenderer.graphicsChanged();
            minecraft.getMainRenderTarget().bindWrite(true);
        }

        return mutablecomponent;
    }
}
