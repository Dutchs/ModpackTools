package com.dutchs.modpacktools.util;

import com.dutchs.modpacktools.ModpackTools;
import com.dutchs.modpacktools.network.*;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.context.ParsedCommandNode;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

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
        if (Objects.equals(fromDim, toDim)) {
            return String.format("/tp %s %s %s %s", target, pos.getX(), pos.getY(), pos.getZ());
        } else {
            return String.format("/execute in %s run tp %s %s %s %s", toDim.location(), target, pos.getX(), pos.getY(), pos.getZ());
        }
    }

    public static void SendBlockCommand(boolean nbt){
        HitResult hit = Minecraft.getInstance().hitResult;
        if ((hit != null ? hit.getType() : null) == HitResult.Type.BLOCK) {
            BlockPos pos = ((BlockHitResult) hit).getBlockPos();
            ModpackTools.NETWORK.toServer(new BlockPacket(pos, false, nbt));
        } else {
            ModpackTools.NETWORK.toServer(new PrivilegedMessagePacket("Not looking at valid block"));
        }
    }

    public static void SendRecipeMakerCommand() {
        RecipeMakerOpenGUIPacket recipeMakerPacket = new RecipeMakerOpenGUIPacket();
        ModpackTools.NETWORK.toServer(recipeMakerPacket);
    }

    public static void SendBlockInvCommand(boolean nbt) {
        HitResult hit = Minecraft.getInstance().hitResult;
        if ((hit != null ? hit.getType() : null) == HitResult.Type.BLOCK) {
            BlockPos pos = ((BlockHitResult) hit).getBlockPos();
            ModpackTools.NETWORK.toServer(new BlockPacket(pos, true, nbt));
        } else {
            ModpackTools.NETWORK.toServer(new PrivilegedMessagePacket("Not looking at valid block"));
        }
    }

    public static void SendHandCommand(boolean nbt) {
        ModpackTools.NETWORK.toServer(new InventoryPacket(InventoryPacket.InventoryType.Hand, nbt));
    }

    public static void SendHotCommand(boolean nbt) {
        ModpackTools.NETWORK.toServer(new InventoryPacket(InventoryPacket.InventoryType.Hotbar, nbt));
    }

    public static void SendInvCommand(boolean nbt) {
        ModpackTools.NETWORK.toServer(new InventoryPacket(InventoryPacket.InventoryType.Inventory, nbt));
    }

    public static void SendEntityCommand(ResourceLocation pType, ResourceKey<Level> dim, int limit) {
        String type = pType == null ? "" : pType.toString();
        String dimension = dim == null ? "" : dim.location().toString();
        EntityPacket entityPacket = new EntityPacket(type, dimension, limit);
        ModpackTools.NETWORK.toServer(entityPacket);
    }
}
