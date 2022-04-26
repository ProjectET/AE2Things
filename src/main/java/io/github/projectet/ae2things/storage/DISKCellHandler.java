package io.github.projectet.ae2things.storage;

import appeng.api.config.IncludeExclude;
import appeng.api.storage.cells.ICellHandler;
import appeng.api.storage.cells.ISaveProvider;
import appeng.core.localization.GuiText;
import appeng.core.localization.Tooltips;
import io.github.projectet.ae2things.item.DISKDrive;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.item.ItemStack;

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

    public void addCellInformationToTooltip(ItemStack stack, List<Component> lines) {
        var handler = getCellInventory(stack, null);

        if(handler == null)
            return;

        if(handler.hasDiskUUID()) {
            lines.add(new TextComponent("Disk UUID: ").withStyle(ChatFormatting.GRAY).append(new TextComponent(handler.getDiskUUID().toString()).withStyle(ChatFormatting.AQUA)));
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
