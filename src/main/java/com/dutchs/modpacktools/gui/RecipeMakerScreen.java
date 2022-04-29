package com.dutchs.modpacktools.gui;

import com.dutchs.modpacktools.ModpackTools;
import com.dutchs.modpacktools.network.RecipeMakerActionPacket;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;

public class RecipeMakerScreen extends AbstractContainerScreen<RecipeMakerMenu> {
    private static final ResourceLocation CRAFTING_TABLE_LOCATION = new ResourceLocation("textures/gui/container/crafting_table.png");

    public RecipeMakerScreen(RecipeMakerMenu screenContainer, Inventory inv, Component titleIn) {
        super(screenContainer, inv, titleIn);
    }

    @Override
    public void render(@NotNull PoseStack ms, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(ms);
        super.render(ms, mouseX, mouseY, partialTicks);
        this.renderTooltip(ms, mouseX, mouseY);
    }

    @Override
    public void init() {
        super.init();
        int xStart = leftPos + 100;
        int yStart = topPos + 60;
        int width = 20;
        int height = 12;
        int spacing = 1;
        this.addRenderableWidget(new Button(xStart, yStart, width, height, new TextComponent("S"), (pButton) ->
        {
            RecipeMakerActionPacket action = new RecipeMakerActionPacket(RecipeMakerActionPacket.RecipeMakerActionType.Shaped);
            ModpackTools.NETWORK.toServer(action);
        }, (pButton, pPoseStack, pMouseX, pMouseY) ->
        {
            this.renderTooltip(pPoseStack, new TextComponent("Shaped"), pMouseX, pMouseY);
        }));
        this.addRenderableWidget(new Button(xStart + width + spacing, yStart, width, height, new TextComponent("SL"), (pButton) ->
        {
            RecipeMakerActionPacket action = new RecipeMakerActionPacket(RecipeMakerActionPacket.RecipeMakerActionType.Shapeless);
            ModpackTools.NETWORK.toServer(action);
        }, (pButton, pPoseStack, pMouseX, pMouseY) ->
        {
            this.renderTooltip(pPoseStack, new TextComponent("Shapeless"), pMouseX, pMouseY);
        }));
    }

    @Override
    protected void renderBg(@NotNull PoseStack pStack, float partialTicks, int x, int y) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, CRAFTING_TABLE_LOCATION);
        int relX = (this.width - this.imageWidth) / 2;
        int relY = (this.height - this.imageHeight) / 2;
        this.blit(pStack, relX, relY, 0, 0, this.imageWidth, this.imageHeight);
    }
}