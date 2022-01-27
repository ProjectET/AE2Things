package io.github.projectet.ae2things.storage;

import appeng.api.config.IncludeExclude;
import appeng.api.storage.cells.ICellHandler;
import appeng.api.storage.cells.ISaveProvider;
import appeng.core.localization.GuiText;
import appeng.core.localization.Tooltips;
import io.github.projectet.ae2things.item.DISKDrive;
import net.minecraft.item.ItemStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

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
        var handler = getCellInventory(stack, null);

        if(handler == null)
            return;

        if(handler.hasDiskUUID()) {
            lines.add(new LiteralText("Disk UUID: ").formatted(Formatting.GRAY).append(new LiteralText(handler.getDiskUUID().toString()).formatted(Formatting.AQUA)));
            lines.add(Tooltips.bytesUsed(handler.getNbtItemCount(), handler.getTotalBytes()));
        }

        if (handler.isPreformatted()) {
            var list = (handler.getPartitionListMode() == IncludeExclude.WHITELIST ? GuiText.Included
                    : GuiText.Excluded)
                    .text();

            if (handler.isFuzzy()) {
                lines.add(GuiText.Partitioned.withSuffix(" - ").append(list).append(" ").append(GuiText.Fuzzy.text()));
            } else {
                lines.add(
                        GuiText.Partitioned.withSuffix(" - ").append(list).append(" ").append(GuiText.Precise.text()));
            }
        }
    }
}
