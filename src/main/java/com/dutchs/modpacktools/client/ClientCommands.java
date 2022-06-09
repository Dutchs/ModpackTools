package com.dutchs.modpacktools.client;

import com.dutchs.modpacktools.Constants;
import com.dutchs.modpacktools.command.DimensionResourceArgument;
import com.dutchs.modpacktools.debug.HUDManager;
import com.dutchs.modpacktools.util.CommandUtil;
import com.dutchs.modpacktools.util.ComponentUtil;
import com.dutchs.modpacktools.util.FileUtil;
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
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.stats.StatType;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;
import net.minecraftforge.client.event.RegisterClientCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.*;

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
                        .then(Commands.literal("dump")
                                        .then(Commands.literal("all").executes(ClientCommands::ALLDUMP_Command))
                                        .then(Commands.literal("items").executes(ClientCommands::ITEMSDUMP_Command))
                                        .then(Commands.literal("blocks").executes(ClientCommands::BLOCKSDUMP_Command))
                                        //.then(Commands.literal("recipes").executes(ClientCommands::RECIPESDUMP_Command))
                                        .then(Commands.literal("enchants").executes(ClientCommands::ENCHANTSDUMP_Command))
                                        .then(Commands.literal("entities").executes(ClientCommands::ENTITIESDUMP_Command))
                                        .then(Commands.literal("attributes").executes(ClientCommands::ATTRIBUTESDUMP_COMMAND))
                                        .then(Commands.literal("biomes").executes(ClientCommands::BIOMESDUMP_COMMAND))
                                        .then(Commands.literal("features").executes(ClientCommands::FEATURESDUMP_COMMAND))
                                        .then(Commands.literal("fluids").executes(ClientCommands::FLUIDSDUMP_COMMAND))
                                        .then(Commands.literal("mob_effects").executes(ClientCommands::MOB_EFFECTSDUMP_COMMAND))
                                        //.then(Commands.literal("structure_features").executes(ClientCommands::STRUCTURE_FEATURESDUMP_COMMAND))
                                        .then(Commands.literal("stat_types").executes(ClientCommands::STAT_TYPESDUMP_COMMAND))
                                        .then(Commands.literal("objective_criteria").executes(ClientCommands::OBJECTIVE_CRITERIADUMP_COMMAND))
                                        .then(Commands.literal("mob_categories").executes(ClientCommands::MOB_CATEGORIESDUMP_COMMAND))
                                //.then(Commands.literal("world_types").executes(ClientCommands::WORLD_TYPESDUMP_COMMAND))
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
                        .then(Commands.literal("block").executes(ClientCommands::BLOCK_Command)
                                .then(Commands.literal("noNBT").executes((ctx) -> BLOCK_Command(ctx, false)))
                        )
                        .then(Commands.literal("blockinv").executes(ClientCommands::BLOCKINV_Command)
                                .then(Commands.literal("noNBT").executes((ctx) -> BLOCKINV_Command(ctx, false)))
                        )
                        .then(Commands.literal("hand").executes(ClientCommands::HAND_Command)
                                .then(Commands.literal("noNBT").executes((ctx) -> HAND_Command(ctx, false)))
                        )
                        .then(Commands.literal("hot").executes(ClientCommands::HOT_Command)
                                .then(Commands.literal("noNBT").executes((ctx) -> HOT_Command(ctx, false)))
                        )
                        .then(Commands.literal("inv").executes(ClientCommands::INV_Command)
                                .then(Commands.literal("noNBT").executes((ctx) -> INV_Command(ctx, false)))
                        )
                        .then(Commands.literal("recipemaker").executes(ClientCommands::RECIPEMAKER_Command)
                        )
        );

        event.getDispatcher().register(Commands.literal("mt")
                .redirect(rootCommand));
    }

    //================================
    //General
    //================================
    private static int ENTITY_Command(CommandContext<CommandSourceStack> ctx, @Nullable ResourceLocation pType, @Nullable ResourceKey<Level> dim, int limit) {
        CommandUtil.SendEntityCommand(pType, dim, limit);
        return 0;
    }

    private static int BLOCK_Command(CommandContext<CommandSourceStack> ctx) {
        return BLOCK_Command(ctx, true);
    }

    private static int BLOCK_Command(CommandContext<CommandSourceStack> ctx, boolean nbt) {
        CommandUtil.SendBlockCommand(nbt);
        return 0;
    }

    private static int RECIPEMAKER_Command(CommandContext<CommandSourceStack> ctx) {
        CommandUtil.SendRecipeMakerCommand();
        return 0;
    }

    //================================
    //Copy item id's
    //================================
    private static int BLOCKINV_Command(CommandContext<CommandSourceStack> ctx) {
        return BLOCKINV_Command(ctx, true);
    }

    private static int BLOCKINV_Command(CommandContext<CommandSourceStack> ctx, boolean nbt) {
        CommandUtil.SendBlockInvCommand(nbt);
        return 0;
    }

    private static int HAND_Command(CommandContext<CommandSourceStack> ctx) {
        return HAND_Command(ctx, true);
    }

    private static int HAND_Command(CommandContext<CommandSourceStack> ctx, boolean nbt) {
        CommandUtil.SendHandCommand(nbt);
        return 0;
    }

    private static int HOT_Command(CommandContext<CommandSourceStack> ctx) {
        return HOT_Command(ctx, true);
    }

    private static int HOT_Command(CommandContext<CommandSourceStack> ctx, boolean nbt) {
        CommandUtil.SendHotCommand(nbt);
        return 0;
    }

    private static int INV_Command(CommandContext<CommandSourceStack> ctx) {
        return INV_Command(ctx, true);
    }

    private static int INV_Command(CommandContext<CommandSourceStack> ctx, boolean nbt) {
        CommandUtil.SendInvCommand(nbt);
        return 0;
    }

    //================================
    //HUDs
    //================================
    private static int TPSHUD_Command(CommandContext<CommandSourceStack> ctx) {
        if (Minecraft.getInstance().hasSingleplayerServer()) {
            HUDManager.RENDERTPS = !HUDManager.RENDERTPS;
        } else {
            PlayerUtil.sendClientMessage(Component.literal(Constants.ERROR_FORMAT + "This command only works when on the IntegratedServer" + ChatFormatting.RESET));
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
                PlayerUtil.sendClientMessage(Component.literal(Constants.ERROR_FORMAT + "You lack permissions to run this command" + ChatFormatting.RESET));
            }
        } else {
            PlayerUtil.sendClientMessage(Component.literal(Constants.ERROR_FORMAT + "This command only works when on the IntegratedServer" + ChatFormatting.RESET));
        }

        return 0;
    }

    private static int ENTITYHUD_Command(CommandContext<CommandSourceStack> ctx) {
        if (Minecraft.getInstance().hasSingleplayerServer()) {
            if (ctx.getSource().hasPermission(2)) {
                HUDManager.RENDERENTITY = !HUDManager.RENDERENTITY;
            } else {
                PlayerUtil.sendClientMessage(Component.literal(Constants.ERROR_FORMAT + "You lack permissions to run this command" + ChatFormatting.RESET));
            }
        } else {
            PlayerUtil.sendClientMessage(Component.literal(Constants.ERROR_FORMAT + "This command only works when on the IntegratedServer" + ChatFormatting.RESET));
        }

        return 0;
    }

    private static int CLEARHUD_Command(CommandContext<CommandSourceStack> ctx) {
        HUDManager.clearHUD();

        return 0;
    }

    //================================
    //DUMP
    //================================
    private static int ALLDUMP_Command(CommandContext<CommandSourceStack> ctx) {
        ITEMSDUMP_Command(ctx);
        BLOCKSDUMP_Command(ctx);
        ENCHANTSDUMP_Command(ctx);
        ENTITIESDUMP_Command(ctx);
        ATTRIBUTESDUMP_COMMAND(ctx);
        BIOMESDUMP_COMMAND(ctx);
        FEATURESDUMP_COMMAND(ctx);
        FLUIDSDUMP_COMMAND(ctx);
        MOB_EFFECTSDUMP_COMMAND(ctx);
        //STRUCTURE_FEATURESDUMP_COMMAND(ctx);
        STAT_TYPESDUMP_COMMAND(ctx);
        OBJECTIVE_CRITERIADUMP_COMMAND(ctx);
        MOB_CATEGORIESDUMP_COMMAND(ctx);
        return 0;
    }

    private static int ITEMSDUMP_Command(CommandContext<CommandSourceStack> ctx) {
        StringBuilder builder = new StringBuilder();
        for (Item e : ForgeRegistries.ITEMS) {
            if (e == Items.AIR) continue;
            builder.append(ForgeRegistries.ITEMS.getKey(e).toString()).append(System.lineSeparator());
        }
        writeDumpFile("items", builder);
        return 0;
    }

    private static int BLOCKSDUMP_Command(CommandContext<CommandSourceStack> ctx) {
        StringBuilder builder = new StringBuilder();
        for (Block e : ForgeRegistries.BLOCKS) {
            if (e == Blocks.AIR) continue;
            builder.append(ForgeRegistries.BLOCKS.getKey(e).toString()).append(System.lineSeparator());
        }
        writeDumpFile("blocks", builder);
        return 0;
    }

