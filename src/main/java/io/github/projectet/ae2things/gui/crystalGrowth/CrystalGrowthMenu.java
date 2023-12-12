package io.github.projectet.ae2things.gui.crystalGrowth;

import appeng.api.inventories.InternalInventory;
import appeng.blockentity.misc.InscriberRecipes;
import appeng.core.definitions.AEItems;
import appeng.core.definitions.ItemDefinition;
import appeng.menu.SlotSemantics;
import appeng.menu.guisync.GuiSync;
import appeng.menu.implementations.MenuTypeBuilder;
import appeng.menu.implementations.UpgradeableMenu;
import appeng.menu.interfaces.IProgressProvider;
import io.github.projectet.ae2things.AE2Things;
import io.github.projectet.ae2things.block.entity.BECrystalGrowth;
import io.github.projectet.ae2things.inventory.CrystalGrowthSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class CrystalGrowthMenu extends UpgradeableMenu<BECrystalGrowth> {

    public static MenuType<CrystalGrowthMenu> CRYSTAL_GROWTH_SHT = MenuTypeBuilder.create(CrystalGrowthMenu::new, BECrystalGrowth.class).build("crystal_growth");;

    private final InternalInventory inventory;
    private final Level world;

    @GuiSync(2)
    public int topRowProg = -1;
    @GuiSync(3)
    public int midRowProg = -1;
    @GuiSync(4)
    public int botRowProg = -1;

    public final IProgressProvider topRow = new IProgressProvider() {
        @Override
        public int getCurrentProgress() {
            return topRowProg;
        }

        @Override
        public int getMaxProgress() {
            return 100;
        }
    };
    public final IProgressProvider midRow = new IProgressProvider() {
        @Override
        public int getCurrentProgress() {
            return midRowProg;
        }

        @Override
        public int getMaxProgress() {
            return 100;
        }
    };
    public final IProgressProvider botRow = new IProgressProvider() {
        @Override
        public int getCurrentProgress() {
            return botRowProg;
        }

        @Override
        public int getMaxProgress() {
            return 100;
        }
    };

    public CrystalGrowthMenu(int id, Inventory ip, BECrystalGrowth crystalGrowth) {
        super(CRYSTAL_GROWTH_SHT, id, ip, crystalGrowth);
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

/*    @Override
    public boolean isValidForSlot(Slot s, ItemStack is) {
        final ItemStack top = inventory.getStackInSlot(0);
        final ItemStack bot = inventory.getStackInSlot(1);

        if (s == this.middle) {
            ItemDefinition<?> press = AEItems.NAME_PRESS;
            if (press.isSameAs(top) || press.isSameAs(bot)) {
                return !press.isSameAs(is);
            }

            return InscriberRecipes.findRecipe(world, is, top, bot, false) != null;
        } else if (s == this.top && !bot.isEmpty() || s == this.bottom && !top.isEmpty()) {
            ItemStack otherSlot;
            if (s == this.top) {
                otherSlot = this.bottom.getItem();
            } else {
                otherSlot = this.top.getItem();
            }

            // name presses
            ItemDefinition<?> namePress = AEItems.NAME_PRESS;
            if (namePress.isSameAs(otherSlot)) {
                return namePress.isSameAs(is);
            }

            // everything else
            // test for a partial recipe match (ignoring the middle slot)
            return InscriberRecipes.isValidOptionalIngredientCombination(world, is, otherSlot);
        }
        return true;
    }*/

    @Override
    protected void standardDetectAndSendChanges() {
        if(isServerSide()) {
            topRowProg = getHost().getTopProg();
            midRowProg = getHost().getMidProg();
            botRowProg = getHost().getBotProg();
        }
        super.standardDetectAndSendChanges();
    }

    @Override
    public boolean stillValid(Player PlayerEntity) {
        return true;
    }
}
