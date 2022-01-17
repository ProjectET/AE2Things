package io.github.projectet.ae2things.compat;

import io.github.projectet.ae2things.AE2Things;
import io.github.projectet.ae2things.gui.advancedInscriber.AdvancedInscriberRootPanel;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.client.registry.category.ButtonArea;
import me.shedaniel.rei.api.client.registry.category.CategoryRegistry;
import me.shedaniel.rei.api.client.registry.screen.ScreenRegistry;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.util.EntryStacks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.registry.Registry;

import java.lang.reflect.Field;

public class REI implements REIClientPlugin {

    Class<?> inscriberRecipeCategory;
    CategoryIdentifier<?> ID;

    public REI() throws ClassNotFoundException, NoSuchFieldException, IllegalAccessException {
        inscriberRecipeCategory = Class.forName("appeng.integration.modules.jei.InscriberRecipeCategory");
        Field id = inscriberRecipeCategory.getDeclaredField("ID");
        id.setAccessible(true);
        ID = (CategoryIdentifier<?>) id.get(null);
    }

    @Override
    public void registerScreens(ScreenRegistry registry) {
        registry.registerContainerClickArea(
                new Rectangle(82, 39, 26, 16),
                AdvancedInscriberRootPanel.class,
                ID);
    }

    @Override
    public void registerCategories(CategoryRegistry registry) {
        ItemStack inscriber = new ItemStack(Registry.ITEM.get(AE2Things.id("advanced_inscriber")));
        registry.addWorkstations(ID, EntryStacks.of(inscriber));
        registry.setPlusButtonArea(ID, ButtonArea.defaultArea());
    }
}
