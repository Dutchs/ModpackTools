package com.dutchs.modpacktools.debug;

import com.dutchs.modpacktools.ConfigHandler;
import com.dutchs.modpacktools.util.RenderUtil;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.debug.DebugRenderer;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.core.SectionPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class ChunkDebugRender implements DebugRenderer.SimpleDebugRenderer {
    private double lastUpdateTime = Double.MIN_VALUE;

    @Nullable
    private ChunkData data, oldData;

    public void render(PoseStack pPoseStack, MultiBufferSource pBufferSource, double pCamX, double pCamY, double pCamZ) {
        Minecraft minecraft = Minecraft.getInstance();
        IntegratedServer integratedserver = minecraft.getSingleplayerServer();

        if (integratedserver != null) {
            FogRenderer.setupNoFog();

            double d0 = (double) Util.getMillis();
            if (d0 - this.lastUpdateTime > ConfigHandler.chunkRendererDelay) {
                this.lastUpdateTime = d0;

                LocalPlayer player = minecraft.player;
                if (player != null) {
                    this.oldData = data;
                    this.data = new ChunkData(integratedserver, pCamX, pCamZ, ConfigHandler.chunkRendererRadius, player.level.dimension());
                }
            }

            if (this.data != null) {
                RenderSystem.enableBlend();
                RenderSystem.defaultBlendFunc();
                RenderSystem.lineWidth(2.0F);
                RenderSystem.disableTexture();
                //RenderSystem.depthMask(false);
                //RenderSystem.disableDepthTest();
                Map<ChunkPos, String[]> map = this.data.serverData.getNow((Map<ChunkPos, String[]>) null);
                if (map == null && oldData != null) {
                    map = oldData.serverData.getNow((Map<ChunkPos, String[]>) null);
                }
                double d1 = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition().y * 0.85D;

                for (ChunkPos chunkpos : this.data.clientData) {
                    if (map != null) {
                        String[] s = map.get(chunkpos);
                        if (s != null) {
                            int i = 0;
                            for (String s1 : s) {
                                RenderUtil.floatingText(s1, (double) SectionPos.sectionToBlockCoord(chunkpos.x, 8), d1 + (double) i, (double) SectionPos.sectionToBlockCoord(chunkpos.z, 8), -1, 0.15F);
                                i -= 2;
                            }
                        }
                    }
                }

                //RenderSystem.depthMask(true);
                //RenderSystem.enableDepthTest();
                RenderSystem.enableTexture();
                RenderSystem.disableBlend();
            }
        } else {
            HUDManager.clearHUD();
        }
    }

    @OnlyIn(Dist.CLIENT)
    final class ChunkData {
        final ImmutableList<ChunkPos> clientData;
        final CompletableFuture<Map<ChunkPos, String[]>> serverData;

        ChunkData(IntegratedServer integratedServer, double x, double y, int radius, ResourceKey<Level> levelResourceKey) {
            int i = SectionPos.posToSectionCoord(x);
            int j = SectionPos.posToSectionCoord(y);
            ImmutableList.Builder<ChunkPos> clientPosBuilder = ImmutableList.builder();

            for (int k = i - radius; k <= i + radius; ++k) {
                for (int l = j - radius; l <= j + radius; ++l) {
                    clientPosBuilder.add(new ChunkPos(k, l));
                }
            }

            this.clientData = clientPosBuilder.build();
            this.serverData = integratedServer.submit(() -> {
                ServerLevel serverlevel = integratedServer.getLevel(levelResourceKey);
                if (serverlevel == null) {
                    return ImmutableMap.of();
                } else {
                    ImmutableMap.Builder<ChunkPos, String[]> builder1 = ImmutableMap.builder();
                    ServerChunkCache serverchunkcache = serverlevel.getChunkSource();

                    for (int i1 = i - radius; i1 <= i + radius; ++i1) {
                        for (int j1 = j - radius; j1 <= j + radius; ++j1) {
                            ChunkPos chunkpos = new ChunkPos(i1, j1);
                            builder1.put(chunkpos, serverchunkcache.getChunkDebugData(chunkpos).split("\n"));
                        }
                    }

                    return builder1.build();
                }
            });
        }
    }

}