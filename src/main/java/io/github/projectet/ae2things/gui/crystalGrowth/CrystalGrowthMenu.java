package io.github.projectet.ae2things.gui.crystalGrowth;

import appeng.menu.implementations.MenuTypeBuilder;
import appeng.menu.implementations.UpgradeableMenu;
import io.github.projectet.ae2things.block.entity.BECrystalGrowth;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.ScreenHandlerType;

public class CrystalGrowthMenu extends UpgradeableMenu<BECrystalGrowth> {

    public static ScreenHandlerType<CrystalGrowthMenu> CRYSTAL_GROWTH_SHT = MenuTypeBuilder.create(CrystalGrowthMenu::new, BECrystalGrowth.class).build("crystal_growth");

    public CrystalGrowthMenu(int id, PlayerInventory ip, BECrystalGrowth crystalGrowth) {
        super(CRYSTAL_GROWTH_SHT, id, ip, crystalGrowth);
    }
}
