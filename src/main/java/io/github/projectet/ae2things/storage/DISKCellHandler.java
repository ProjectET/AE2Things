package io.github.projectet.ae2things.storage;

import appeng.api.storage.cells.ICellHandler;
import appeng.api.storage.cells.ISaveProvider;
import io.github.projectet.ae2things.item.DISKDrive;
import net.minecraft.item.ItemStack;

public class DISKCellHandler implements ICellHandler {

    @Override
    public boolean isCell(ItemStack is) {
        return is.getItem() instanceof DISKDrive;
    }

    @Override
    public DISKCellInventory getCellInventory(ItemStack is, ISaveProvider container) {
        return DISKCellInventory.createInventory(is, container);
    }
}
