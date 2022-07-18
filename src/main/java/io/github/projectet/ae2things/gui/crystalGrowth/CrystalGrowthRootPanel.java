package io.github.projectet.ae2things.gui.crystalGrowth;

import appeng.client.gui.implementations.UpgradeableScreen;
import appeng.client.gui.style.ScreenStyle;
import appeng.client.gui.widgets.ProgressBar;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class CrystalGrowthRootPanel extends UpgradeableScreen<CrystalGrowthMenu> {

    private final ProgressBar pb1;
    private final ProgressBar pb2;
    private final ProgressBar pb3;

    public CrystalGrowthRootPanel(CrystalGrowthMenu menu, Inventory playerInventory, Component title, ScreenStyle style) {
        super(menu, playerInventory, title, style);
        this.pb1 = new ProgressBar(this.menu.topRow, style.getImage("progressBar"), ProgressBar.Direction.VERTICAL);
        this.pb2 = new ProgressBar(this.menu.midRow, style.getImage("progressBar"), ProgressBar.Direction.VERTICAL);
        this.pb3 = new ProgressBar(this.menu.botRow, style.getImage("progressBar"), ProgressBar.Direction.VERTICAL);
        widgets.add("pb1", this.pb1);
        widgets.add("pb2", this.pb2);
        widgets.add("pb3", this.pb3);
    }

    @Override
    protected void updateBeforeRender() {
        super.updateBeforeRender();

        this.pb1.setFullMsg(Component.literal((this.menu.topRow.getCurrentProgress() * 100 / this.menu.topRow.getMaxProgress()) + "%"));
        this.pb2.setFullMsg(Component.literal((this.menu.midRow.getCurrentProgress() * 100 / this.menu.midRow.getMaxProgress()) + "%"));
        this.pb3.setFullMsg(Component.literal((this.menu.botRow.getCurrentProgress() * 100 / this.menu.botRow.getMaxProgress()) + "%"));
    }
}
