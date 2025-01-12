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
    public static int hudEntityDelay;
    public static boolean autoCopyItems;
    public static boolean includeNBT;
    public static boolean clearContents;
    public static boolean printAsJSON;

    public static void bakeClientConfig() {
        ModpackTools.logInfo("Updating client config");
        hudEntityDelay = CLIENT_CONFIG.hudEntityDelay.get();
        autoCopyItems = CLIENT_CONFIG.autoCopyItems.get();
        includeNBT = CLIENT_CONFIG.includeNBT.get();
        clearContents = CLIENT_CONFIG.clearContents.get();
        printAsJSON = CLIENT_CONFIG.printAsJSON.get();
    }

    public static void assignAndSaveConfig(int entDelay, boolean autoCopy, boolean nbt, boolean clear, boolean json) {
        ConfigHandler.CLIENT_CONFIG.hudEntityDelay.set(entDelay);
        ConfigHandler.CLIENT_CONFIG.autoCopyItems.set(autoCopy);
        ConfigHandler.CLIENT_CONFIG.includeNBT.set(nbt);
        ConfigHandler.CLIENT_CONFIG.clearContents.set(clear);
        ConfigHandler.CLIENT_CONFIG.printAsJSON.set(json);
        //ModConfigEvent might or might not fire, hence we update it here manually.
        ConfigHandler.bakeClientConfig();
        ConfigHandler.CLIENT_SPEC.save();
    }

    public static class ClientConfig {
        public ForgeConfigSpec.IntValue hudEntityDelay;
        public ForgeConfigSpec.BooleanValue autoCopyItems;
        public ForgeConfigSpec.BooleanValue includeNBT;
        public ForgeConfigSpec.BooleanValue clearContents;
        public ForgeConfigSpec.BooleanValue printAsJSON;

        public ClientConfig(ForgeConfigSpec.Builder builder) {
            builder.push("HUD");
            hudEntityDelay = builder.comment("Delay between entity HUD refreshes, in milliseconds")
                    .defineInRange("hudEntityDelay", 250, 10, Integer.MAX_VALUE);
            builder.pop();
            builder.push("Behavior");
            autoCopyItems = builder.comment("Automatically copy data to clipboard when running commands that list them (mt hand/hot/inv/blockinv/etc)")
                    .define("autoCopyItems", true);
            builder.pop();
            builder.push("Commands");
            includeNBT = builder.comment("Default to including NBT if true, if false leave it out")
                    .define("includeNBT", true);
            clearContents = builder.comment("Default to removing contents of evaluated ItemStacks after running the command")
                    .define("clearContents", false);
            printAsJSON = builder.comment("Default to printing output as JSON array")
                    .define("printAsJSON", false);
            builder.pop();
        }
    }
}
