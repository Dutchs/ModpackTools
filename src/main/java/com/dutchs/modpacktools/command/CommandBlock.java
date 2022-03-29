package com.dutchs.modpacktools.command;

import com.dutchs.modpacktools.ModpackTools;
import com.dutchs.modpacktools.network.ClientPollForPacket;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;

public class CommandBlock implements Command<CommandSourceStack> {
    private static final CommandBlock CMD = new CommandBlock();

    public static ArgumentBuilder<CommandSourceStack, ?> register(CommandDispatcher<CommandSourceStack> dispatcher) {
        return Commands.literal("block")
                .executes(CMD)
                .then(CommandBlockInv.register(dispatcher));
    }

    @Override
    public int run(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer p = context.getSource().getPlayerOrException();
        ModpackTools.NETWORK.toPlayer(new ClientPollForPacket(ClientPollForPacket.PollFor.Block), p);
        return 0;
    }
}
