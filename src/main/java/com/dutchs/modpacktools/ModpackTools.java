package com.dutchs.modpacktools;

import com.dutchs.modpacktools.client.ClientCommands;
import com.dutchs.modpacktools.client.SetupClient;
import com.dutchs.modpacktools.command.SetupCommands;
import com.dutchs.modpacktools.network.BlockPacket;
import com.dutchs.modpacktools.network.ClientPollForPacket;
import com.dutchs.modpacktools.network.NetworkManager;
import com.dutchs.modpacktools.server.ServerHandler;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(Constants.MODID)
public class ModpackTools {
    public static NetworkManager NETWORK;

    private static final Logger LOGGER = LogManager.getLogger(Constants.MODID);

    public static void logInfo(String msg) {
        LOGGER.info("[" + Constants.MODNAME + "] " + msg);
    }

    public ModpackTools() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, ConfigHandler.CLIENT_SPEC);
//        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, ConfigHandler.COMMON_SPEC);

        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> FMLJavaModLoadingContext.get().getModEventBus().addListener(SetupClient::init));
        MinecraftForge.EVENT_BUS.register(ClientCommands.class);

        MinecraftForge.EVENT_BUS.register(SetupCommands.class);
        MinecraftForge.EVENT_BUS.register(ServerHandler.class);

        NETWORK = new NetworkManager(Constants.MODID);
        NETWORK.registerPackets(BlockPacket.class, ClientPollForPacket.class);
    }
}
