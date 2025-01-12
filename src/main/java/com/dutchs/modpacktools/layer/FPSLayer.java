package com.dutchs.modpacktools.layer;

import com.dutchs.modpacktools.ConfigHandler;
import com.dutchs.modpacktools.Constants;
import com.dutchs.modpacktools.debug.HUDManager;
import com.dutchs.modpacktools.util.RenderUtil;
import com.google.common.collect.Maps;
import net.minecraft.Util;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.LayeredDraw;
import net.minecraft.client.gui.components.debugchart.FpsDebugChart;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.debugchart.LocalSampleLogger;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.*;

public class FPSLayer implements LayeredDraw.Layer {
    private final LocalSampleLogger frameTimeLogger = new LocalSampleLogger(1);
    private final FpsDebugChart fpsChart;
    private final Minecraft minecraft;

    public FPSLayer() {
        this.minecraft = Minecraft.getInstance();
        this.fpsChart = new FpsDebugChart(minecraft.font, this.frameTimeLogger);
        HUDManager.FPSHUD = this;
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, @NotNull DeltaTracker deltaTracker) {
        if(HUDManager.RENDERFPS) {
            int i = guiGraphics.guiWidth();
            int j = i / 2;
            this.fpsChart.drawChart(guiGraphics, 0, this.fpsChart.getWidth(j));
        }
    }

    public void logFrameDuration(long pFrameDuration) {
        this.frameTimeLogger.logSample(pFrameDuration);
    }
}