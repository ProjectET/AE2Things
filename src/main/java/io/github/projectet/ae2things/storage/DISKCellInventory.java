package io.github.projectet.ae2things.storage;

import appeng.api.storage.cells.CellState;
import appeng.api.storage.cells.IBasicCellItem;
import appeng.api.storage.cells.ISaveProvider;
import appeng.api.storage.cells.StorageCell;
import io.github.projectet.ae2things.item.DISKDrive;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

import java.util.Objects;

public class DISKCellInventory implements StorageCell {

    public DISKCellInventory(DISKDrive cellType, ItemStack stack, ISaveProvider saveProvider) {
    }

    @Override
    public CellState getStatus() {
        return null;
    }

    @Override
    public double getIdleDrain() {
        return 0;
    }

    @Override
    public void persist() {

    }

    @Override
    public Text getDescription() {
        return null;
    }

    public static DISKCellInventory createInventory(ItemStack stack, ISaveProvider saveProvider) {
        Objects.requireNonNull(stack, "Cannot create cell inventory for null itemstack");

        if (!(stack.getItem() instanceof DISKDrive cellType)) {
            return null;
        }

        if (!cellType.isStorageCell(stack)) {
            // This is not an error. Items may decide to not be a storage cell temporarily.
            return null;
        }

        // The cell type's channel matches, so this cast is safe
        return new DISKCellInventory(cellType, stack, saveProvider);
    }
}
