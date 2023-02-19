package io.github.projectet.ae2things.gui.crystalGrowth;

import appeng.api.inventories.InternalInventory;
import appeng.menu.SlotSemantics;
import appeng.menu.implementations.UpgradeableMenu;
import appeng.menu.interfaces.IProgressProvider;
import io.github.projectet.ae2things.AE2Things;
import io.github.projectet.ae2things.block.entity.BECrystalGrowth;
import io.github.projectet.ae2things.inventory.CrystalGrowthSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.Level;

public class CrystalGrowthMenu extends UpgradeableMenu<BECrystalGrowth> {

    public static MenuType<CrystalGrowthMenu> CRYSTAL_GROWTH_SHT;

    private final InternalInventory inventory;
    private final Level world;

    public final IProgressProvider topRow;
    public final IProgressProvider midRow;
    public final IProgressProvider botRow;


    public CrystalGrowthMenu(int id, Inventory ip, BECrystalGrowth crystalGrowth) {
        super(CRYSTAL_GROWTH_SHT, id, ip, crystalGrowth);

        topRow = new IProgressProvider() {
            @Override
            public int getCurrentProgress() {
                return crystalGrowth.getTopProg();
            }

            @Override
            public int getMaxProgress() {
                return 100;
            }
        };

        midRow = new IProgressProvider() {
            @Override
            public int getCurrentProgress() {
                return crystalGrowth.getMidProg();
            }

            @Override
            public int getMaxProgress() {
                return 100;
            }
        };

        botRow = new IProgressProvider() {
            @Override
            public int getCurrentProgress() {
                return crystalGrowth.getBotProg();
            }

            @Override
            public int getMaxProgress() {
                return 100;
            }
        };

        world = ip.player.level;
        inventory = crystalGrowth.getInternalInventory();
        int i = 0;
        for(int y = 0; y < 3; y++) {
            for(int x = 0; x < 4; x++) {
                int xx = 26 + (x * 36);
                int yy = 17 + (y * 18);
                CrystalGrowthSlot slot = new CrystalGrowthSlot(inventory, i, xx, yy);
                this.addSlot(slot, AE2Things.CG_SEMANTIC);
                i++;
            }
        }
    }
}
