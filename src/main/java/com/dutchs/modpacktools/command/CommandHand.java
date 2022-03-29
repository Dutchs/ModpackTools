package com.dutchs.modpacktools.command;

import com.dutchs.modpacktools.util.CommandUtil;
import com.dutchs.modpacktools.util.ComponentUtil;
import com.dutchs.modpacktools.util.ItemStackUtil;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;

public class CommandHand implements Command<CommandSourceStack> {

    private static final CommandHand CMD = new CommandHand();

    public static ArgumentBuilder<CommandSourceStack, ?> register(CommandDispatcher<CommandSourceStack> dispatcher) {
        return Commands.literal("hand")
                .executes(CMD)
                .then(Commands.literal("NBT")
                        .executes(CMD));
    }

    @Override
    public int run(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();

        String handStack = ItemStackUtil.ItemStackPrinter(player.getMainHandItem(), CommandUtil.lastNodeEquals(context, "NBT"), false);

        if (handStack != null) {
            context.getSource().sendSuccess(ComponentUtil.formatTitleContentWithCopy("Hand", handStack), true);
        } else {
            context.getSource().sendFailure(new TextComponent("Nothing to print").withStyle(ChatFormatting.DARK_RED));
        }

        return 0;
    }
}
