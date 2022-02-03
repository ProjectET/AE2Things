package io.github.projectet.ae2things.gui.advancedInscriber;

import appeng.api.inventories.InternalInventory;
import appeng.api.upgrades.IUpgradeableObject;
import appeng.blockentity.misc.InscriberRecipes;
import appeng.core.definitions.AEItems;
import appeng.core.definitions.ItemDefinition;
import appeng.menu.SlotSemantics;
import appeng.menu.guisync.GuiSync;
import appeng.menu.implementations.MenuTypeBuilder;
import appeng.menu.implementations.UpgradeableMenu;
import appeng.menu.interfaces.IProgressProvider;
import appeng.menu.slot.OutputSlot;
import appeng.menu.slot.RestrictedInputSlot;
import io.github.projectet.ae2things.block.entity.BEAdvancedInscriber;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.Slot;
import net.minecraft.world.World;

public class AdvancedInscriberMenu extends UpgradeableMenu<BEAdvancedInscriber> implements IProgressProvider, IUpgradeableObject {

    public static ScreenHandlerType<AdvancedInscriberMenu> ADVANCED_INSCRIBER_SHT = MenuTypeBuilder.create(AdvancedInscriberMenu::new, BEAdvancedInscriber.class).build("advanced_inscriber");

    private InternalInventory inventory;
    private World world;

    private final Slot top;
    private final Slot middle;
    private final Slot bottom;

    @GuiSync(2)
    public int maxProcessingTime = -1;

    @GuiSync(3)
    public int processingTime = -1;

    public AdvancedInscriberMenu(int syncId, PlayerInventory playerInventory, BEAdvancedInscriber advancedInscriber) {
        super(ADVANCED_INSCRIBER_SHT, syncId, playerInventory, advancedInscriber);
        world = playerInventory.player.world;
        inventory = advancedInscriber.getInternalInventory();

        RestrictedInputSlot top = new RestrictedInputSlot(RestrictedInputSlot.PlacableItemType.INSCRIBER_PLATE, inventory, 0);
        this.top = this.addSlot(top, SlotSemantics.INSCRIBER_PLATE_TOP);

        RestrictedInputSlot bottom = new RestrictedInputSlot(RestrictedInputSlot.PlacableItemType.INSCRIBER_PLATE, inventory,
                1);
        this.bottom = this.addSlot(bottom, SlotSemantics.INSCRIBER_PLATE_BOTTOM);

        RestrictedInputSlot middle = new RestrictedInputSlot(RestrictedInputSlot.PlacableItemType.INSCRIBER_INPUT, inventory,
                2);
        this.middle = this.addSlot(middle, SlotSemantics.MACHINE_INPUT);

        var output = new OutputSlot(inventory, 3, null);
        this.addSlot(output, SlotSemantics.MACHINE_OUTPUT);
    }

    @Override
    protected void standardDetectAndSendChanges() {
        if (isServerSide()) {
            this.maxProcessingTime = getHost().getMaxProcessingTime();
            this.processingTime = getHost().getProcessingTime();
        }
        super.standardDetectAndSendChanges();
    }

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
                otherSlot = this.bottom.getStack();
            } else {
                otherSlot = this.top.getStack();
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
    }

    @Override
    public int getCurrentProgress() {
        return processingTime;
    }

    @Override
    public int getMaxProgress() {
        return this.maxProcessingTime;
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return true;
    }
}
