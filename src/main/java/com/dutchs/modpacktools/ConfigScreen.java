package com.dutchs.modpacktools;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraftforge.client.gui.widget.ForgeSlider;

public class ConfigScreen extends Screen {
    private final Screen lastScreen;
    private ForgeSlider hudEntityDelay;
    private boolean autoCopyItems;
    private boolean includeNBT;
    private boolean clearContents;
    private boolean printAsJSON;

    public ConfigScreen(Screen last) {
        super(Component.literal(Constants.MODNAME + " Options"));
        this.lastScreen = last;
        this.autoCopyItems = ConfigHandler.autoCopyItems;
        this.includeNBT = ConfigHandler.includeNBT;
        this.clearContents = ConfigHandler.clearContents;
        this.printAsJSON = ConfigHandler.printAsJSON;
    }

    @Override
    protected void init() {
        int centreWidth = this.width / 2;
        int topAlign = this.height / 8;
        int optionTop = topAlign + 5;
        int defaultControlWidth = 150;
        int defaultWidthPadding = 15;
        int defaultControlHeight = 20;
        int defaultPadding = 5;

        //builder.push("HUD");
        this.addRenderableWidget(new StringWidget(centreWidth - defaultControlWidth, optionTop, defaultControlWidth, defaultControlHeight, Component.literal("Entity HUD refresh delay"), this.font));
        this.addRenderableWidget(hudEntityDelay = new ForgeSlider(centreWidth + defaultWidthPadding, optionTop, defaultControlWidth, defaultControlHeight, Component.empty(), Component.literal("s"), 0.1, 5.0, (double) ConfigHandler.hudEntityDelay / 1000, 0.05, 2, true));
        //builder.push("Behavior");
        this.addRenderableWidget(new StringWidget(centreWidth - defaultControlWidth, optionTop + defaultControlHeight + defaultPadding, defaultControlWidth, defaultControlHeight, Component.literal("Copy command output"), this.font));
        this.addRenderableWidget(CycleButton.onOffBuilder(this.autoCopyItems).create(centreWidth + defaultWidthPadding, optionTop + defaultControlHeight + defaultPadding, defaultControlWidth, defaultControlHeight,
                        Component.literal("autoCopyItems"), (button, value) -> this.autoCopyItems = value));
        //builder.push("Commands");
        this.addRenderableWidget(new StringWidget(centreWidth - defaultControlWidth, optionTop + (defaultControlHeight + defaultPadding) * 2, defaultControlWidth, defaultControlHeight, Component.literal("Include NBT by default"), this.font));
        this.addRenderableWidget(CycleButton.onOffBuilder(this.includeNBT).create(centreWidth + defaultWidthPadding, optionTop + (defaultControlHeight + defaultPadding) * 2, defaultControlWidth, defaultControlHeight,
                Component.literal("includeNBT"), (button, value) -> this.includeNBT = value));
        this.addRenderableWidget(new StringWidget(centreWidth - defaultControlWidth, optionTop + (defaultControlHeight + defaultPadding) * 3, defaultControlWidth, defaultControlHeight, Component.literal("Clear contents by default"), this.font));
        this.addRenderableWidget(CycleButton.onOffBuilder(this.clearContents).create(centreWidth + defaultWidthPadding, optionTop + (defaultControlHeight + defaultPadding) * 3, defaultControlWidth, defaultControlHeight,
                Component.literal("clearContents"), (button, value) -> this.clearContents = value));
        this.addRenderableWidget(new StringWidget(centreWidth - defaultControlWidth, optionTop + (defaultControlHeight + defaultPadding) * 4, defaultControlWidth, defaultControlHeight, Component.literal("Print as JSON by default"), this.font));
        this.addRenderableWidget(CycleButton.onOffBuilder(this.printAsJSON).create(centreWidth + defaultWidthPadding, optionTop + (defaultControlHeight + defaultPadding) * 4, defaultControlWidth, defaultControlHeight,
                Component.literal("printAsJSON"), (button, value) -> this.printAsJSON = value));

        int doneWidth = 75;
        this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, button -> this.onClose())
                .bounds((this.width - doneWidth) / 2, this.height - 27, doneWidth, 20).build());
    }

    @Override
    public void onClose() {
        ConfigHandler.assignAndSaveConfig((int) (hudEntityDelay.getValue() * 1000), this.autoCopyItems, this.includeNBT, this.clearContents, this.printAsJSON);
        this.minecraft.setScreen(this.lastScreen);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        //renderBackground has to be called manually here, it's not called in the superclass.
        this.renderBackground(guiGraphics);
        super.render(guiGraphics, pMouseX, pMouseY, pPartialTick);
        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 20, -1);
    }
}