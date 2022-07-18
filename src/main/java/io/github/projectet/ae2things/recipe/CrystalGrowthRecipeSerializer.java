package io.github.projectet.ae2things.recipe;

import com.google.gson.JsonObject;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;

public class CrystalGrowthRecipeSerializer implements RecipeSerializer<CrystalGrowthRecipe> {

    public static final CrystalGrowthRecipeSerializer INSTANCE = new CrystalGrowthRecipeSerializer();

    private CrystalGrowthRecipeSerializer() {}

    @Override
    public CrystalGrowthRecipe fromJson(ResourceLocation resourceLocation, JsonObject jsonObject) {
        ItemStack output = ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(jsonObject, "result"));
        JsonObject ingredients = GsonHelper.getAsJsonObject(jsonObject, "ingredients");
        Ingredient flawless = Ingredient.fromJson(ingredients.get("flawless"));
        Ingredient flawed = ingredients.has("flawed") ? Ingredient.fromJson(ingredients.get("flawed")) : Ingredient.EMPTY;
        Ingredient chipped = ingredients.has("chipped") ? Ingredient.fromJson(ingredients.get("chipped")) : Ingredient.EMPTY;
        Ingredient damaged = ingredients.has("damaged") ? Ingredient.fromJson(ingredients.get("damaged")) : Ingredient.EMPTY;
        return new CrystalGrowthRecipe(resourceLocation, flawless, flawed, chipped, damaged, output);
    }

    @Override
    public CrystalGrowthRecipe fromNetwork(ResourceLocation resourceLocation, FriendlyByteBuf friendlyByteBuf) {
        ItemStack output = friendlyByteBuf.readItem();
        Ingredient flawless = Ingredient.fromNetwork(friendlyByteBuf);
        Ingredient flawed = Ingredient.fromNetwork(friendlyByteBuf);
        Ingredient chipped = Ingredient.fromNetwork(friendlyByteBuf);
        Ingredient damaged = Ingredient.fromNetwork(friendlyByteBuf);

        return new CrystalGrowthRecipe(resourceLocation, flawless, flawed, chipped, damaged, output);
    }

    @Override
    public void toNetwork(FriendlyByteBuf friendlyByteBuf, CrystalGrowthRecipe recipe) {
        friendlyByteBuf.writeItem(recipe.getResultItem());
        recipe.getFlawlessCrystal().toNetwork(friendlyByteBuf);
        recipe.getFlawedCrystal().toNetwork(friendlyByteBuf);
        recipe.getChippedCrystal().toNetwork(friendlyByteBuf);
        recipe.getDamagedCrystal().toNetwork(friendlyByteBuf);
    }
}
