package io.github.projectet.ae2things.gui.advancedInscriber;

import appeng.menu.interfaces.IProgressProvider;
import io.github.cottonmc.cotton.gui.SyncedGuiDescription;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;

public class AdvancedInscriberMenu extends SyncedGuiDescription implements IProgressProvider {

    public static ScreenHandlerType<AdvancedInscriberMenu> ADVANCED_INSCRIBER_SHT;

    int processingTime;
    int maxProcessingTime = 100;

    public AdvancedInscriberMenu(int syncId, PlayerInventory playerInventory) {
        super(AdvancedInscriberMenu.ADVANCED_INSCRIBER_SHT, syncId, playerInventory);
    }

    public AdvancedInscriberMenu(int syncId, PlayerInventory playerInventory, PacketByteBuf buf) {
        super(AdvancedInscriberMenu.ADVANCED_INSCRIBER_SHT, syncId, playerInventory);
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
