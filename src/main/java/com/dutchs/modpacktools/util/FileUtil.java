package com.dutchs.modpacktools.util;

import net.minecraft.client.Minecraft;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class FileUtil {
    public static String getDumpFileName(String name) {
        String result = null;
        try {
            result = Minecraft.getInstance().gameDirectory.getCanonicalPath() + File.separator + name + ".log";
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    public static boolean writeToFile(String path, StringBuilder stringBuilder) {
        boolean success = true;
        File file = new File(path);
        BufferedWriter writer = null;
        try {
            try {
                writer = new BufferedWriter(new FileWriter(file));
                writer.append(stringBuilder);
            } finally {
                if (writer != null) writer.close();
            }
        } catch (IOException e) {
            success = false;
            e.printStackTrace();
        }
        return success;
    }
}
