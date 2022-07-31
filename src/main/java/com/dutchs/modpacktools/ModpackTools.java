package com.dutchs.modpacktools;

import com.dutchs.modpacktools.client.ClientCommands;
import com.dutchs.modpacktools.client.KeyInputHandler;
import com.dutchs.modpacktools.client.SetupClient;
import com.dutchs.modpacktools.handler.ClientHandler;
import com.dutchs.modpacktools.network.*;
import com.dutchs.modpacktools.registry.ContainerRegistry;
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
        //ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, ConfigHandler.COMMON_SPEC);

        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> this::clientInit);

        MinecraftForge.EVENT_BUS.register(ServerHandler.class);

        NETWORK = new NetworkManager(Constants.MODID);
        NETWORK.registerPackets(BlockPacket.class, ClientBlockResultPacket.class, InventoryPacket.class, ClientInventoryResultPacket.class, EntityPacket.class, PrivilegedMessagePacket.class, RecipeMakerOpenGUIPacket.class, RecipeMakerActionPacket.class, ClientRecipeMakerResultPacket.class);
    }

    private void clientInit() {
        ContainerRegistry.init(FMLJavaModLoadingContext.get().getModEventBus());
        FMLJavaModLoadingContext.get().getModEventBus().register(SetupClient.class);
        MinecraftForge.EVENT_BUS.register(ClientCommands.class);
        MinecraftForge.EVENT_BUS.register(KeyInputHandler.class);
        MinecraftForge.EVENT_BUS.register(ClientHandler.class);
    }
}
