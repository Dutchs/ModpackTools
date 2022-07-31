package com.dutchs.modpacktools.debug;

import com.dutchs.modpacktools.ConfigHandler;
import com.dutchs.modpacktools.Constants;
import com.dutchs.modpacktools.util.RenderUtil;
import com.google.common.collect.Maps;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;
import java.util.*;

public class EntityOverlay {
    @Nullable
    private static List<Pair<ResourceLocation, Integer>> entityInfo;
    private static int entityCount = -1;
    private static String dimensionID;
    private static double lastUpdateTime = Double.MIN_VALUE;
    private static double lastScaleFactor = Double.MIN_VALUE;
    private static int overlayWidth = -1;
    private static int overlayHeight = -1;

    public static final IGuiOverlay ENTITY_HUD = (gui, pPoseStack, partialTicks, width, height) -> {
        if (HUDManager.RENDERENTITY) {
            Minecraft minecraft = Minecraft.getInstance();
            IntegratedServer integratedServer = minecraft.getSingleplayerServer();

            if (integratedServer != null) {
                double scaleFactor = minecraft.getWindow().getGuiScale();
                if (Double.compare(lastScaleFactor, scaleFactor) != 0) {
                    lastScaleFactor = scaleFactor;
                    overlayWidth = Math.min(calculateOverlayWidth(minecraft.font), 240);
                    overlayHeight = minecraft.getWindow().getGuiScaledHeight() - Constants.CHAT_HEIGHT;
                }

                double d0 = (double) Util.getMillis();
                if (entityInfo == null || d0 - lastUpdateTime > ConfigHandler.hudEntityDelay) {
                    lastUpdateTime = d0;
                    Set<ResourceLocation> names = ForgeRegistries.ENTITY_TYPES.getKeys();
                    LocalPlayer player = minecraft.player;
                    if (player != null) {
                        ResourceKey<Level> dimension = player.getLevel().dimension();
                        ServerLevel world = integratedServer.getLevel(dimension);
                        if (world != null) {
                            dimensionID = dimension.location().toString();
                            Map<ResourceLocation, MutablePair<Integer, Map<ChunkPos, Integer>>> list = Maps.newHashMap();
                            world.getEntities().getAll().forEach(e -> {
                                MutablePair<Integer, Map<ChunkPos, Integer>> info = list.computeIfAbsent(ForgeRegistries.ENTITY_TYPES.getKey(e.getType()), k -> MutablePair.of(0, Maps.newHashMap()));
                                ChunkPos chunk = new ChunkPos(e.blockPosition());
                                info.left++;
                                info.right.put(chunk, info.right.getOrDefault(chunk, 0) + 1);
                            });

                            entityInfo = new ArrayList<>();
                            list.forEach((key, value) -> {
                                if (names.contains(key)) {
                                    Pair<ResourceLocation, Integer> of = Pair.of(key, value.left);
                                    entityInfo.add(of);
                                }
                            });
                            entityInfo.sort((a, b) -> {
                                if (Objects.equals(a.getRight(), b.getRight()))
                                    return a.getKey().toString().compareTo(b.getKey().toString());
                                else
                                    return b.getRight() - a.getRight();
                            });
                            entityCount = entityInfo.stream().mapToInt(Pair::getRight).sum();
                        }
                    }
                }
                RenderUtil.drawEntityOverlay(pPoseStack, entityInfo, entityCount, dimensionID, overlayWidth, overlayHeight);
            } else {
                HUDManager.clearHUD();
            }
        }
    };

    private static int calculateOverlayWidth(Font font) {
        int result = -1;
        Set<ResourceLocation> names = ForgeRegistries.ENTITY_TYPES.getKeys();
        for (ResourceLocation name : names) {
            int width = font.width("1234: " + name.toString());
            if (width > result)
                result = width;
        }
        return result;
    }
}