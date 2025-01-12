package com.dutchs.modpacktools.util;

import com.dutchs.modpacktools.Constants;
import com.dutchs.modpacktools.debug.GCManager;
import com.dutchs.modpacktools.debug.GCTimer;
import com.dutchs.modpacktools.debug.HUDManager;
import com.dutchs.modpacktools.server.ServerHandler;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Transformation;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FrameTimer;
import net.minecraft.util.Mth;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;

public class RenderUtil {

    public static void drawTPSOverlay(GuiGraphics pGuiGraphics, FrameTimer pTickLogging) {
        Minecraft minecraft = Minecraft.getInstance();
        Font font = minecraft.font;
        GCManager gcManager = ServerHandler.GC_MANAGER;

        int screenWidth = minecraft.getWindow().getGuiScaledWidth();
        int halfScreenWidth = screenWidth / 2;
        int hudLeft = screenWidth - Math.min(halfScreenWidth, 240);
        int hudHeight = 60;
        int hudBottom = minecraft.getWindow().getGuiScaledHeight() - Constants.CHAT_HEIGHT;

        int logStart = pTickLogging.getLogStart();
        int logEnd = pTickLogging.getLogEnd();
        long[] along = pTickLogging.getLog();
        int l = hudLeft;
        int i1 = Math.max(0, along.length - halfScreenWidth);
        int j1 = along.length - i1;
        int logIndex = pTickLogging.wrapIndex(logStart + i1);
        long total = 0L;
        int min = Integer.MAX_VALUE;
        int max = Integer.MIN_VALUE;

        for(int j2 = 0; j2 < j1; ++j2) {
            int k2 = (int)(along[pTickLogging.wrapIndex(logIndex + j2)] / 1000000L);
            min = Math.min(min, k2);
            max = Math.max(max, k2);
            total += (long)k2;
        }

        pGuiGraphics.fill(RenderType.guiOverlay(), hudLeft, hudBottom - 60, hudLeft + j1, hudBottom, -1873784752);

        //TPS graph
        while(logIndex != logEnd) {
            int scaled = pTickLogging.scaleSampleTo(along[logIndex], 60, 20);
            int l2 = 60;
            int sampleColor = getSampleColor(Mth.clamp(scaled, 0, l2), 0, l2 / 2, l2);
            int clamped = Mth.clamp(scaled, 0, hudHeight + 5);
            pGuiGraphics.fill(RenderType.guiOverlay(), l, hudBottom - clamped, l + 1, hudBottom, sampleColor);
            ++l;
            logIndex = pTickLogging.wrapIndex(logIndex + 1);
        }

        //GCollection graph
        Collection<GCTimer> timers = gcManager.getTimers();
        for (GCTimer timer : timers) {
            int tHUDLeft = hudLeft;
            long[] gcLog = timer.getLog();
            int gcI = Math.max(0, gcLog.length - halfScreenWidth);
            int gcStart = timer.getLogStart();
            int gcEnd = timer.getLogEnd();
            int gcIndex = timer.wrapIndex(gcStart + gcI);

            while(gcIndex != gcEnd) {
                if(gcLog[gcIndex] > 0)
                {
                    pGuiGraphics.fill(RenderType.guiOverlay(), tHUDLeft - 1, hudBottom - 1, tHUDLeft + 3, hudBottom + 3, -5242636);
                }
                ++tHUDLeft;
                gcIndex = timer.wrapIndex(gcIndex + 1);
            }
        }

        pGuiGraphics.fill(RenderType.guiOverlay(), hudLeft + 1, hudBottom - 60 + 1, hudLeft + 14, hudBottom - 60 + 10, -1873784752);
        pGuiGraphics.drawString(font, "20 TPS", hudLeft + 2, hudBottom - 60 + 2, 14737632, true);
        pGuiGraphics.hLine(RenderType.guiOverlay(), hudLeft, hudLeft + j1 - 1, hudBottom - 60, -1);

        pGuiGraphics.hLine(RenderType.guiOverlay(), hudLeft, hudLeft + j1 - 1, hudBottom - 1, -1);
        pGuiGraphics.vLine(RenderType.guiOverlay(), hudLeft, hudBottom - 60, hudBottom, -1);
        pGuiGraphics.vLine(RenderType.guiOverlay(), hudLeft + j1 - 1, hudBottom - 60, hudBottom, -1);

        String s1 = min + " ms min";
        String s2 = total / (long)j1 + " ms avg";
        String s = max + " ms max";
        pGuiGraphics.drawString(font, s1, hudLeft + 2, hudBottom - 60 - 9, 14737632);
        pGuiGraphics.drawCenteredString(font, s2, hudLeft + j1 / 2, hudBottom - 60 - 9, 14737632);
        pGuiGraphics.drawString(font, s, hudLeft + j1 - font.width(s), hudBottom - 60 - 9, 14737632);
    }

