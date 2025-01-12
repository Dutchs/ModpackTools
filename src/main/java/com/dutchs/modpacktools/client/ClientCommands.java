package com.dutchs.modpacktools.client;

import com.dutchs.modpacktools.ConfigHandler;
import com.dutchs.modpacktools.Constants;
import com.dutchs.modpacktools.command.TagKeyArgument;
import com.dutchs.modpacktools.debug.HUDManager;
import com.dutchs.modpacktools.util.CommandUtil;
import com.dutchs.modpacktools.util.ComponentUtil;
import com.dutchs.modpacktools.util.FileUtil;
import com.dutchs.modpacktools.util.PlayerUtil;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.stats.StatType;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerType;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;
import net.minecraftforge.client.event.RegisterClientCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.tags.ITag;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.*;

public class ClientCommands {

    @SubscribeEvent
    public static void registerClientCommands(RegisterClientCommandsEvent event) {
        LiteralCommandNode<CommandSourceStack> rootCommand = event.getDispatcher().register(
                Commands.literal("mt")
                        .then(Commands.literal("hud")
                                .then(Commands.literal("fps").executes(ClientCommands::FPSHUD_Command))
                                .then(Commands.literal("clear").executes(ClientCommands::CLEARHUD_Command))
                                .then(Commands.literal("tps").executes(ClientCommands::TPSHUD_Command))
                                .then(Commands.literal("entity").executes(ClientCommands::ENTITYHUD_Command))
                        )
                        .then(Commands.literal("dump")
                                        //.then(Commands.literal("advancements").executes(ClientCommands::ADVANCEMENTDUMP_COMMAND))
                                        .then(Commands.literal("all").executes(ClientCommands::ALLDUMP_Command))
                                        .then(Commands.literal("activities").executes(ClientCommands::ACTIVITIESDUMP_COMMAND))
                                        .then(Commands.literal("attributes").executes(ClientCommands::ATTRIBUTESDUMP_COMMAND))
                                        .then(Commands.literal("blocks").executes((ctx) -> BLOCKSDUMP_Command(ctx, true, null))
                                                .then(Commands.literal("withtag")
                                                        .then(Commands.argument("tag", TagKeyArgument.block()).executes((ctx) -> BLOCKSDUMP_Command(ctx, false, TagKeyArgument.getLocation(ctx, "tag")))
                                                        )
                                                )
                                                .then(Commands.literal("withouttag")
                                                        .then(Commands.argument("tag", TagKeyArgument.block()).executes((ctx) -> BLOCKSDUMP_Command(ctx, true, TagKeyArgument.getLocation(ctx, "tag")))
                                                        )
                                                )
                                        )
                                        .then(Commands.literal("custom_stats").executes(ClientCommands::CUSTOMSTATSDUMP_COMMAND))
                                        .then(Commands.literal("depletable_items").executes(ClientCommands::DEPLETABLEITEMSDUMP_Command))
                                        .then(Commands.literal("enchants").executes(ClientCommands::ENCHANTSDUMP_Command))
                                        .then(Commands.literal("entities").executes(ClientCommands::ENTITIESDUMP_Command))
                                        .then(Commands.literal("features").executes(ClientCommands::FEATURESDUMP_COMMAND))
                                        .then(Commands.literal("fluids").executes(ClientCommands::FLUIDSDUMP_COMMAND))
                                        .then(Commands.literal("items").executes(ClientCommands::ITEMSDUMP_Command))
                                        .then(Commands.literal("mob_categories").executes(ClientCommands::MOB_CATEGORIESDUMP_COMMAND))
                                        .then(Commands.literal("mob_effects").executes(ClientCommands::MOB_EFFECTSDUMP_COMMAND))
                                        .then(Commands.literal("objective_criteria").executes(ClientCommands::OBJECTIVECRITERIADUMP_COMMAND))
                                        .then(Commands.literal("poi_types").executes(ClientCommands::POITYPESDUMP_COMMAND))
                                        .then(Commands.literal("potions").executes(ClientCommands::POTIONSDUMP_COMMAND))
                                        .then(Commands.literal("sound_events").executes(ClientCommands::SOUNDEVENTSDUMP_COMMAND))
                                        .then(Commands.literal("stat_types").executes(ClientCommands::STATTYPESDUMP_COMMAND))
                                        .then(Commands.literal("villager_professions").executes(ClientCommands::VILLAGERPROFESSIONSDUMP_COMMAND))
                                        .then(Commands.literal("villager_types").executes(ClientCommands::VILLAGERTYPEDUMP_COMMAND))
                                        .then(Commands.literal("structure").executes(ClientCommands::STRUCTUREDUMP_COMMAND))
                                        .then(Commands.literal("damage_type").executes(ClientCommands::DAMAGE_TYPEDUMP_COMMAND))
                                        .then(Commands.literal("configured_feature").executes(ClientCommands::CONFIGURED_FEATUREDUMP_COMMAND))
                                        .then(Commands.literal("biome").executes(ClientCommands::BIOMESDUMP_COMMAND))
                                        .then(Commands.literal("block_entity_types").executes(ClientCommands::BLOCKENTITYTYPESDUMP_COMMAND))
                                        .then(Commands.literal("dimension_type").executes(ClientCommands::DIMENSION_TYPEDUMP_COMMAND))
                                        .then(Commands.literal("menu_types").executes(ClientCommands::MENU_TYPESDUMP_COMMAND))
                                        //.then(Commands.literal("recipes").executes(ClientCommands::RECIPESDUMP_Command))
                                        //.then(Commands.literal("structure_features").executes(ClientCommands::STRUCTURE_FEATURESDUMP_COMMAND))
                                        //.then(Commands.literal("world_types").executes(ClientCommands::WORLD_TYPESDUMP_COMMAND))
                        )
                        .then(Commands.literal("block").executes((ctx) -> BLOCK_Command(ConfigHandler.includeNBT))
                                .then(Commands.literal("noNBT").executes((ctx) -> BLOCK_Command(false)))
                                .then(Commands.literal("yesNBT").executes((ctx) -> BLOCK_Command(true)))
                        )
                        .then(Commands.literal("blockinv").executes((ctx) -> BLOCKINV_Command(ConfigHandler.includeNBT, ConfigHandler.clearContents, ConfigHandler.printAsJSON))
                                .then(Commands.literal("noNBT").executes((ctx) -> BLOCKINV_Command(false, ConfigHandler.clearContents, ConfigHandler.printAsJSON))
                                        .then(Commands.literal("clear").executes((ctx) -> BLOCKINV_Command(false, true, ConfigHandler.printAsJSON)))
                                        .then(Commands.literal("keep").executes((ctx) -> BLOCKINV_Command(false, false, ConfigHandler.printAsJSON)))
                                )
                                .then(Commands.literal("yesNBT").executes((ctx) -> BLOCKINV_Command(true, ConfigHandler.clearContents, ConfigHandler.printAsJSON))
                                        .then(Commands.literal("clear").executes((ctx) -> BLOCKINV_Command(true, true, ConfigHandler.printAsJSON)))
                                        .then(Commands.literal("keep").executes((ctx) -> BLOCKINV_Command(true, false, ConfigHandler.printAsJSON)))
                                )
                                .then(Commands.literal("clear").executes((ctx) -> BLOCKINV_Command(ConfigHandler.includeNBT, true, ConfigHandler.printAsJSON))
                                        .then(Commands.literal("noNBT").executes((ctx) -> BLOCKINV_Command(false, true, ConfigHandler.printAsJSON)))
                                        .then(Commands.literal("yesNBT").executes((ctx) -> BLOCKINV_Command(true, true, ConfigHandler.printAsJSON)))
                                )
                                .then(Commands.literal("keep").executes((ctx) -> BLOCKINV_Command(ConfigHandler.includeNBT, false, ConfigHandler.printAsJSON))
                                        .then(Commands.literal("noNBT").executes((ctx) -> BLOCKINV_Command(false, false, ConfigHandler.printAsJSON)))
                                        .then(Commands.literal("yesNBT").executes((ctx) -> BLOCKINV_Command(true, false, ConfigHandler.printAsJSON)))
                                )
                                .then(Commands.literal("json").executes((ctx) -> BLOCKINV_Command(ConfigHandler.includeNBT, ConfigHandler.clearContents, true))
                                        .then(Commands.literal("clear").executes((ctx) -> BLOCKINV_Command(ConfigHandler.includeNBT, true, true))
                                                .then(Commands.literal("noNBT").executes((ctx) -> BLOCKINV_Command(false, true, true)))
                                                .then(Commands.literal("yesNBT").executes((ctx) -> BLOCKINV_Command(true, true, true)))
                                        )
                                        .then(Commands.literal("keep").executes((ctx) -> BLOCKINV_Command(ConfigHandler.includeNBT, false, true))
                                                .then(Commands.literal("noNBT").executes((ctx) -> BLOCKINV_Command(false, false, true)))
                                                .then(Commands.literal("yesNBT").executes((ctx) -> BLOCKINV_Command(true, false, true)))
                                        )
                                        .then(Commands.literal("noNBT").executes((ctx) -> BLOCKINV_Command(false, ConfigHandler.clearContents, true))
                                                .then(Commands.literal("clear").executes((ctx) -> BLOCKINV_Command(false, true, true)))
                                                .then(Commands.literal("keep").executes((ctx) -> BLOCKINV_Command(false, false, true)))
                                        )
                                        .then(Commands.literal("yesNBT").executes((ctx) -> BLOCKINV_Command(true, ConfigHandler.clearContents, true))
                                                .then(Commands.literal("clear").executes((ctx) -> BLOCKINV_Command(true, true, true)))
                                                .then(Commands.literal("keep").executes((ctx) -> BLOCKINV_Command(true, false, true)))
                                        )
                                )
                        )
                        .then(Commands.literal("hand").executes((ctx) -> HAND_Command(ConfigHandler.includeNBT))
                                .then(Commands.literal("noNBT").executes((ctx) -> HAND_Command(false)))
                                .then(Commands.literal("yesNBT").executes((ctx) -> HAND_Command(true)))
                        )
                        .then(Commands.literal("hot").executes((ctx) -> HOT_Command(ConfigHandler.includeNBT))
                                .then(Commands.literal("noNBT").executes((ctx) -> HOT_Command(false)))
                                .then(Commands.literal("yesNBT").executes((ctx) -> HOT_Command(true)))
                        )
                        .then(Commands.literal("inv").executes((ctx) -> INV_Command(ConfigHandler.includeNBT))
                                .then(Commands.literal("noNBT").executes((ctx) -> INV_Command(false)))
                                .then(Commands.literal("yesNBT").executes((ctx) -> INV_Command(true)))
                        )
                        .then(Commands.literal("recipemaker").executes(ClientCommands::RECIPEMAKER_Command)
                        )
                        .then(Commands.literal("panorama").executes((ctx) -> PANORAMA_Command(1024))
                                .then(Commands.argument("resolution", IntegerArgumentType.integer(128, 4096)).executes((ctx) -> PANORAMA_Command(IntegerArgumentType.getInteger(ctx, "resolution"))))
                        )
        );
    }

