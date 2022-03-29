package com.dutchs.modpacktools.command;

import com.dutchs.modpacktools.Constants;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.commands.CommandSourceStack;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;


public class SetupCommands {

    @SubscribeEvent
    public static void registerCommands(RegisterCommandsEvent event) {
        register(event.getDispatcher());
    }

    private static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        LiteralCommandNode<CommandSourceStack> rootCommand = dispatcher.register(
                net.minecraft.commands.Commands.literal(Constants.MODID)
                        .requires(cs -> cs.hasPermission(2))
                        .then(CommandHand.register(dispatcher))
                        .then(CommandHot.register(dispatcher))
                        .then(CommandInv.register(dispatcher))
                        .then(CommandBlock.register(dispatcher))
                        //.then(CommandConfig.register(dispatcher))
                        .then(CommandEntity.register(dispatcher))
        );

        dispatcher.register(net.minecraft.commands.Commands.literal("mt")
                .requires(cs -> cs.hasPermission(2))
                .redirect(rootCommand));
    }
}
