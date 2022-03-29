package com.dutchs.modpacktools.command;

import com.dutchs.modpacktools.ModpackTools;
import com.dutchs.modpacktools.network.ClientPollForPacket;
import com.dutchs.modpacktools.util.CommandUtil;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;

public class CommandBlockInv implements Command<CommandSourceStack> {
    private static final CommandBlockInv CMD = new CommandBlockInv();

    public static ArgumentBuilder<CommandSourceStack, ?> register(CommandDispatcher<CommandSourceStack> dispatcher) {
        return Commands.literal("inv")
                .executes(CMD)
                .then(Commands.literal("NBT")
                        .executes(CMD));
    }

    @Override
    public int run(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer p = context.getSource().getPlayerOrException();
        ModpackTools.NETWORK.toPlayer(new ClientPollForPacket(CommandUtil.lastNodeEquals(context, "NBT") ? ClientPollForPacket.PollFor.BlockInvNBT : ClientPollForPacket.PollFor.BlockInv), p);
        return 0;
    }
}