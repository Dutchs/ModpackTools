package com.dutchs.modpacktools.util;

import net.minecraft.client.Minecraft;

public class ClipboardUtil {
    public static void copyToClipboard(String val) {
        Minecraft.getInstance().keyboardHandler.setClipboard(val);
    }
}
