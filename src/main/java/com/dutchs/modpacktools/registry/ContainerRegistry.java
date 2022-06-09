package com.dutchs.modpacktools.registry;

import com.dutchs.modpacktools.Constants;
import com.dutchs.modpacktools.gui.RecipeMakerMenu;
import net.minecraft.core.Registry;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.*;

public class ContainerRegistry {
    private static final DeferredRegister<MenuType<?>> MENU = DeferredRegister.create(Registry.MENU_REGISTRY, Constants.MODID);
    public static final RegistryObject<MenuType<RecipeMakerMenu>> RECIPE_MAKER = MENU.register("recipe_maker", () -> IForgeMenuType.create((windowId, inv, data) -> new RecipeMakerMenu(windowId, inv, inv.player)));

    public static void init(IEventBus bus){
        MENU.register(bus);
    }
}
