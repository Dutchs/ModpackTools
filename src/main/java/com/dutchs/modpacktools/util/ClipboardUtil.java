package com.dutchs.modpacktools.util;

import com.dutchs.modpacktools.ConfigHandler;
import net.minecraft.client.Minecraft;

public class ClipboardUtil {
    public static void copyToClipboard(String val) {
        Minecraft.getInstance().keyboardHandler.setClipboard(val);
    }
}
