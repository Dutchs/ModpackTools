package com.dutchs.modpacktools;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import org.apache.commons.lang3.tuple.Pair;

@Mod.EventBusSubscriber(modid = Constants.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ConfigHandler {
    static public final ClientConfig CLIENT_CONFIG;
    static public final ForgeConfigSpec CLIENT_SPEC;

    static {
        final Pair<ClientConfig, ForgeConfigSpec> clientSpecPair = new ForgeConfigSpec.Builder().configure(ClientConfig::new);
        CLIENT_CONFIG = clientSpecPair.getLeft();
        CLIENT_SPEC = clientSpecPair.getRight();
    }

    @SubscribeEvent
    public static void onModConfigEvent(final ModConfigEvent configEvent) {
        if (configEvent.getConfig().getSpec() == ConfigHandler.CLIENT_SPEC) {
            bakeClientConfig();
        }
    }

    //Client
    public static int chunkRendererDelay;
    public static int chunkRendererRadius;
    public static int hudEntityDelay;
    public static boolean autoCopyItems;

    public static void bakeClientConfig() {
        chunkRendererDelay = CLIENT_CONFIG.chunkRendererDelay.get();
        chunkRendererRadius = CLIENT_CONFIG.chunkRendererRadius.get();
        hudEntityDelay = CLIENT_CONFIG.hudEntityDelay.get();
        autoCopyItems = CLIENT_CONFIG.autoCopyItems.get();
    }

    public static class ClientConfig {
        public ForgeConfigSpec.IntValue chunkRendererDelay;
        public ForgeConfigSpec.IntValue chunkRendererRadius;
        public ForgeConfigSpec.IntValue hudEntityDelay;
        public ForgeConfigSpec.BooleanValue autoCopyItems;

        public ClientConfig(ForgeConfigSpec.Builder builder) {
            builder.push("HUD");
            chunkRendererDelay = builder.comment("Delay between chunk data refreshes")
                    .defineInRange("chunkRendererDelay", 500, 10, Integer.MAX_VALUE);
            chunkRendererRadius = builder.comment("Radius used for debug chunk rendering")
                    .defineInRange("chunkRendererRadius", 8, 1, Integer.MAX_VALUE);
            hudEntityDelay = builder.comment("Delay between entity HUD data refreshes")
                    .defineInRange("hudEntityDelay", 250, 10, Integer.MAX_VALUE);
            builder.pop();
            builder.push("Behavior");
            autoCopyItems = builder.comment("Automatically copy items to clipboard when running commands to list them (mt hand/hot/inv/blockinv)")
                    .define("autoCopyItems", true);
            builder.pop();
        }
    }
}
