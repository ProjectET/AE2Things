package io.github.projectet.ae2things.gui.advancedInscriber;

import appeng.api.inventories.InternalInventory;
import appeng.blockentity.misc.InscriberRecipes;
import appeng.core.definitions.AEItems;
import appeng.core.definitions.ItemDefinition;
import appeng.menu.AEBaseMenu;
import appeng.menu.interfaces.IProgressProvider;
import appeng.menu.slot.OutputSlot;
import appeng.menu.slot.RestrictedInputSlot;
import io.github.cottonmc.cotton.gui.SyncedGuiDescription;
import io.github.projectet.ae2things.block.entity.BEAdvancedInscriber;
import io.github.projectet.ae2things.inventory.CombinedInventory;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class AdvancedInscriberMenu extends AEBaseMenu implements IProgressProvider {

    public static ScreenHandlerType<AdvancedInscriberMenu> ADVANCED_INSCRIBER_SHT;

    int processingTime;
    int maxProcessingTime = 100;

    private CombinedInventory inventory;
    private BEAdvancedInscriber blockEntity;
    public BlockPos blockPos;
    private World world;

    private final Slot top;
    private final Slot middle;
    private final Slot bottom;

    public AdvancedInscriberMenu(int syncId, PlayerInventory playerInventory, PacketByteBuf buf) {
        super(ADVANCED_INSCRIBER_SHT, syncId, null, null);

        blockPos = buf.readBlockPos();
        world = playerInventory.player.world;

        blockEntity = (BEAdvancedInscriber) world.getBlockEntity(blockPos);

        this.inventory = blockEntity;

        RestrictedInputSlot top = new RestrictedInputSlot(RestrictedInputSlot.PlacableItemType.INSCRIBER_PLATE, inventory.getItems(), 0);
        this.top = addSlot(top);

        RestrictedInputSlot bottom = new RestrictedInputSlot(RestrictedInputSlot.PlacableItemType.INSCRIBER_PLATE, inventory.getItems(), 1);
        this.bottom = addSlot(bottom);

        RestrictedInputSlot middle = new RestrictedInputSlot(RestrictedInputSlot.PlacableItemType.INSCRIBER_INPUT, inventory.getItems(), 2);
        this.middle = addSlot(middle);

        var output = new OutputSlot(inventory.getItems(), 3, null);
        addSlot(output);
    }

    public AdvancedInscriberMenu(int syncId, PlayerInventory playerInventory, BlockPos pos) {
        this(syncId, playerInventory, PacketByteBufs.create().writeBlockPos(pos));
    }

    public boolean isValidForSlot(Slot s, ItemStack is) {
        final ItemStack top = inventory.getItems().getStackInSlot(0);
        final ItemStack bot = inventory.getItems().getStackInSlot(1);

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
        return this.processingTime;
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
