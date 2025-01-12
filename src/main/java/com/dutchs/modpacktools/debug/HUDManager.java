package com.dutchs.modpacktools.debug;

import com.dutchs.modpacktools.layer.EntityLayer;
import com.dutchs.modpacktools.layer.FPSLayer;
import com.dutchs.modpacktools.layer.TPSLayer;

public class HUDManager {
    public static boolean RENDERTPS = false;
    public static boolean RENDERFPS = false;
    public static boolean RENDERENTITY = false;

    public static EntityLayer ENTITYHUD;
    public static FPSLayer FPSHUD;
    public static TPSLayer TPSHUD;

    public static void clearHUD() {
        RENDERTPS = false;
        RENDERFPS = false;
        RENDERENTITY = false;
    }
}
