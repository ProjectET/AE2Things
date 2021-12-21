package io.github.projectet.ae2things.storage;

import appeng.api.storage.cells.ISaveProvider;
import appeng.me.cells.BasicCellHandler;
import appeng.me.cells.BasicCellInventory;
import net.minecraft.item.ItemStack;

public class DISKCellHandler extends BasicCellHandler {

    @Override
    public BasicCellInventory getCellInventory(ItemStack is, ISaveProvider container) {
        return BasicCellInventory.createInventory(is, container);
    }
}
