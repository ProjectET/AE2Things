package io.github.projectet.ae2things.recipe;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;

import java.util.Collection;

public class CrystalGrowthRecipes {

    public CrystalGrowthRecipe getRecipefromStack(Level level, ItemStack item) {
        Collection<CrystalGrowthRecipe> values = level.getRecipeManager().byType(CrystalGrowthRecipe.TYPE).values();
        CrystalGrowthRecipe matchedRecipe = null;
        for (CrystalGrowthRecipe recipe : values) {
            for(Ingredient ingredient : recipe.getIngredients()) {
                if(ingredient.test(item))
                    matchedRecipe = recipe;
                    break;
            }

        }
        return matchedRecipe;
    }
}
