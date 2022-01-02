package io.github.projectet.ae2things.gui.advancedInscriber;

import io.github.cottonmc.cotton.gui.client.CottonInventoryScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;

public class AdvancedInscriberRootPanel extends CottonInventoryScreen<AdvancedInscriberMenu> {

    public AdvancedInscriberRootPanel(AdvancedInscriberMenu description, PlayerInventory inventory, Text title) {
        super(description, inventory, title);
    }
}