//    private static int RECIPESDUMP_Command(CommandContext<CommandSourceStack> ctx) {
//        //TODO
//        return 0;
//    }

    private static int ENCHANTSDUMP_Command(CommandContext<CommandSourceStack> ctx) {
        StringBuilder builder = new StringBuilder();
        for (var e : ForgeRegistries.ENCHANTMENTS.getKeys()) {
            builder.append(e.toString()).append(System.lineSeparator());
        }
        writeDumpFile("enchantments", builder);
        return 0;
    }

    private static int ENTITIESDUMP_Command(CommandContext<CommandSourceStack> ctx) {
        StringBuilder builder = new StringBuilder();
        for (var e : ForgeRegistries.ENTITIES.getKeys()) {
            builder.append(e.toString()).append(System.lineSeparator());
        }
        writeDumpFile("entities", builder);
        return 0;
    }

    private static int ATTRIBUTESDUMP_COMMAND(CommandContext<CommandSourceStack> ctx) {
        StringBuilder builder = new StringBuilder();
        for (var e : ForgeRegistries.ATTRIBUTES.getKeys()) {
            builder.append(e.toString()).append(System.lineSeparator());
        }
        writeDumpFile("attributes", builder);
        return 0;
    }

    private static int BIOMESDUMP_COMMAND(CommandContext<CommandSourceStack> ctx) {
        StringBuilder builder = new StringBuilder();
        for (var e : ForgeRegistries.BIOMES.getKeys()) {
            builder.append(e.toString()).append(System.lineSeparator());
        }
        writeDumpFile("biomes", builder);
        return 0;
    }

    private static int FEATURESDUMP_COMMAND(CommandContext<CommandSourceStack> ctx) {
        StringBuilder builder = new StringBuilder();
        for (var e : ForgeRegistries.FEATURES.getKeys()) {
            builder.append(e.toString()).append(System.lineSeparator());
        }
        writeDumpFile("features", builder);
        return 0;
    }

    private static int FLUIDSDUMP_COMMAND(CommandContext<CommandSourceStack> ctx) {
        StringBuilder builder = new StringBuilder();
        for (Fluid e : ForgeRegistries.FLUIDS) {
            if (e == Fluids.EMPTY)
                continue;
            builder.append(ForgeRegistries.FLUIDS.getKey(e).toString()).append(System.lineSeparator());
        }
        writeDumpFile("fluids", builder);
        return 0;
    }

    private static int MOB_EFFECTSDUMP_COMMAND(CommandContext<CommandSourceStack> ctx) {
        StringBuilder builder = new StringBuilder();
        for (var e : ForgeRegistries.MOB_EFFECTS.getKeys()) {
            builder.append(e.toString()).append(System.lineSeparator());
        }
        writeDumpFile("mob_effects", builder);
        return 0;
    }

