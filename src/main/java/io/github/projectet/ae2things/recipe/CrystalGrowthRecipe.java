package io.github.projectet.ae2things.recipe;

import io.github.projectet.ae2things.AE2Things;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

public class CrystalGrowthRecipe implements Recipe<Container> {

    private final ResourceLocation TYPE_ID = AE2Things.id("crystal_growth_chamber");

    private final RecipeType<CrystalGrowthRecipe> TYPE = RecipeType.register(TYPE_ID.toString());

    private final ResourceLocation id;

    private final Ingredient flawlessCrystal;
    private final Ingredient flawedCrystal;
    private final Ingredient chippedCrystal;
    private final Ingredient damagedCrystal;
    private final ItemStack outputIngredient;

    public CrystalGrowthRecipe(ResourceLocation id, Ingredient flawlessCrystal, Ingredient flawedCrystal,
                               Ingredient chippedCrystal, Ingredient damagedCrystal, ItemStack outputIngredient) {
        this.id = id;
        this.flawlessCrystal = flawlessCrystal;
        this.flawedCrystal = flawedCrystal;
        this.chippedCrystal = chippedCrystal;
        this.damagedCrystal = damagedCrystal;
        this.outputIngredient = outputIngredient;
    }

    public boolean isFlawless(ItemStack testStack) {
        return getFlawlessCrystal().test(testStack);
    }

    public Ingredient getFlawlessCrystal() {
        return flawlessCrystal;
    }

    public Ingredient getFlawedCrystal() {
        return flawedCrystal;
    }

    public Ingredient getChippedCrystal() {
        return chippedCrystal;
    }

    public Ingredient getDamagedCrystal() {
        return damagedCrystal;
    }

    @Override
    public boolean matches(Container container, Level level) {
        return false;
    }

    @Override
    public ItemStack assemble(Container container) {
        return outputIngredient.copy();
    }

    @Override
    public boolean canCraftInDimensions(int i, int j) {
        return true;
    }

    @Override
    public ItemStack getResultItem() {
        return outputIngredient;
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        return NonNullList.of(flawlessCrystal, flawedCrystal, chippedCrystal, damagedCrystal);
    }

    @Override
    public ResourceLocation getId() {
        return id;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return CrystalGrowthRecipeSerializer.INSTANCE;
    }

    @Override
    public RecipeType<?> getType() {
        return TYPE;
    }
}
