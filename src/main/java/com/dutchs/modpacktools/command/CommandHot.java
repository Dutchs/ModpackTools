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

public class CommandHot implements Command<CommandSourceStack> {

    private static final CommandHot CMD = new CommandHot();

    public static ArgumentBuilder<CommandSourceStack, ?> register(CommandDispatcher<CommandSourceStack> dispatcher) {
        return Commands.literal("hot")
                .executes(CMD)
                .then(Commands.literal("NBT")
                        .executes(CMD));
    }

    @Override
    public int run(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();

        String itemStacks = ItemStackUtil.ItemStackPrinter(player.getInventory().items.subList(0, 9), CommandUtil.lastNodeEquals(context, "NBT"), false);

        if (itemStacks != null) {
            context.getSource().sendSuccess(ComponentUtil.formatTitleContentWithCopy("Hotbar", itemStacks), true);
        } else {
            context.getSource().sendFailure(new TextComponent("Nothing to print").withStyle(ChatFormatting.DARK_RED));
        }

        return 0;
    }
}