package io.github.projectet.ae2things.gui.crystalGrowth;

import appeng.api.inventories.InternalInventory;
import appeng.menu.SlotSemantics;
import appeng.menu.implementations.MenuTypeBuilder;
import appeng.menu.implementations.UpgradeableMenu;
import io.github.projectet.ae2things.block.entity.BECrystalGrowth;
import io.github.projectet.ae2things.inventory.CrystalGrowthSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.Level;

public class CrystalGrowthMenu extends UpgradeableMenu<BECrystalGrowth> {

    public static MenuType<CrystalGrowthMenu> CRYSTAL_GROWTH_SHT;

    private final InternalInventory inventory;
    private final Level world;

    public CrystalGrowthMenu(int id, Inventory ip, BECrystalGrowth crystalGrowth) {
        super(CRYSTAL_GROWTH_SHT, id, ip, crystalGrowth);
        world = ip.player.level;
        inventory = crystalGrowth.getInternalInventory();
        for(int i = 0; i < inventory.size(); i++) {
            CrystalGrowthSlot slot = new CrystalGrowthSlot(inventory, i);
            this.addSlot(slot, SlotSemantics.STORAGE);
        }
    }
}
