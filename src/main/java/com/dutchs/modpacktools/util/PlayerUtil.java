package com.dutchs.modpacktools.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.TextComponent;

public class PlayerUtil {

    public static void sendClientMessage(TextComponent textComponent) {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        if (player != null) {
            minecraft.gui.getChat().addMessage(textComponent);
        }
    }
}