    //================================
    //General
    //================================
    private static int BLOCK_Command(boolean nbt) {
        CommandUtil.SendBlockCommand(nbt);
        return 0;
    }

    private static int RECIPEMAKER_Command(CommandContext<CommandSourceStack> ctx) {
        CommandUtil.SendRecipeMakerCommand();
        return 0;
    }

    private static int PANORAMA_Command(int resolution) {


        File grabDir = FileUtil.getOrCreateDirectory(Minecraft.getInstance().gameDirectory, "panoramas");
        if(grabDir == null) {
            PlayerUtil.sendClientMessage(Component.literal("Error, failed to create panorama").withStyle(Constants.ERROR_FORMAT));
            return 0;
        }

        long start = System.currentTimeMillis();
        Component comp = Minecraft.getInstance().grabPanoramixScreenshot(grabDir, resolution, resolution);
        long end = System.currentTimeMillis() - start;

        PlayerUtil.sendClientMessage(Component.literal("Took panorama in: " + end + "ms - ").append(comp));

        return (int)end;
    }

    //================================
    //Copy item id's
    //================================
    private static int BLOCKINV_Command(boolean nbt, boolean remove, boolean json) {
        CommandUtil.SendBlockInvCommand(nbt, remove, json);
        return 0;
    }

    private static int HAND_Command(boolean nbt) {
        CommandUtil.SendHandCommand(nbt);
        return 0;
    }

