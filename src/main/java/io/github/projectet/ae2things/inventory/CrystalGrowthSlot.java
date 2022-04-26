package io.github.projectet.ae2things.inventory;

import appeng.api.implementations.items.IGrowableCrystal;
import appeng.api.inventories.InternalInventory;
import appeng.core.definitions.AEItems;
import appeng.items.misc.CrystalSeedItem;
import appeng.menu.slot.AppEngSlot;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class CrystalGrowthSlot extends AppEngSlot {

    public static Set<Item> validItems = new HashSet<>(List.of(Items.REDSTONE, Items.QUARTZ, AEItems.CERTUS_QUARTZ_CRYSTAL_CHARGED.asItem()));

    public CrystalGrowthSlot(InternalInventory inv, int invSlot) {
        super(inv, invSlot);
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        if (!this.getMenu().isValidForSlot(this, stack))
            return false;

        if (stack.isEmpty())
            return false;

        if (stack.getItem() == Items.AIR)
            return false;

        if (!super.mayPlace(stack))
            return false;

        if(stack.getItem() instanceof IGrowableCrystal)
            return true;

        return validItems.contains(stack.getItem());
    }
}
