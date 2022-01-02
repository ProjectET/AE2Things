package io.github.projectet.ae2things.client;

import appeng.api.IAEAddonEntrypoint;
import io.github.projectet.ae2things.gui.advancedInscriber.AdvancedInscriberMenu;
import io.github.projectet.ae2things.gui.advancedInscriber.AdvancedInscriberRootPanel;
import io.github.projectet.ae2things.item.DISKDrive;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.screenhandler.v1.ScreenRegistry;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;

@Environment(EnvType.CLIENT)
public class AE2ThingsClient implements IAEAddonEntrypoint {

    @Override
    public void onAe2Initialized() {
        ScreenRegistry.<AdvancedInscriberMenu, AdvancedInscriberRootPanel>register(AdvancedInscriberMenu.ADVANCED_INSCRIBER_SHT, AdvancedInscriberRootPanel::new);
    }
}
