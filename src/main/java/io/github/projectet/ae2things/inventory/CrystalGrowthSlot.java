package io.github.projectet.ae2things.inventory;

import appeng.api.inventories.InternalInventory;

import appeng.api.stacks.GenericStack;
import appeng.menu.AEBaseMenu;
import io.github.projectet.ae2things.recipe.CrystalGrowthRecipe;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class CrystalGrowthSlot extends Slot {

    private static final Container EMPTY_INVENTORY = new SimpleContainer(0);
    private final InternalInventory inventory;
    private final int invSlot;

    private Level world;

    private AEBaseMenu menu = null;
    private boolean active = true;

    public final int slotIndex;


    public CrystalGrowthSlot(InternalInventory inv, int invSlot, int x, int y, int slotIndex, Level world) {
        super(EMPTY_INVENTORY, invSlot, x, y);
        this.invSlot = invSlot;
        this.slotIndex = slotIndex;
        this.inventory = inv;
        this.world = world;
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        if (containsWrapperItem()) {
            return false;
        }
        CrystalGrowthRecipe recipe = CrystalGrowthRecipe.getRecipefromStack(world, stack);
        if(recipe == null)
            return false;
        else {
            switch(slotIndex) {
                case 0 -> {
                    return recipe.getFlawlessCrystal().test(stack) || recipe.getFlawedCrystal().test(stack);
                }
                case 1 -> {
                    return recipe.getChippedCrystal().test(stack);
                }
                case 2 -> {
                    return recipe.getDamagedCrystal().test(stack);
                }
                default -> {
                    return false;
                }
            }
        }
    }

    @Override
    public ItemStack getItem() {
        if (!this.isSlotEnabled()) {
            return ItemStack.EMPTY;
        }

        if (this.invSlot >= this.inventory.size()) {
            return ItemStack.EMPTY;
        }

        return this.inventory.getStackInSlot(this.invSlot);
    }

    @Override
    public void set(ItemStack stack) {
        if (this.isSlotEnabled()) {
            this.inventory.setItemDirect(this.invSlot, stack);
            this.setChanged();
        }
    }

    @Override
    public void initialize(ItemStack stack) {
        this.inventory.setItemDirect(this.invSlot, stack);
    }

    private void notifyContainerSlotChanged() {
        if (this.getMenu() != null) {
            this.getMenu().onSlotChange(this);
        }
    }

    public InternalInventory getInventory() {
        return this.inventory;
    }

    @Override
    public void setChanged() {
        super.setChanged();
        notifyContainerSlotChanged();
    }

    @Override
    public int getMaxStackSize() {
        return this.inventory.getSlotLimit(this.invSlot);
    }

    @Override
    public int getMaxStackSize(ItemStack stack) {
        return Math.min(this.getMaxStackSize(), stack.getMaxStackSize());
    }

    @Override
    public boolean mayPickup(Player player) {
        if (containsWrapperItem()) {
            return false;
        }
        if (this.isSlotEnabled()) {
            return !this.inventory.extractItem(this.invSlot, 1, true).isEmpty();
        }
        return false;
    }

    @Override
    public ItemStack remove(int amount) {
        if (containsWrapperItem()) {
            return ItemStack.EMPTY;
        }

        return this.inventory.extractItem(this.invSlot, amount, false);
    }

    private boolean containsWrapperItem() {
        return GenericStack.isWrapped(getItem());
    }

    @Override
    public boolean isActive() {
        return this.isSlotEnabled() && active;
    }

    public boolean isSlotEnabled() {
        return true;
    }

    protected AEBaseMenu getMenu() {
        return this.menu;
    }

    public void setMenu(AEBaseMenu menu) {
        this.menu = menu;
    }
}