    private static int HOT_Command(boolean nbt) {
        CommandUtil.SendHotCommand(nbt);
        return 0;
    }

    private static int INV_Command(boolean nbt) {
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

//    private static int CHUNKHUD_Command(CommandContext<CommandSourceStack> ctx) {
//        if (Minecraft.getInstance().hasSingleplayerServer()) {
//            if (ctx.getSource().hasPermission(2)) {
//                HUDManager.RENDERCHUNK = !HUDManager.RENDERCHUNK;
//            } else {
//                PlayerUtil.sendClientMessage(Component.literal(Constants.ERROR_FORMAT + "You lack permissions to run this command" + ChatFormatting.RESET));
//            }
//        } else {
//            PlayerUtil.sendClientMessage(Component.literal(Constants.ERROR_FORMAT + "This command only works when on the IntegratedServer" + ChatFormatting.RESET));
//        }
//
//        return 0;
//    }

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
        ACTIVITIESDUMP_COMMAND(ctx);
        ATTRIBUTESDUMP_COMMAND(ctx);
        BIOMESDUMP_COMMAND(ctx);
        BLOCKENTITYTYPESDUMP_COMMAND(ctx);
        BLOCKSDUMP_Command(ctx, true, null);
        CUSTOMSTATSDUMP_COMMAND(ctx);
        DEPLETABLEITEMSDUMP_Command(ctx);
        ENCHANTSDUMP_Command(ctx);
        ENTITIESDUMP_Command(ctx);
        FEATURESDUMP_COMMAND(ctx);
        FLUIDSDUMP_COMMAND(ctx);
        ITEMSDUMP_Command(ctx);
        MOB_EFFECTSDUMP_COMMAND(ctx);
        MOB_CATEGORIESDUMP_COMMAND(ctx);
        OBJECTIVECRITERIADUMP_COMMAND(ctx);
        POITYPESDUMP_COMMAND(ctx);
        POTIONSDUMP_COMMAND(ctx);
        SOUNDEVENTSDUMP_COMMAND(ctx);
        STATTYPESDUMP_COMMAND(ctx);
        VILLAGERPROFESSIONSDUMP_COMMAND(ctx);
        VILLAGERTYPEDUMP_COMMAND(ctx);
        STRUCTUREDUMP_COMMAND(ctx);
        DAMAGE_TYPEDUMP_COMMAND(ctx);
        CONFIGURED_FEATUREDUMP_COMMAND(ctx);
        BIOMESDUMP_COMMAND(ctx);
        DIMENSION_TYPEDUMP_COMMAND(ctx);
        MENU_TYPESDUMP_COMMAND(ctx);
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

    private static int DEPLETABLEITEMSDUMP_Command(CommandContext<CommandSourceStack> ctx) {
        StringBuilder builder = new StringBuilder();
        for (Item e : ForgeRegistries.ITEMS) {
            if (e == Items.AIR || !e.canBeDepleted()) continue;
            builder.append(ForgeRegistries.ITEMS.getKey(e).toString()).append(System.lineSeparator());
        }
        writeDumpFile("depletable_items", builder);
        return 0;
    }

    private static int BLOCKSDUMP_Command(CommandContext<CommandSourceStack> ctx, boolean blacklist, @Nullable ResourceLocation tagKey) {
        ITag<Block> tag = null;

        StringBuilder builder = new StringBuilder();
        if(tagKey != null) {
            String text = (blacklist ? "Without BlockTag: " : "With BlockTag: ") + tagKey.toString();
            PlayerUtil.sendClientMessage(Component.literal(text).withStyle(Constants.CHAT_FORMAT));
            builder.append(text).append(System.lineSeparator());
            TagKey<Block> blockTagKey = BlockTags.create(tagKey);
            tag = ForgeRegistries.BLOCKS.tags().getTag(blockTagKey);
        }

        for (Block e : ForgeRegistries.BLOCKS) {
            if (e == Blocks.AIR) continue;

            if(tag == null) {
                builder.append(ForgeRegistries.BLOCKS.getKey(e).toString()).append(System.lineSeparator());
            } else {
                if(tag.contains(e) != blacklist){
                    builder.append(ForgeRegistries.BLOCKS.getKey(e).toString()).append(System.lineSeparator());
                }
            }
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
        for (Enchantment e : ForgeRegistries.ENCHANTMENTS) {
            builder.append(ForgeRegistries.ENCHANTMENTS.getKey(e).toString()).append(System.lineSeparator());
        }
        writeDumpFile("enchantments", builder);
        return 0;
    }

    private static int ENTITIESDUMP_Command(CommandContext<CommandSourceStack> ctx) {
        StringBuilder builder = new StringBuilder();
        for (EntityType<?> e : ForgeRegistries.ENTITY_TYPES) {
            builder.append(ForgeRegistries.ENTITY_TYPES.getKey(e).toString()).append(System.lineSeparator());
        }
        writeDumpFile("entities", builder);
        return 0;
    }

    private static int ATTRIBUTESDUMP_COMMAND(CommandContext<CommandSourceStack> ctx) {
        StringBuilder builder = new StringBuilder();
        for (Attribute e : ForgeRegistries.ATTRIBUTES) {
            builder.append(ForgeRegistries.ATTRIBUTES.getKey(e).toString()).append(System.lineSeparator());
        }
        writeDumpFile("attributes", builder);
        return 0;
    }

    private static int FEATURESDUMP_COMMAND(CommandContext<CommandSourceStack> ctx) {
        StringBuilder builder = new StringBuilder();
        for (Feature<?> e : ForgeRegistries.FEATURES) {
            builder.append(ForgeRegistries.FEATURES.getKey(e).toString()).append(System.lineSeparator());
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
        for (MobEffect e : ForgeRegistries.MOB_EFFECTS) {
            builder.append(ForgeRegistries.MOB_EFFECTS.getKey(e).toString()).append(System.lineSeparator());
        }
        writeDumpFile("mob_effects", builder);
        return 0;
    }

    private static int STRUCTUREDUMP_COMMAND(CommandContext<CommandSourceStack> ctx) {
        Minecraft minecraft = Minecraft.getInstance();
        IntegratedServer integratedServer = minecraft.getSingleplayerServer();
        if(integratedServer != null){
            StringBuilder builder = new StringBuilder();
            Optional<Registry<Structure>> registry = integratedServer.registryAccess().registry(Registries.STRUCTURE);
            if(registry.isPresent()){
                Registry<Structure> structureRegistry = registry.get();
                for (Structure e : structureRegistry) {
                    builder.append(structureRegistry.getKey(e).toString()).append(System.lineSeparator());
                }
                writeDumpFile("structure", builder);
            }
        }
        else{
            PlayerUtil.sendClientMessage(ComponentUtil.formatTitleContent("Dumping: structure", Component.literal("Failed (not in singleplayer)").withStyle(Constants.ERROR_FORMAT)));
        }

        return 0;
    }

    private static int DAMAGE_TYPEDUMP_COMMAND(CommandContext<CommandSourceStack> ctx) {
        Minecraft minecraft = Minecraft.getInstance();
        IntegratedServer integratedServer = minecraft.getSingleplayerServer();
        if(integratedServer != null){
            StringBuilder builder = new StringBuilder();
            Optional<Registry<DamageType>> registry = integratedServer.registryAccess().registry(Registries.DAMAGE_TYPE);
            if(registry.isPresent()){
                Registry<DamageType> damageTypeRegistry = registry.get();
                for (DamageType e : damageTypeRegistry) {
                    builder.append(damageTypeRegistry.getKey(e).toString()).append(System.lineSeparator());
                }
                writeDumpFile("damage_type", builder);
            }
        }
        else{
            PlayerUtil.sendClientMessage(ComponentUtil.formatTitleContent("Dumping: damage_type", Component.literal("Failed (not in singleplayer)").withStyle(Constants.ERROR_FORMAT)));
        }

        return 0;
    }

    private static int CONFIGURED_FEATUREDUMP_COMMAND(CommandContext<CommandSourceStack> ctx) {
        Minecraft minecraft = Minecraft.getInstance();
        IntegratedServer integratedServer = minecraft.getSingleplayerServer();
        if(integratedServer != null){
            StringBuilder builder = new StringBuilder();
            Optional<Registry<ConfiguredFeature<?, ?>>> registry = integratedServer.registryAccess().registry(Registries.CONFIGURED_FEATURE);
            if(registry.isPresent()){
                Registry<ConfiguredFeature<?, ?>> configuredFeatureRegistry = registry.get();
                for (ConfiguredFeature<?, ?> e : configuredFeatureRegistry) {
                    builder.append(configuredFeatureRegistry.getKey(e).toString()).append(System.lineSeparator());
                }
                writeDumpFile("configured_feature", builder);
            }
        }
        else{
            PlayerUtil.sendClientMessage(ComponentUtil.formatTitleContent("Dumping: configured_feature", Component.literal("Failed (not in singleplayer)").withStyle(Constants.ERROR_FORMAT)));
        }

        return 0;
    }

    private static int BIOMESDUMP_COMMAND(CommandContext<CommandSourceStack> ctx) {
        Minecraft minecraft = Minecraft.getInstance();
        IntegratedServer integratedServer = minecraft.getSingleplayerServer();
        if(integratedServer != null){
            StringBuilder builder = new StringBuilder();
            Optional<Registry<Biome>> registry = integratedServer.registryAccess().registry(Registries.BIOME);
            if(registry.isPresent()){
                Registry<Biome> biomeRegistry = registry.get();
                for (Biome e : biomeRegistry) {
                    builder.append(biomeRegistry.getKey(e).toString()).append(System.lineSeparator());
                }
                writeDumpFile("biome", builder);
            }
        }
        else{
            PlayerUtil.sendClientMessage(ComponentUtil.formatTitleContent("Dumping: biome", Component.literal("Failed (not in singleplayer)").withStyle(Constants.ERROR_FORMAT)));
        }

        return 0;
    }

    private static int DIMENSION_TYPEDUMP_COMMAND(CommandContext<CommandSourceStack> ctx) {
        Minecraft minecraft = Minecraft.getInstance();
        IntegratedServer integratedServer = minecraft.getSingleplayerServer();
        if(integratedServer != null){
            StringBuilder builder = new StringBuilder();
            Optional<Registry<DimensionType>> registry = integratedServer.registryAccess().registry(Registries.DIMENSION_TYPE);
            if(registry.isPresent()){
                Registry<DimensionType> dimensionRegistry = registry.get();
                for (DimensionType e : dimensionRegistry) {
                    builder.append(dimensionRegistry.getKey(e).toString()).append(System.lineSeparator());
                }
                writeDumpFile("dimension_type", builder);
            }
        }
        else{
            PlayerUtil.sendClientMessage(ComponentUtil.formatTitleContent("Dumping: dimension_type", Component.literal("Failed (not in singleplayer)").withStyle(Constants.ERROR_FORMAT)));
        }

        return 0;
    }

    private static int STATTYPESDUMP_COMMAND(CommandContext<CommandSourceStack> ctx) {
        StringBuilder builder = new StringBuilder();
        ResourceLocation minecraftCustom = new ResourceLocation("custom");
        for (StatType<?> e : ForgeRegistries.STAT_TYPES) {
            ResourceLocation statType = ForgeRegistries.STAT_TYPES.getKey(e);
            if (statType.equals(minecraftCustom)) {

                ArrayList<String> customStats = new ArrayList<>();
                e.forEach(e2 -> {
                    customStats.add(e2.getName());
                });
                customStats.sort(Comparator.naturalOrder());

                for (String s : customStats) {
                    builder.append(s).append(System.lineSeparator());
                }

            } else {
                builder.append(statType.getNamespace()).append(".").append(statType.getPath()).append(":*").append(System.lineSeparator());
            }
        }
        writeDumpFile("stat_types", builder);
        return 0;
    }

    private static int OBJECTIVECRITERIADUMP_COMMAND(CommandContext<CommandSourceStack> ctx) {
        StringBuilder builder = new StringBuilder();
        for (String e : ObjectiveCriteria.getCustomCriteriaNames().stream().sorted().toList()) {
            builder.append(e).append(System.lineSeparator());
        }
        writeDumpFile("objective_criteria", builder);
        return 0;
    }

    private static int MOB_CATEGORIESDUMP_COMMAND(CommandContext<CommandSourceStack> ctx) {
        //HashMap<String, List<String>> cats = new HashMap<>();
        TreeMap<String, List<String>> cats = new TreeMap<>();
        for (EntityType<?> e : ForgeRegistries.ENTITY_TYPES) {
            cats.compute(e.getCategory().getName(), (k, v) -> {
                if (v == null) {
                    v = new ArrayList<>();
                }

                v.add(EntityType.getKey(e).toString());

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

    private static int POTIONSDUMP_COMMAND(CommandContext<CommandSourceStack> ctx) {
        StringBuilder builder = new StringBuilder();
        for (Potion e : ForgeRegistries.POTIONS) {
            builder.append(ForgeRegistries.POTIONS.getKey(e).toString()).append(System.lineSeparator());
        }
        writeDumpFile("potions", builder);
        return 0;
    }

    private static int ACTIVITIESDUMP_COMMAND(CommandContext<CommandSourceStack> ctx) {
        StringBuilder builder = new StringBuilder();
        for (Activity e : ForgeRegistries.ACTIVITIES) {
            builder.append(ForgeRegistries.ACTIVITIES.getKey(e).toString()).append(System.lineSeparator());
        }
        writeDumpFile("activities", builder);
        return 0;
    }

    private static int POITYPESDUMP_COMMAND(CommandContext<CommandSourceStack> ctx) {
        StringBuilder builder = new StringBuilder();
        for (PoiType e : ForgeRegistries.POI_TYPES) {
            builder.append(ForgeRegistries.POI_TYPES.getKey(e).toString()).append(System.lineSeparator());
        }
        writeDumpFile("poi_types", builder);
        return 0;
    }

    private static int SOUNDEVENTSDUMP_COMMAND(CommandContext<CommandSourceStack> ctx) {
        StringBuilder builder = new StringBuilder();
        for (SoundEvent e : ForgeRegistries.SOUND_EVENTS) {
            builder.append(ForgeRegistries.SOUND_EVENTS.getKey(e).toString()).append(System.lineSeparator());
        }
        writeDumpFile("sound_events", builder);
        return 0;
    }

    private static int VILLAGERPROFESSIONSDUMP_COMMAND(CommandContext<CommandSourceStack> ctx) {
        StringBuilder builder = new StringBuilder();
        for (VillagerProfession e : ForgeRegistries.VILLAGER_PROFESSIONS) {
            builder.append(ForgeRegistries.VILLAGER_PROFESSIONS.getKey(e).toString()).append(System.lineSeparator());
        }
        writeDumpFile("villager_professions", builder);
        return 0;
    }

    private static int VILLAGERTYPEDUMP_COMMAND(CommandContext<CommandSourceStack> ctx) {
        StringBuilder builder = new StringBuilder();
        for (VillagerType e : BuiltInRegistries.VILLAGER_TYPE) {
            builder.append(BuiltInRegistries.VILLAGER_TYPE.getKey(e).toString()).append(System.lineSeparator());
        }
        writeDumpFile("villager_types", builder);
        return 0;
    }

    private static int CUSTOMSTATSDUMP_COMMAND(CommandContext<CommandSourceStack> ctx) {
        StringBuilder builder = new StringBuilder();
        for (ResourceLocation e : BuiltInRegistries.CUSTOM_STAT) {
            builder.append(BuiltInRegistries.CUSTOM_STAT.getKey(e).toString()).append(System.lineSeparator());
        }
        writeDumpFile("custom_stats", builder);
        return 0;
    }

    private static int BLOCKENTITYTYPESDUMP_COMMAND(CommandContext<CommandSourceStack> ctx) {
        StringBuilder builder = new StringBuilder();
        for (BlockEntityType<?> e : ForgeRegistries.BLOCK_ENTITY_TYPES) {
            builder.append(ForgeRegistries.BLOCK_ENTITY_TYPES.getKey(e).toString()).append(System.lineSeparator());
        }
        writeDumpFile("block_entity_types", builder);
        return 0;
    }

    private static int MENU_TYPESDUMP_COMMAND(CommandContext<CommandSourceStack> ctx) {
        StringBuilder builder = new StringBuilder();
        for (MenuType<?> e : ForgeRegistries.MENU_TYPES) {
            builder.append(ForgeRegistries.MENU_TYPES.getKey(e).toString()).append(System.lineSeparator());
        }
        writeDumpFile("menu_types", builder);
        return 0;
    }

//    private static int ADVANCEMENTDUMP_COMMAND(CommandContext<CommandSourceStack> ctx) {
//        Minecraft minecraft = Minecraft.getInstance();
//        IntegratedServer integratedServer = minecraft.getSingleplayerServer();
//        if(integratedServer != null){
//            StringBuilder builder = new StringBuilder();
//            Collection<Advancement> allAdvancements = integratedServer.getAdvancements().getAllAdvancements();
//            for (Advancement advancement : allAdvancements) {
//                if(!advancement.getId().getPath().startsWith("recipes")){
//                }
//            }
////            builder.append(dimensionRegistry.getKey(e).toString()).append(System.lineSeparator());
////            writeDumpFile("advancements", builder);
//        }
//        else{
//            PlayerUtil.sendClientMessage(ComponentUtil.formatTitleContent("Dumping: dimension_type", Component.literal("Failed (not in singleplayer)").withStyle(Constants.ERROR_FORMAT)));
//        }
//
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