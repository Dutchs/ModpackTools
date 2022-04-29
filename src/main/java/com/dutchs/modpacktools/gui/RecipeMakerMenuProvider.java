package com.dutchs.modpacktools.gui;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.jetbrains.annotations.NotNull;

public class RecipeMakerMenuProvider implements MenuProvider {

    public RecipeMakerMenuProvider() {
    }

    @Override
    public @NotNull Component getDisplayName() {
        return new TextComponent("Recipe Maker");
    }

    @Override
    public AbstractContainerMenu createMenu(int i, @NotNull Inventory playerInventory, @NotNull Player player) {
        return new RecipeMakerMenu(i, playerInventory, player);
    }
}