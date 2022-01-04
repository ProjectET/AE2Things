package io.github.projectet.ae2things.gui.advancedInscriber;

import appeng.api.inventories.InternalInventory;
import appeng.menu.interfaces.IProgressProvider;
import appeng.menu.slot.OutputSlot;
import appeng.menu.slot.RestrictedInputSlot;
import io.github.cottonmc.cotton.gui.SyncedGuiDescription;
import io.github.projectet.ae2things.block.entity.BEAdvancedInscriber;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.math.BlockPos;

public class AdvancedInscriberMenu extends SyncedGuiDescription implements IProgressProvider {

    public static ScreenHandlerType<AdvancedInscriberMenu> ADVANCED_INSCRIBER_SHT;

    int processingTime;
    int maxProcessingTime = 100;

    public AdvancedInscriberMenu(int syncId, PlayerInventory playerInventory, ScreenHandlerContext context) {
        super(AdvancedInscriberMenu.ADVANCED_INSCRIBER_SHT, syncId, playerInventory, getBlockInventory(context), null);
    }

    public AdvancedInscriberMenu(int syncId, PlayerInventory playerInventory, Inventory inventory, BlockPos pos) {
        super(AdvancedInscriberMenu.ADVANCED_INSCRIBER_SHT, syncId, playerInventory);

        BEAdvancedInscriber blockEntity = (BEAdvancedInscriber) world.getBlockEntity(pos);

        InternalInventory inv = blockEntity.getInternalInventory();
        RestrictedInputSlot top = new RestrictedInputSlot(RestrictedInputSlot.PlacableItemType.INSCRIBER_PLATE, inv, 0);
        addSlot(top);

        RestrictedInputSlot bottom = new RestrictedInputSlot(RestrictedInputSlot.PlacableItemType.INSCRIBER_PLATE, inv, 1);
        addSlot(bottom);

        RestrictedInputSlot middle = new RestrictedInputSlot(RestrictedInputSlot.PlacableItemType.INSCRIBER_INPUT, inv, 2);
        addSlot(middle);

        var output = new OutputSlot(inv, 3, null);
        addSlot(output);

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
