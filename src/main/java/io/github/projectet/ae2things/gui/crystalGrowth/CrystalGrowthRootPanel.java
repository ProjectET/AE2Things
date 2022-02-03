package io.github.projectet.ae2things.gui.crystalGrowth;

import appeng.client.gui.implementations.UpgradeableScreen;
import appeng.client.gui.style.ScreenStyle;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;

public class CrystalGrowthRootPanel extends UpgradeableScreen<CrystalGrowthMenu> {

    public CrystalGrowthRootPanel(CrystalGrowthMenu menu, PlayerInventory playerInventory, Text title, ScreenStyle style) {
        super(menu, playerInventory, title, style);
    }
}
