package com.dutchs.modpacktools.util;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.context.ParsedCommandNode;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;

public class CommandUtil {
    @Nullable
    private static LiteralCommandNode getLastLiteralCommandNode(CommandContext<CommandSourceStack> context) {
        LiteralCommandNode result = null;

        List<ParsedCommandNode<CommandSourceStack>> nodes = context.getNodes();
        ParsedCommandNode<CommandSourceStack> commandSourceStackParsedCommandNode = nodes.get(nodes.size() - 1);
        CommandNode<CommandSourceStack> node = commandSourceStackParsedCommandNode.getNode();
        if (node instanceof LiteralCommandNode commandNode) {
            result = commandNode;
        }

        return result;
    }

    public static boolean lastNodeEquals(CommandContext<CommandSourceStack> context, String literal) {
        LiteralCommandNode lastNode = getLastLiteralCommandNode(context);
        if (lastNode != null) {
            return literal.equals(lastNode.getLiteral());
        } else {
            return false;
        }
    }

    public static String createTPCommandBetweenDimensions(String target, ResourceKey<Level> fromDim, ResourceKey<Level> toDim, BlockPos pos) {
        if(Objects.equals(fromDim, toDim)) {
            return String.format("/tp @p %s %s %s", pos.getX(), pos.getY(), pos.getZ());
        } else {
            return String.format("/execute in %s run tp @p %s %s %s", toDim.location(), pos.getX(), pos.getY(), pos.getZ());
        }
    }
}
