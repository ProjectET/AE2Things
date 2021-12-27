package io.github.projectet.ae2things.storage;

import appeng.api.config.IncludeExclude;
import appeng.api.storage.cells.ICellHandler;
import appeng.api.storage.cells.ISaveProvider;
import appeng.core.localization.GuiText;
import io.github.projectet.ae2things.item.DISKDrive;
import io.github.projectet.ae2things.util.Constants;
import net.minecraft.item.ItemStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;

import java.util.List;

public class DISKCellHandler implements ICellHandler {

    public static final DISKCellHandler INSTANCE = new DISKCellHandler();

    @Override
    public boolean isCell(ItemStack is) {
        return is.getItem() instanceof DISKDrive;
    }

    @Override
    public DISKCellInventory getCellInventory(ItemStack is, ISaveProvider container) {
        return DISKCellInventory.createInventory(is, container);
    }

    public void addCellInformationToTooltip(ItemStack stack, List<Text> lines) {
        if(stack.getOrCreateNbt().contains(Constants.DISKUUID)) {
            lines.add(new LiteralText(stack.getOrCreateNbt().getLong(DISKCellInventory.ITEM_COUNT_TAG) + " ").append(GuiText.Of.text())
                    .append(" " + ((DISKDrive) stack.getItem()).getBytes(stack) + "").append(GuiText.BytesUsed.text()));

            /*if (handler.isPreformatted()) {
                var list = (handler.getPartitionListMode() == IncludeExclude.WHITELIST ? GuiText.Included
                        : GuiText.Excluded)
                        .text();

                if (handler.isFuzzy()) {
                    lines.add(GuiText.Partitioned.withSuffix(" - ").append(list).append(" ").append(GuiText.Fuzzy.text()));
                } else {
                    lines.add(
                            GuiText.Partitioned.withSuffix(" - ").append(list).append(" ").append(GuiText.Precise.text()));
                }
            }*/
        }
    }
}
