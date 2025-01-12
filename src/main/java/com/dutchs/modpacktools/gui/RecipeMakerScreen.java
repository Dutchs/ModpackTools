package com.dutchs.modpacktools.gui;

import com.dutchs.modpacktools.Constants;
import com.dutchs.modpacktools.ModpackTools;
import com.dutchs.modpacktools.network.RecipeMakerActionPacket;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

public class RecipeMakerScreen extends AbstractContainerScreen<RecipeMakerMenu> {
    private static final ResourceLocation CRAFTING_TABLE_LOCATION = ResourceLocation.withDefaultNamespace("textures/gui/container/crafting_table.png");

    private boolean shiftDown;
    private Button shapedButton;
    private Button shapelessButton;
    private Button clearButton;

    public RecipeMakerScreen(RecipeMakerMenu screenContainer, Inventory inv, Component titleIn) {
        super(screenContainer, inv, titleIn);
    }

    @Override
    public void render(@NotNull GuiGraphics ms, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(ms, mouseX, mouseY, partialTicks);
        super.render(ms, mouseX, mouseY, partialTicks);
        this.renderTooltip(ms, mouseX, mouseY);
    }

    @Override
    protected void renderTooltip(@NotNull GuiGraphics pGuiGraphics, int pX, int pY) {
        boolean currentShift = hasShiftDown();
        if(shiftDown != currentShift){
            shiftDown = currentShift;
            if(shiftDown){
                clearButton.setTooltip(Tooltip.create(Component.literal("Clear Everything").withStyle(Constants.ERROR_FORMAT)));
            } else {
                clearButton.setTooltip(Tooltip.create(Component.literal("Clear Recipe")));
            }
        }

        if(this.menu.getCarried().isEmpty() && this.menu.craftSlotsContainSlot(this.hoveredSlot) && this.hoveredSlot.hasItem()){
            int i = this.menu.getIndexerForCraftSlot(this.hoveredSlot);
            ItemStack itemStack = this.hoveredSlot.getItem();
            boolean shiftDown = hasShiftDown();

            List<Component> list = itemStack.getTooltipLines(Item.TooltipContext.of(minecraft.level), minecraft.player, minecraft.options.advancedItemTooltips ? TooltipFlag.Default.ADVANCED : TooltipFlag.Default.NORMAL);
            list.add(Component.literal("Item: " + ForgeRegistries.ITEMS.getKey(itemStack.getItem()).toString()).withStyle(i == -1 ? (shiftDown ? ChatFormatting.RED : ChatFormatting.GOLD) : ChatFormatting.GRAY));
            int tagIndex = 0;
            for (TagKey<Item> tag : this.hoveredSlot.getItem().getItemHolder().getTagKeys().toList()) {
                list.add(Component.literal("Tag: " + tag.location().toString()).withStyle(i == tagIndex ? (shiftDown ? ChatFormatting.RED : ChatFormatting.GOLD) : ChatFormatting.GRAY));
                tagIndex++;
            }
            pGuiGraphics.renderTooltip(this.font, list, java.util.Optional.empty(), pX, pY);
        }
        else {
            super.renderTooltip(pGuiGraphics, pX, pY);
        }
    }

    @Override
    public void init() {
        super.init();
        int spacing = 1;

        int xStartCraft = leftPos + 110;
        int yStartCraft = topPos + 60;
        int widthCraft = 20;
        int heightCraft = 13;

        int xStartClear = leftPos + 91;
        int yStartClear = topPos + 5;
        int widthClear = 40;
        int heightClear = 13;

        Button.Builder shaped = Button.builder(Component.literal("S"), (pButton) ->
        {
            int[] indexerArr = Arrays.stream(this.menu.getInputIndexers().toArray()).mapToInt(t -> (int)t).toArray();
            RecipeMakerActionPacket action = new RecipeMakerActionPacket(RecipeMakerActionPacket.RecipeMakerActionType.Shaped, indexerArr);
            ModpackTools.NETWORK.toServer(action);
        }).bounds(xStartCraft, yStartCraft, widthCraft, heightCraft).tooltip(Tooltip.create(Component.literal("Shaped")));
        this.addRenderableWidget(shapedButton = shaped.build());

        Button.Builder shapeless = Button.builder(Component.literal("SL"), (pButton) ->
        {
            int[] indexerArr = Arrays.stream(this.menu.getInputIndexers().toArray()).mapToInt(t -> (int)t).toArray();
            RecipeMakerActionPacket action = new RecipeMakerActionPacket(RecipeMakerActionPacket.RecipeMakerActionType.Shapeless, indexerArr);
            ModpackTools.NETWORK.toServer(action);
        }).bounds(xStartCraft + widthCraft + spacing, yStartCraft, widthCraft, heightCraft).tooltip(Tooltip.create(Component.literal("Shapeless")));
        this.addRenderableWidget(shapelessButton = shapeless.build());

        Button.Builder clear = Button.builder(Component.literal("Clear"), (pButton) ->
        {
            RecipeMakerActionPacket.RecipeMakerActionType type = RecipeMakerActionPacket.RecipeMakerActionType.ClearRecipe;
            if(hasShiftDown()){
                type = RecipeMakerActionPacket.RecipeMakerActionType.ClearEverything;
            }
            RecipeMakerActionPacket action = new RecipeMakerActionPacket(type, new int[0]);
            ModpackTools.NETWORK.toServer(action);
        }).bounds(xStartClear + widthClear, yStartClear, widthClear, heightClear).tooltip(Tooltip.create(Component.literal("Clear Recipe")));
        this.addRenderableWidget(clearButton = clear.build());
    }

    @Override
    protected void renderBg(@NotNull GuiGraphics pStack, float partialTicks, int x, int y) {
        int relX = (this.width - this.imageWidth) / 2;
        int relY = (this.height - this.imageHeight) / 2;
        pStack.blit(CRAFTING_TABLE_LOCATION, relX, relY, 0, 0, this.imageWidth, this.imageHeight);
    }

    @Override
    public boolean mouseScrolled(double pMouseX, double pMouseY, double pScrollX, double pScrollY) {
        if(this.menu.getCarried().isEmpty() && this.menu.craftSlotsContainSlot(this.hoveredSlot) && this.hoveredSlot.hasItem()){
            int totalLength = 1 + (int) this.hoveredSlot.getItem().getItemHolder().getTagKeys().count();
            int currentIndex = this.menu.getIndexerForCraftSlot(this.hoveredSlot);
            int newIndex = currentIndex;
            if(pScrollY > 0) { //Scroll up
                newIndex = currentIndex - 1;
                if(newIndex < -1) {
                    newIndex = totalLength - 2;
                }
            } else if(pScrollY < 0) { //Scroll down
                newIndex = currentIndex + 1;
                if(newIndex > totalLength - 2) {
                    newIndex = -1;
                }
            }
            if(newIndex != currentIndex) {
                if(hasShiftDown()) {
                    this.menu.setIndexerForAllMatchingCraftSlots(this.hoveredSlot, newIndex);
                } else {
                    this.menu.setIndexerForCraftSlot(this.hoveredSlot, newIndex);
                }
            }
            return true;
        }
        else {
            return super.mouseScrolled(pMouseX, pMouseY, pScrollX, pScrollY);
        }
    }
}