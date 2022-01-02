package io.github.projectet.ae2things.gui.advancedInscriber;

import io.github.cottonmc.cotton.gui.SyncedGuiDescription;
import net.minecraft.entity.player.PlayerInventory;

public class AdvancedInscriberRootPanel extends SyncedGuiDescription {

    public AdvancedInscriberRootPanel(int syncId, PlayerInventory playerInventory) {
        super(AdvancedInscriberMenu.ADVANCED_INSCRIBER_SHT, syncId, playerInventory);
    }


}
