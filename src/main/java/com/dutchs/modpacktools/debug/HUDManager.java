package com.dutchs.modpacktools.debug;

public class HUDManager {
    public static boolean RENDERTPS = false;
    public static boolean RENDERFPS = false;
    public static boolean RENDERCHUNK = false;
    public static boolean RENDERENTITY = false;

    public static void clearHUD() {
        RENDERTPS = false;
        RENDERFPS = false;
        RENDERCHUNK = false;
        RENDERENTITY = false;
    }
}
