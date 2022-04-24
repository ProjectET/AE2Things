package io.github.projectet.ae2things.gui.cell;

import appeng.api.config.*;
import appeng.api.implementations.menuobjects.IPortableTerminal;
import appeng.api.implementations.menuobjects.ItemMenuHost;
import appeng.api.storage.MEStorage;
import appeng.api.util.IConfigManager;
import appeng.menu.ISubMenu;
import appeng.util.ConfigManager;
import com.google.common.base.Preconditions;
import io.github.projectet.ae2things.item.PortableDISKItem;
import io.github.projectet.ae2things.storage.DISKCellHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.BiConsumer;

public class PortableDISKMenuHost extends ItemMenuHost implements IPortableTerminal {

    private final BiConsumer<PlayerEntity, ISubMenu> returnMainMenu;
    private final MEStorage cellStorage;
    private final PortableDISKItem item;

    public PortableDISKMenuHost(PlayerEntity player, @Nullable Integer slot, PortableDISKItem diskItem, ItemStack itemStack, BiConsumer<PlayerEntity, ISubMenu> returnMainMenu) {
        super(player, slot, itemStack);
        Preconditions.checkArgument(itemStack.getItem() == diskItem, "Stack doesn't match item");
        this.returnMainMenu = returnMainMenu;
        this.cellStorage = DISKCellHandler.INSTANCE.getCellInventory(itemStack, null);
        Objects.requireNonNull(cellStorage, "Portable cell doesn't expose a cell inventory.");
        this.item = diskItem;
    }

    @Override
    public double extractAEPower(double amt, Actionable mode, PowerMultiplier usePowerMultiplier) {
        amt = usePowerMultiplier.multiply(amt);

        if (mode == Actionable.SIMULATE) {
            return usePowerMultiplier.divide(Math.min(amt, this.item.getAECurrentPower(getItemStack())));
        }

        return usePowerMultiplier.divide(this.item.extractAEPower(getItemStack(), amt, Actionable.MODULATE));
    }

    @Override
    public boolean onBroadcastChanges(ScreenHandler menu) {
        return ensureItemStillInSlot() && drainPower();
    }

    @Nullable
    @Override
    public MEStorage getInventory() {
        return cellStorage;
    }

    @Override
    public void returnToMainMenu(PlayerEntity player, ISubMenu subMenu) {
        returnMainMenu.accept(player, subMenu);
    }

    @Override
    public ItemStack getMainMenuIcon() {
        return getItemStack();
    }

    @Override
    public IConfigManager getConfigManager() {
        var out = new ConfigManager((manager, settingName) -> {
            manager.writeToNBT(getItemStack().getOrCreateNbt());
        });

        out.registerSetting(Settings.SORT_BY, SortOrder.NAME);
        out.registerSetting(Settings.VIEW_MODE, ViewItems.ALL);
        out.registerSetting(Settings.SORT_DIRECTION, SortDir.ASCENDING);

        out.readFromNBT(getItemStack().getOrCreateNbt().copy());
        return out;
    }
}
