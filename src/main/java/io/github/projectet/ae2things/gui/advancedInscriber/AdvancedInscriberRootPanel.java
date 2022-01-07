package io.github.projectet.ae2things.gui.advancedInscriber;

import io.github.cottonmc.cotton.gui.client.CottonInventoryScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;

public class AdvancedInscriberRootPanel extends HandledScreen<AdvancedInscriberMenu> {

    public AdvancedInscriberRootPanel(AdvancedInscriberMenu description, PlayerInventory inventory, Text title) {
        super(description, inventory, title);
        width = 176;
        height = 176;
        backgroundHeight = 176;
        backgroundWidth = 176;
    }

    @Override
    protected void drawBackground(MatrixStack matrices, float delta, int mouseX, int mouseY) {

    }
}
