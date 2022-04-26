package io.github.projectet.ae2things.gui.cell;

import appeng.api.implementations.blockentities.IChestOrDrive;
import appeng.api.stacks.AEKeyType;
import appeng.api.storage.cells.ICellGuiHandler;
import appeng.api.storage.cells.ICellHandler;
import appeng.menu.MenuOpener;
import appeng.menu.locator.MenuLocators;
import appeng.menu.me.common.MEStorageMenu;
import io.github.projectet.ae2things.storage.IDISKCellItem;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

public class DISKItemCellGuiHandler implements ICellGuiHandler {
    @Override
    public boolean isSpecializedFor(ItemStack cell) {
        return cell.getItem() instanceof IDISKCellItem diskCell
                && diskCell.getKeyType() == AEKeyType.items();
    }

    @Override
    public void openChestGui(Player player, IChestOrDrive chest, ICellHandler cellHandler, ItemStack cell) {
        MenuOpener.open(MEStorageMenu.TYPE, player,
                MenuLocators.forBlockEntity(((BlockEntity) chest)));
    }
}
