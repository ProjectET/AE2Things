package io.github.projectet.ae2things.gui.crystalGrowth;

import appeng.api.inventories.InternalInventory;
import appeng.menu.SlotSemantics;
import appeng.menu.implementations.MenuTypeBuilder;
import appeng.menu.implementations.UpgradeableMenu;
import io.github.projectet.ae2things.block.entity.BECrystalGrowth;
import io.github.projectet.ae2things.inventory.CrystalGrowthSlot;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.world.World;

public class CrystalGrowthMenu extends UpgradeableMenu<BECrystalGrowth> {

    public static ScreenHandlerType<CrystalGrowthMenu> CRYSTAL_GROWTH_SHT;

    private final InternalInventory inventory;
    private final World world;

    public CrystalGrowthMenu(int id, PlayerInventory ip, BECrystalGrowth crystalGrowth) {
        super(CRYSTAL_GROWTH_SHT, id, ip, crystalGrowth);
        world = ip.player.world;
        inventory = crystalGrowth.getInternalInventory();
        for(int i = 0; i < inventory.size(); i++) {
            CrystalGrowthSlot slot = new CrystalGrowthSlot(inventory, i);
            this.addSlot(slot, SlotSemantics.STORAGE);
        }
    }
}
