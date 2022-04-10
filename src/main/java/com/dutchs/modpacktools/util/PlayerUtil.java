package com.dutchs.modpacktools.util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;

public class PlayerUtil {
    public static void sendClientMessage(Component component) {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        if (player != null) {
            minecraft.gui.getChat().addMessage(component);
        }
    }
}