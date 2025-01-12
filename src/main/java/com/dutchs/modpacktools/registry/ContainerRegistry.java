package com.dutchs.modpacktools.registry;

import com.dutchs.modpacktools.Constants;
import com.dutchs.modpacktools.gui.RecipeMakerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.registries.*;

public class ContainerRegistry {
    public static final DeferredRegister<MenuType<?>> MENU_TYPES = DeferredRegister.create(ForgeRegistries.MENU_TYPES, Constants.MODID);
    public static final RegistryObject<MenuType<RecipeMakerMenu>> RECIPE_MAKER = MENU_TYPES.register("recipe_maker", () -> IForgeMenuType.create(((windowId, inv, data) -> new RecipeMakerMenu(windowId, inv, inv.player))));
}