    public static void drawFPSOverlay(GuiGraphics pGuiGraphics) {
        Minecraft minecraft = Minecraft.getInstance();
        Font font = minecraft.font;
        FrameTimer frameTimer = minecraft.getFrameTimer();

        int screenWidth = minecraft.getWindow().getGuiScaledWidth();
        int halfScreenWidth = screenWidth / 2;
        int hudLeft = screenWidth - Math.min(halfScreenWidth, 240);
        int hudHeight = 60;
        int hudBottom = minecraft.getWindow().getGuiScaledHeight() - (Constants.CHAT_HEIGHT + (HUDManager.RENDERTPS ? hudHeight + 12 : 0));

        int i = frameTimer.getLogStart();
        int j = frameTimer.getLogEnd();
        long[] frameLog = frameTimer.getLog();
        int l = hudLeft;
        int i1 = Math.max(0, frameLog.length - halfScreenWidth);
        int j1 = frameLog.length - i1;
        int $$8 = frameTimer.wrapIndex(i + i1);
        long total = 0L;
        int min = Integer.MAX_VALUE;
        int max = Integer.MIN_VALUE;

        for(int j2 = 0; j2 < j1; ++j2) {
            int k2 = (int)(frameLog[frameTimer.wrapIndex($$8 + j2)] / 1000000L);
            min = Math.min(min, k2);
            max = Math.max(max, k2);
            total += (long)k2;
        }

        pGuiGraphics.fill(RenderType.guiOverlay(), hudLeft, hudBottom - 60, hudLeft + j1, hudBottom, -1873784752);

        while($$8 != j) {
            int scaled = frameTimer.scaleSampleTo(frameLog[$$8], 30, 60);
            int l2 = 100;
            int sampleColor = getSampleColor(Mth.clamp(scaled, 0, l2), 0, l2 / 2, l2);
            int clamped = Mth.clamp(scaled, 0, hudHeight + 5);
            pGuiGraphics.fill(RenderType.guiOverlay(), l, hudBottom - clamped, l + 1, hudBottom, sampleColor);
            ++l;
            $$8 = frameTimer.wrapIndex($$8 + 1);
        }

        pGuiGraphics.fill(RenderType.guiOverlay(), hudLeft + 1, hudBottom - 30 + 1, hudLeft + 14, hudBottom - 30 + 10, -1873784752);
        pGuiGraphics.drawString(font, "60 FPS", hudLeft + 2, hudBottom - 30 + 2, 14737632, true);
        pGuiGraphics.hLine(RenderType.guiOverlay(), hudLeft, hudLeft + j1 - 1, hudBottom - 30, -1);
        pGuiGraphics.fill(RenderType.guiOverlay(), hudLeft + 1, hudBottom - 60 + 1, hudLeft + 14, hudBottom - 60 + 10, -1873784752);
        pGuiGraphics.drawString(font, "30 FPS", hudLeft + 2, hudBottom - 60 + 2, 14737632, true);
        pGuiGraphics.hLine(RenderType.guiOverlay(), hudLeft, hudLeft + j1 - 1, hudBottom - 60, -1);

        pGuiGraphics.hLine(RenderType.guiOverlay(), hudLeft, hudLeft + j1 - 1, hudBottom - 1, -1);
        pGuiGraphics.vLine(RenderType.guiOverlay(), hudLeft, hudBottom - 60, hudBottom, -1);
        pGuiGraphics.vLine(RenderType.guiOverlay(), hudLeft + j1 - 1, hudBottom - 60, hudBottom, -1);
        int l3 = (Integer)minecraft.options.framerateLimit().get();
        if (l3 > 0 && l3 <= 250) {
            pGuiGraphics.hLine(RenderType.guiOverlay(), hudLeft, hudLeft + j1 - 1, hudBottom - 1 - (int)((double)1800.0F / (double)l3), -16711681);
        }

        String s1 = min + " ms min";
        String s2 = total / (long)j1 + " ms avg";
        String s = max + " ms max";
        pGuiGraphics.drawString(font, s1, hudLeft + 2, hudBottom - 60 - 9, 14737632);
        pGuiGraphics.drawCenteredString(font, s2, hudLeft + j1 / 2, hudBottom - 60 - 9, 14737632);
        pGuiGraphics.drawString(font, s, hudLeft + j1 - font.width(s), hudBottom - 60 - 9, 14737632);
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

    private static void hLine(GuiGraphics pGraphics, int pMinX, int pMaxX, int pY, int pColor) {
        if (pMaxX < pMinX) {
            int i = pMinX;
            pMinX = pMaxX;
            pMaxX = i;
        }

        pGraphics.fill(pMinX, pY, pMaxX + 1, pY + 1, pColor);
    }

    private static void vLine(GuiGraphics pGraphics, int pX, int pMinY, int pMaxY, int pColor) {
        if (pMaxY < pMinY) {
            int i = pMinY;
            pMinY = pMaxY;
            pMaxY = i;
        }

        pGraphics.fill(pX, pMinY + 1, pX + 1, pMaxY, pColor);
    }

    public static void drawEntityOverlay(GuiGraphics pGraphics, @Nullable List<Pair<ResourceLocation, Integer>> entityInfo, int entityCount, String dimensionID, int width, int height) {
        Font font = Minecraft.getInstance().font;
        RenderSystem.disableDepthTest();

        pGraphics.fill(0, 0, width, height, -1873784752);
//        font.draw(pGraphics, String.format("Entities: %d - %s", entityCount, dimensionID), 2f, 2F, -1);
        pGraphics.drawString(font, String.format("Entities: %d - %s", entityCount, dimensionID), 2, 2, -1);
        if(entityInfo != null){
            for (int i = 0; i < entityInfo.size(); i++) {
                Pair<ResourceLocation, Integer> e = entityInfo.get(i);
                pGraphics.drawString(font, e.getValue() + ": " + e.getKey(), 2, font.lineHeight * i + font.lineHeight + 2, -1);
                //font.draw(pGraphics, e.getValue() + ": " + e.getKey(), 2f, font.lineHeight * i + font.lineHeight + 2f, -1);
            }
        }

        hLine(pGraphics, 0, width - 1, 1, -1);
        hLine(pGraphics, 0, width - 1, height - 1, -1);
        vLine(pGraphics, 0, 1, height - 1, -1);
        vLine(pGraphics, width - 1, 1, height - 1, -1);

        RenderSystem.enableDepthTest();
    }
}
