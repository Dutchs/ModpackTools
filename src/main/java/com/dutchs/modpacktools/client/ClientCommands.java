package com.dutchs.modpacktools.client;

import com.dutchs.modpacktools.Constants;
import com.dutchs.modpacktools.debug.HUDManager;
import com.dutchs.modpacktools.util.PlayerUtil;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.TextComponent;
import net.minecraftforge.client.event.RegisterClientCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class ClientCommands {
    @SubscribeEvent
    public static void registerClientCommands(RegisterClientCommandsEvent event) {
        LiteralCommandNode<CommandSourceStack> rootCommand = event.getDispatcher().register(
                Commands.literal(Constants.MODID + "hud")
                        .requires(cs -> cs.hasPermission(2))
                        .then(Commands.literal("tps").executes(ClientCommands::TPS_Command))
                        .then(Commands.literal("chunk").executes(ClientCommands::CHUNK_Command))
                        .then(Commands.literal("entity").executes(ClientCommands::ENTITY_Command))
                        .then(Commands.literal("clear").executes(ClientCommands::CLEAR_Command)));

        event.getDispatcher().register(Commands.literal("mth")
                .requires(cs -> cs.hasPermission(2))
                .redirect(rootCommand));
    }

    private static int TPS_Command(CommandContext<CommandSourceStack> ctx) {
        if (Minecraft.getInstance().hasSingleplayerServer())
            HUDManager.RENDERTPS = !HUDManager.RENDERTPS;
        else
            PlayerUtil.sendClientMessage(new TextComponent(Constants.ERROR_FORMAT + "This command only works when on the IntegratedServer" + ChatFormatting.RESET));

        return 0;
    }

    private static int CHUNK_Command(CommandContext<CommandSourceStack> ctx) {
        if (Minecraft.getInstance().hasSingleplayerServer())
            HUDManager.RENDERCHUNK = !HUDManager.RENDERCHUNK;
        else
            PlayerUtil.sendClientMessage(new TextComponent(Constants.ERROR_FORMAT + "This command only works on using the IntegratedServer" + ChatFormatting.RESET));

        return 0;
    }

    private static int ENTITY_Command(CommandContext<CommandSourceStack> ctx) {
        if (Minecraft.getInstance().hasSingleplayerServer())
            HUDManager.RENDERENTITY = !HUDManager.RENDERENTITY;
        else
            PlayerUtil.sendClientMessage(new TextComponent(Constants.ERROR_FORMAT + "This command only works when on the IntegratedServer" + ChatFormatting.RESET));

        return 0;
    }

    private static int CLEAR_Command(CommandContext<CommandSourceStack> commandSourceStackCommandContext) {
        if (Minecraft.getInstance().hasSingleplayerServer()) {
            HUDManager.clearHUD();
        } else {
            PlayerUtil.sendClientMessage(new TextComponent(Constants.ERROR_FORMAT + "This command only works when on the IntegratedServer" + ChatFormatting.RESET));
        }

        return 0;
    }
}
