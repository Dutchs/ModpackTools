package com.dutchs.modpacktools.datagen;

import com.dutchs.modpacktools.Constants;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Constants.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class DataGenerators {

    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
//        if (event.includeServer()) {
//        }
        if (event.includeClient()) {
            generator.addProvider(true, new ModLanguageProvider(generator, "en_us"));
        }
    }
}
