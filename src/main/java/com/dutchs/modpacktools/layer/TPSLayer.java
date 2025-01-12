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
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.LayeredDraw;
import net.minecraft.client.gui.components.debugchart.TpsDebugChart;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.debugchart.LocalSampleLogger;
import net.minecraft.util.debugchart.TpsDebugDimensions;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.*;

public class TPSLayer implements LayeredDraw.Layer {
    //private final LocalSampleLogger tickTimeLogger = new LocalSampleLogger(TpsDebugDimensions.values().length);
    private final TpsDebugChart tpsChart;
    private final Minecraft minecraft;

    public TPSLayer(Gui gui){
        HUDManager.TPSHUD = this;
        this.minecraft = Minecraft.getInstance();
        this.tpsChart = new TpsDebugChart(minecraft.font, gui.getDebugOverlay().getTickTimeLogger(), () ->
        {
            ClientLevel level = minecraft.level;
            if(level != null) {
                return level.tickRateManager().millisecondsPerTick();
            }
            else {
                return 0.0f;
            }
        });
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, @NotNull DeltaTracker deltaTracker) {
        if(HUDManager.RENDERTPS) {
            if (this.minecraft.getDebugOverlay().getTickTimeLogger().size() > 0) {
                int i = guiGraphics.guiWidth();
                int j = i / 2;
                int k = this.tpsChart.getWidth(j);
                this.tpsChart.drawChart(guiGraphics, i - k, k);
            }
        }
    }
}