//    private static int STRUCTURE_FEATURESDUMP_COMMAND(CommandContext<CommandSourceStack> ctx) {
//        //TODO
//        StringBuilder builder = new StringBuilder();
//        for (StructureFeature<?> e : ForgeRegistries.STRUCTURE_FEATURES) {
//            builder.append(e.getRegistryName().toString()).append(System.lineSeparator());
//        }
//        writeDumpFile("structure_features", builder);
//        return 0;
//    }

    private static int STAT_TYPESDUMP_COMMAND(CommandContext<CommandSourceStack> ctx) {
        StringBuilder builder = new StringBuilder();
        ResourceLocation minecraftCustom = new ResourceLocation("custom");
        for (StatType<?> e : ForgeRegistries.STAT_TYPES) {
            ResourceLocation statResource = ForgeRegistries.STAT_TYPES.getKey(e);
            if (statResource.equals(minecraftCustom)) {

                ArrayList<String> customStats = new ArrayList<>();
                e.forEach(e2 -> {
                    customStats.add(e2.getName());
                });
                customStats.sort(Comparator.naturalOrder());

                for (String s : customStats) {
                    builder.append(s).append(System.lineSeparator());
                }

            } else {
                builder.append(statResource.getNamespace() + "." + statResource.getPath() + ":*").append(System.lineSeparator());
            }

        }
        writeDumpFile("stat_types", builder);
        return 0;
    }

    private static int OBJECTIVE_CRITERIADUMP_COMMAND(CommandContext<CommandSourceStack> ctx) {
        StringBuilder builder = new StringBuilder();
        for (String e : ObjectiveCriteria.getCustomCriteriaNames().stream().sorted().toList()) {
            builder.append(e).append(System.lineSeparator());
        }
        writeDumpFile("objective_criteria", builder);
        return 0;
    }

    private static int MOB_CATEGORIESDUMP_COMMAND(CommandContext<CommandSourceStack> ctx) {
        TreeMap<String, List<String>> cats = new TreeMap<>();
        for (EntityType<?> e : ForgeRegistries.ENTITIES) {
            cats.compute(e.getCategory().getName(), (k, v) -> {
                if (v == null) {
                    v = new ArrayList<>();
                }

                v.add(ForgeRegistries.ENTITIES.getKey(e).toString());
                //v.add(e.getRegistryName().toString());

                return v;
            });
        }

        StringBuilder builder = new StringBuilder();
        for (Map.Entry<String, List<String>> e : cats.entrySet()) {
            builder.append(e.getKey()).append(System.lineSeparator());
            for (var v : e.getValue().stream().sorted().toList()) {
                builder.append("\t").append(v).append(System.lineSeparator());
            }
        }
        writeDumpFile("mob_categories", builder);
        return 0;
    }

//    private static int WORLD_TYPESDUMP_COMMAND(CommandContext<CommandSourceStack> ctx) {
//        StringBuilder builder = new StringBuilder();
//        for (ForgeWorldPreset e : ForgeRegistries.WORLD_TYPES) {
//            builder.append(e.getRegistryName().toString()).append(System.lineSeparator());
//        }
//        writeDumpFile("world_types", builder);
//        return 0;
//    }

    private static void writeDumpFile(String name, StringBuilder stringBuilder) {
        boolean result = false;
        String path = FileUtil.getDumpFileName(name + "_dump");
        if (path != null) {
            result = FileUtil.writeToFile(path, stringBuilder);
        }
        if (result) {
            PlayerUtil.sendClientMessage(ComponentUtil.formatTitleContent("Dumping: " + name, ComponentUtil.withOpenFile(Component.literal(path).withStyle(Constants.OPEN_FILE_FORMAT), path)));
        } else {
            PlayerUtil.sendClientMessage(ComponentUtil.formatTitleContent("Dumping: " + name, Component.literal("Failed").withStyle(Constants.ERROR_FORMAT)));
        }
    }
}