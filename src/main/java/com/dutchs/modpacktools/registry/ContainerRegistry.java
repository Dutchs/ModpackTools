package com.dutchs.modpacktools.registry;

import com.dutchs.modpacktools.Constants;
import com.dutchs.modpacktools.gui.RecipeMakerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.ObjectHolder;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class ContainerRegistry {
    @SubscribeEvent
    public static void onContainerRegistry(final RegistryEvent.Register<MenuType<?>> event) {
        IForgeRegistry<MenuType<?>> r = event.getRegistry();
        r.register(IForgeMenuType.create(((windowId, inv, data) -> new RecipeMakerMenu(windowId, inv, inv.player))).setRegistryName("recipe_maker"));
    }

    @ObjectHolder(Constants.MODID + ":recipe_maker")
    public static MenuType<RecipeMakerMenu> RECIPE_MAKER;
}
