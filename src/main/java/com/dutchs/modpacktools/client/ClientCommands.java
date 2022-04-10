package com.dutchs.modpacktools.client;

import com.dutchs.modpacktools.Constants;
import com.dutchs.modpacktools.ModpackTools;
import com.dutchs.modpacktools.command.DimensionResourceArgument;
import com.dutchs.modpacktools.debug.HUDManager;
import com.dutchs.modpacktools.network.BlockPacket;
import com.dutchs.modpacktools.network.EntityPacket;
import com.dutchs.modpacktools.network.InventoryPacket;
import com.dutchs.modpacktools.network.PrivilegedMessagePacket;
import com.dutchs.modpacktools.util.PlayerUtil;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.client.event.RegisterClientCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;

public class ClientCommands {

    @SubscribeEvent
    public static void registerClientCommands(RegisterClientCommandsEvent event) {
        LiteralCommandNode<CommandSourceStack> rootCommand = event.getDispatcher().register(
                Commands.literal(Constants.MODID)
                        .then(Commands.literal("hud")
                                .then(Commands.literal("fps").executes(ClientCommands::FPSHUD_Command))
                                .then(Commands.literal("clear").executes(ClientCommands::CLEARHUD_Command))
                                .then(Commands.literal("tps").executes(ClientCommands::TPSHUD_Command))
                                .then(Commands.literal("chunk").executes(ClientCommands::CHUNKHUD_Command))
                                .then(Commands.literal("entity").executes(ClientCommands::ENTITYHUD_Command))
                        )
                        .then(Commands.literal("entity").then(Commands.argument("entitytype", ResourceLocationArgument.id())
                                        .suggests((ctx, builder) -> SharedSuggestionProvider.suggest(ForgeRegistries.ENTITIES.getKeys().stream().map(ResourceLocation::toString), builder))
                                        .then(Commands.argument("dim", DimensionResourceArgument.dimensionResource())
                                                .executes(ctx -> ENTITY_Command(ctx, ctx.getArgument("entitytype", ResourceLocation.class), DimensionResourceArgument.getDimensionResource(ctx, "dim"), 10))
                                                .then(Commands.argument("limit", IntegerArgumentType.integer())
                                                        .executes(ctx -> ENTITY_Command(ctx, ctx.getArgument("entitytype", ResourceLocation.class), DimensionResourceArgument.getDimensionResource(ctx, "dim"), IntegerArgumentType.getInteger(ctx, "limit")))
                                                )
                                        )
                                        .executes(ctx -> ENTITY_Command(ctx, ctx.getArgument("entitytype", ResourceLocation.class), null, 10)))
                                .executes(ctx -> ENTITY_Command(ctx, null, null, 10))
                        )
                        .then(Commands.literal("hand").executes(ClientCommands::HAND_Command))
                        .then(Commands.literal("hot").executes(ClientCommands::HOT_Command))
                        .then(Commands.literal("inv").executes(ClientCommands::INV_Command))
                        .then(Commands.literal("block").executes(ClientCommands::BLOCK_Command))
                        .then(Commands.literal("blockinv").executes(ClientCommands::BLOCKINV_Command))
        );

        event.getDispatcher().register(Commands.literal("mt")
                .redirect(rootCommand));
    }

    private static int BLOCK_Command(CommandContext<CommandSourceStack> ctx) {
        HitResult hit = Minecraft.getInstance().hitResult;
        if ((hit != null ? hit.getType() : null) == HitResult.Type.BLOCK) {
            BlockPos pos = ((BlockHitResult) hit).getBlockPos();
            ModpackTools.NETWORK.toServer(new BlockPacket(pos, false));
        } else {
            ModpackTools.NETWORK.toServer(new PrivilegedMessagePacket("Not looking at valid block"));
        }

        return 0;
    }

    private static int BLOCKINV_Command(CommandContext<CommandSourceStack> ctx) {
        HitResult hit = Minecraft.getInstance().hitResult;
        if ((hit != null ? hit.getType() : null) == HitResult.Type.BLOCK) {
            BlockPos pos = ((BlockHitResult) hit).getBlockPos();
            ModpackTools.NETWORK.toServer(new BlockPacket(pos, true));
        } else {
            ModpackTools.NETWORK.toServer(new PrivilegedMessagePacket("Not looking at valid block"));
        }

        return 0;
    }

    private static int ENTITY_Command(CommandContext<CommandSourceStack> ctx, @Nullable ResourceLocation pType, @Nullable ResourceKey<Level> dim, int limit) {
        String type = pType == null ? "" : pType.toString();
        String dimension = dim == null ? "" : dim.location().toString();
        EntityPacket entityPacket = new EntityPacket(type, dimension, limit);
        ModpackTools.NETWORK.toServer(entityPacket);
        return 0;
    }

    private static int HAND_Command(CommandContext<CommandSourceStack> ctx) {
        ModpackTools.NETWORK.toServer(new InventoryPacket(InventoryPacket.InventoryType.Hand));
        return 0;
    }

    private static int HOT_Command(CommandContext<CommandSourceStack> ctx) {
        ModpackTools.NETWORK.toServer(new InventoryPacket(InventoryPacket.InventoryType.Hotbar));
        return 0;
    }

    private static int INV_Command(CommandContext<CommandSourceStack> ctx) {
        ModpackTools.NETWORK.toServer(new InventoryPacket(InventoryPacket.InventoryType.Inventory));
        return 0;
    }

    private static int TPSHUD_Command(CommandContext<CommandSourceStack> ctx) {
        if (Minecraft.getInstance().hasSingleplayerServer()) {
            HUDManager.RENDERTPS = !HUDManager.RENDERTPS;
        } else {
            PlayerUtil.sendClientMessage(new TextComponent(Constants.ERROR_FORMAT + "This command only works when on the IntegratedServer" + ChatFormatting.RESET));
        }

        return 0;
    }

    private static int FPSHUD_Command(CommandContext<CommandSourceStack> ctx) {
        HUDManager.RENDERFPS = !HUDManager.RENDERFPS;

        return 0;
    }

    private static int CHUNKHUD_Command(CommandContext<CommandSourceStack> ctx) {
        if (Minecraft.getInstance().hasSingleplayerServer()) {
            if (ctx.getSource().hasPermission(2)) {
                HUDManager.RENDERCHUNK = !HUDManager.RENDERCHUNK;
            } else {
                PlayerUtil.sendClientMessage(new TextComponent(Constants.ERROR_FORMAT + "You lack permissions to run this command" + ChatFormatting.RESET));
            }
        } else {
            PlayerUtil.sendClientMessage(new TextComponent(Constants.ERROR_FORMAT + "This command only works when on the IntegratedServer" + ChatFormatting.RESET));
        }

        return 0;
    }

    private static int ENTITYHUD_Command(CommandContext<CommandSourceStack> ctx) {
        if (Minecraft.getInstance().hasSingleplayerServer()) {
            if (ctx.getSource().hasPermission(2)) {
                HUDManager.RENDERENTITY = !HUDManager.RENDERENTITY;
            } else {
                PlayerUtil.sendClientMessage(new TextComponent(Constants.ERROR_FORMAT + "You lack permissions to run this command" + ChatFormatting.RESET));
            }
        } else {
            PlayerUtil.sendClientMessage(new TextComponent(Constants.ERROR_FORMAT + "This command only works when on the IntegratedServer" + ChatFormatting.RESET));
        }

        return 0;
    }

    private static int CLEARHUD_Command(CommandContext<CommandSourceStack> commandSourceStackCommandContext) {
        HUDManager.clearHUD();

        return 0;
    }
}
