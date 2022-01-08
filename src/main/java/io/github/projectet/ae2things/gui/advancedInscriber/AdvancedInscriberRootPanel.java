package io.github.projectet.ae2things.gui.advancedInscriber;

import appeng.client.gui.implementations.UpgradeableScreen;
import appeng.client.gui.style.ScreenStyle;
import appeng.client.gui.widgets.ProgressBar;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;

public class AdvancedInscriberRootPanel extends UpgradeableScreen<AdvancedInscriberMenu> {

    private final ProgressBar pb;

    public AdvancedInscriberRootPanel(AdvancedInscriberMenu menu, PlayerInventory playerInventory, Text title, ScreenStyle style) {
        super(menu, playerInventory, title, style);
        this.pb = new ProgressBar(this.handler, style.getImage("progressBar"), ProgressBar.Direction.VERTICAL);
        widgets.add("progressBar", this.pb);
    }

    @Override
    protected void updateBeforeRender() {
        super.updateBeforeRender();

        int progress = this.handler.getCurrentProgress() * 100 / this.handler.getMaxProgress();
        this.pb.setFullMsg(new LiteralText(progress + "%"));
    }
}
