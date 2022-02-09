package io.github.projectet.ae2things.inventory;

import appeng.api.implementations.items.IGrowableCrystal;
import appeng.api.inventories.InternalInventory;
import appeng.core.definitions.AEItems;
import appeng.items.misc.CrystalSeedItem;
import appeng.menu.slot.AppEngSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CrystalGrowthSlot extends AppEngSlot {

    public static Set<Item> validItems = new HashSet<>(List.of(Items.REDSTONE, Items.QUARTZ, AEItems.CERTUS_QUARTZ_CRYSTAL_CHARGED.asItem()));

    public CrystalGrowthSlot(InternalInventory inv, int invSlot) {
        super(inv, invSlot);
    }

    @Override
    public boolean canInsert(ItemStack stack) {
        if (!this.getMenu().isValidForSlot(this, stack))
            return false;

        if (stack.isEmpty())
            return false;

        if (stack.getItem() == Items.AIR)
            return false;

        if (!super.canInsert(stack))
            return false;

        if(stack.getItem() instanceof IGrowableCrystal)
            return true;

        return validItems.contains(stack.getItem());
    }
}
