package com.dutchs.modpacktools.util;

import com.dutchs.modpacktools.Constants;
import com.dutchs.modpacktools.ModpackTools;
import com.dutchs.modpacktools.debug.GCManager;
import com.dutchs.modpacktools.debug.GCTimer;
import com.dutchs.modpacktools.server.ServerHandler;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Matrix4f;
import com.mojang.math.Transformation;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FrameTimer;
import net.minecraft.util.Mth;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Collection;
import java.util.List;

public class RenderUtil {
    public static void floatingText(String pText, double pX, double pY, double pZ, int pColor, float pScale) {
        floatingText(pText, pX, pY, pZ, pColor, pScale, true, 0.0F);
    }

    private static void floatingText(String pText, double pX, double pY, double pZ, int pColor, float pScale, boolean pCenter, float pXOffset) {
        Minecraft minecraft = Minecraft.getInstance();
        Camera camera = minecraft.gameRenderer.getMainCamera();
        if (camera.isInitialized() && minecraft.getEntityRenderDispatcher().options != null) {
            Font font = minecraft.font;
            double d0 = camera.getPosition().x;
            double d1 = camera.getPosition().y;
            double d2 = camera.getPosition().z;
            PoseStack posestack = RenderSystem.getModelViewStack();
            posestack.pushPose();
            posestack.translate((double) ((float) (pX - d0)), (double) ((float) (pY - d1) + 0.07F), (double) ((float) (pZ - d2)));
            posestack.mulPoseMatrix(new Matrix4f(camera.rotation()));
            posestack.scale(pScale, -pScale, pScale);
            RenderSystem.enableTexture();
            //RenderSystem.disableDepthTest();
            //RenderSystem.depthMask(true);
            posestack.scale(-1.0F, 1.0F, 1.0F);
            RenderSystem.applyModelViewMatrix();
            float f = pCenter ? (float) (-font.width(pText)) / 2.0F : 0.0F;
            f -= pXOffset / pScale;
            MultiBufferSource.BufferSource multibuffersource$buffersource = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());
            font.drawInBatch(pText, f, 0.0F, pColor, false, Transformation.identity().getMatrix(), multibuffersource$buffersource, false, 0, 15728880);
            multibuffersource$buffersource.endBatch();
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            //RenderSystem.enableDepthTest();
            posestack.popPose();
            RenderSystem.applyModelViewMatrix();
        }
    }

    public static void drawTPSOverlay(PoseStack pPoseStack, FrameTimer frameTimer) {
        Minecraft minecraft = Minecraft.getInstance();
        Font font = minecraft.font;
        GCManager gcManager = ServerHandler.GC_MANAGER;

        int screenWidth = minecraft.getWindow().getGuiScaledWidth();
        int halfScreenWidth = screenWidth / 2;
        int hudLeft = screenWidth - Math.min(halfScreenWidth, 240);
        int hudBottom = minecraft.getWindow().getGuiScaledHeight() - Constants.CHAT_HEIGHT;
        int hudHeight = 60;

        RenderSystem.disableDepthTest();

        int logStart = frameTimer.getLogStart();
        int logEnd = frameTimer.getLogEnd();
        long[] tpsLog = frameTimer.getLog();
        int tHUDLeft = hudLeft;
        int i1 = Math.max(0, tpsLog.length - halfScreenWidth);
        int j1 = tpsLog.length - i1;
        int frameIndex = frameTimer.wrapIndex(logStart + i1);
        long k1 = 0L;
        int msMin = Integer.MAX_VALUE;
        int msMax = Integer.MIN_VALUE;

        for (int j2 = 0; j2 < j1; ++j2) {
            int k2 = (int) (tpsLog[frameTimer.wrapIndex(frameIndex + j2)] / 1000000L);
            msMin = Math.min(msMin, k2);
            msMax = Math.max(msMax, k2);
            k1 += k2;
        }

        GuiComponent.fill(pPoseStack, hudLeft, hudBottom - hudHeight, hudLeft + j1, hudBottom, -1873784752);
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        BufferBuilder bufferbuilder = Tesselator.getInstance().getBuilder();
        RenderSystem.enableBlend();
        RenderSystem.disableTexture();
        RenderSystem.defaultBlendFunc();
        bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

        for (Matrix4f matrix4f = Transformation.identity().getMatrix(); frameIndex != logEnd; frameIndex = frameTimer.wrapIndex(frameIndex + 1)) {
            int l2 = frameTimer.scaleSampleTo(tpsLog[frameIndex], 60, 20);
            int i3 = 60;
            int j3 = getSampleColor(Mth.clamp(l2, 0, i3), 0, i3 / 2, i3);
            int a = j3 >> 24 & 255;
            int r = j3 >> 16 & 255;
            int g = j3 >> 8 & 255;
            int b = j3 & 255;
            bufferbuilder.vertex(matrix4f, (float) (tHUDLeft + 1), (float) hudBottom, 0.0F).color(r, g, b, a).endVertex();
            bufferbuilder.vertex(matrix4f, (float) (tHUDLeft + 1), (float) (hudBottom - l2 + 1), 0.0F).color(r, g, b, a).endVertex();
            bufferbuilder.vertex(matrix4f, (float) tHUDLeft, (float) (hudBottom - l2 + 1), 0.0F).color(r, g, b, a).endVertex();
            bufferbuilder.vertex(matrix4f, (float) tHUDLeft, (float) hudBottom, 0.0F).color(r, g, b, a).endVertex();
            ++tHUDLeft;
        }

        Collection<GCTimer> timers = gcManager.getTimers();
        for (GCTimer timer : timers) {
            tHUDLeft = hudLeft;
            long[] gcLog = timer.getLog();
            int gcI = Math.max(0, gcLog.length - halfScreenWidth);
            int gcStart = timer.getLogStart();
            int gcEnd = timer.getLogEnd();
            int gcIndex = timer.wrapIndex(gcStart + gcI);
            for (Matrix4f matrix4f = Transformation.identity().getMatrix(); gcIndex != gcEnd; gcIndex = timer.wrapIndex(gcIndex + 1)) {
                long gc = gcLog[gcIndex];
                if (gc > 0) {
                    int l2 = timer.scaleSampleTo(gc, 60, 20);
                    int i3 = 60;
                    int j3 = getSampleColor(Mth.clamp(l2, 0, i3), 0, i3 / 2, i3);
                    int a = j3 >> 24 & 255;
                    int r = j3 >> 16 & 255;
                    int g = j3 >> 8 & 255;
                    int b = j3 & 255;
                    bufferbuilder.vertex(matrix4f, (float) tHUDLeft, (float) (hudBottom + 4), 0.0F).color(r, g, b, a).endVertex();
                    bufferbuilder.vertex(matrix4f, (float) (tHUDLeft + 2), (float) (hudBottom + 2), 0.0F).color(r, g, b, a).endVertex();
                    bufferbuilder.vertex(matrix4f, (float) tHUDLeft, (float) hudBottom, 0.0F).color(r, g, b, a).endVertex();
                    bufferbuilder.vertex(matrix4f, (float) (tHUDLeft - 2), (float) (hudBottom + 2), 0.0F).color(r, g, b, a).endVertex();
                }
                ++tHUDLeft;
            }
        }

        bufferbuilder.end();
        BufferUploader.end(bufferbuilder);
        RenderSystem.enableTexture();
        RenderSystem.disableBlend();

        GuiComponent.fill(pPoseStack, hudLeft + 1, hudBottom - 30 + 1, hudLeft + 14, hudBottom - 30 + 10, -1873784752);
        font.draw(pPoseStack, "25 ms", (float) (hudLeft + 2), (float) (hudBottom - 30 + 2), 14737632);
        hLine(pPoseStack, hudLeft, hudLeft + j1 - 1, hudBottom - 30, -1);
        GuiComponent.fill(pPoseStack, hudLeft + 1, hudBottom - 60 + 1, hudLeft + 14, hudBottom - 60 + 10, -1873784752);
        font.draw(pPoseStack, "50 ms", (float) (hudLeft + 2), (float) (hudBottom - 60 + 2), 14737632);
        hLine(pPoseStack, hudLeft, hudLeft + j1 - 1, hudBottom - 60, -1);

        hLine(pPoseStack, hudLeft, hudLeft + j1 - 1, hudBottom - 1, -1);
        vLine(pPoseStack, hudLeft, hudBottom - 60, hudBottom, -1);
        vLine(pPoseStack, hudLeft + j1 - 1, hudBottom - 60, hudBottom, -1);

        String s1 = k1 / (long) j1 + " ms avg";
        String s2 = msMax + " ms max";
        font.drawShadow(pPoseStack, msMin + " ms min", (float) (hudLeft + 2), (float) (hudBottom - 60 - 9), 14737632);
        font.drawShadow(pPoseStack, s1, (float) (hudLeft + j1 / 2 - font.width(s1) / 2), (float) (hudBottom - 60 - 9), 14737632);
        font.drawShadow(pPoseStack, s2, (float) (hudLeft + j1 - font.width(s2)), (float) (hudBottom - 60 - 9), 14737632);

        RenderSystem.enableDepthTest();
    }

    private static int getSampleColor(int pHeight, int pHeightMin, int pHeightMid, int pHeightMax) {
        return pHeight < pHeightMid ? colorLerp(-16711936, -256, (float) pHeight / (float) pHeightMid) : colorLerp(-256, -65536, (float) (pHeight - pHeightMid) / (float) (pHeightMax - pHeightMid));
    }

    private static int colorLerp(int pCol1, int pCol2, float pFactor) {
        int i = pCol1 >> 24 & 255;
        int j = pCol1 >> 16 & 255;
        int k = pCol1 >> 8 & 255;
        int l = pCol1 & 255;
        int i1 = pCol2 >> 24 & 255;
        int j1 = pCol2 >> 16 & 255;
        int k1 = pCol2 >> 8 & 255;
        int l1 = pCol2 & 255;
        int i2 = Mth.clamp((int) Mth.lerp(pFactor, (float) i, (float) i1), 0, 255);
        int j2 = Mth.clamp((int) Mth.lerp(pFactor, (float) j, (float) j1), 0, 255);
        int k2 = Mth.clamp((int) Mth.lerp(pFactor, (float) k, (float) k1), 0, 255);
        int l2 = Mth.clamp((int) Mth.lerp(pFactor, (float) l, (float) l1), 0, 255);
        return i2 << 24 | j2 << 16 | k2 << 8 | l2;
    }

    private static void hLine(PoseStack pPoseStack, int pMinX, int pMaxX, int pY, int pColor) {
        if (pMaxX < pMinX) {
            int i = pMinX;
            pMinX = pMaxX;
            pMaxX = i;
        }

        GuiComponent.fill(pPoseStack, pMinX, pY, pMaxX + 1, pY + 1, pColor);
    }

    private static void vLine(PoseStack pPoseStack, int pX, int pMinY, int pMaxY, int pColor) {
        if (pMaxY < pMinY) {
            int i = pMinY;
            pMinY = pMaxY;
            pMaxY = i;
        }

        GuiComponent.fill(pPoseStack, pX, pMinY + 1, pX + 1, pMaxY, pColor);
    }

    public static void drawEntityOverlay(PoseStack pPoseStack, List<Pair<ResourceLocation, Integer>> entityInfo, int entityCount, String dimensionID, int width, int height) {
        Font font = Minecraft.getInstance().font;
        RenderSystem.disableDepthTest();

        GuiComponent.fill(pPoseStack, 0, 0, width, height, -1873784752);
        font.draw(pPoseStack, String.format("Entities: %d - %s", entityCount, dimensionID), 2f, 2F, -1);
        for (int i = 0; i < entityInfo.size(); i++) {
            Pair<ResourceLocation, Integer> e = entityInfo.get(i);
            font.draw(pPoseStack, e.getValue() + ": " + e.getKey(), 2f, font.lineHeight * i + font.lineHeight + 2f, -1);
        }

        hLine(pPoseStack, 0, width - 1, 1, -1);
        hLine(pPoseStack, 0, width - 1, height - 1, -1);
        vLine(pPoseStack, 0, 1, height - 1, -1);
        vLine(pPoseStack, width - 1, 1, height - 1, -1);

        RenderSystem.enableDepthTest();
    }
}
