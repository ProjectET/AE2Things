package io.github.projectet.ae2things.client;

import appeng.api.IAEAddonEntrypoint;
import appeng.client.gui.style.ScreenStyle;
import appeng.client.gui.style.StyleManager;
import appeng.items.storage.BasicStorageCell;
import appeng.items.tools.powered.PortableCellItem;
import io.github.projectet.ae2things.gui.advancedInscriber.AdvancedInscriberMenu;
import io.github.projectet.ae2things.gui.advancedInscriber.AdvancedInscriberRootPanel;
import io.github.projectet.ae2things.gui.crystalGrowth.CrystalGrowthMenu;
import io.github.projectet.ae2things.gui.crystalGrowth.CrystalGrowthRootPanel;
import io.github.projectet.ae2things.item.AETItems;
import io.github.projectet.ae2things.item.DISKDrive;
import io.github.projectet.ae2things.item.PortableDISKItem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.fabricmc.fabric.api.client.screenhandler.v1.ScreenRegistry;

@Environment(EnvType.CLIENT)
public class AE2ThingsClient implements IAEAddonEntrypoint {

    @SuppressWarnings("RedundantTypeArguments")
    @Override
    public void onAe2Initialized() {
        ScreenRegistry.<AdvancedInscriberMenu, AdvancedInscriberRootPanel>register(AdvancedInscriberMenu.ADVANCED_INSCRIBER_SHT, (menu, playerInv, title) -> {
            ScreenStyle style;
            try {
                style = StyleManager.loadStyleDoc("/screens/advanced_inscriber.json");
            }
            catch (Exception e) {
                throw new RuntimeException("Failed to read Screen JSON file" , e);
            }

            return new AdvancedInscriberRootPanel(menu, playerInv, title, style);
        });
        ScreenRegistry.<CrystalGrowthMenu, CrystalGrowthRootPanel>register(CrystalGrowthMenu.CRYSTAL_GROWTH_SHT, (menu, playerInv, title) -> {
            ScreenStyle style;
            try {
                style = StyleManager.loadStyleDoc("/screens/crystal_growth.json");
            }
            catch (Exception e) {
                throw new RuntimeException("Failed to read Screen JSON file" , e);
            }

            return new CrystalGrowthRootPanel(menu, playerInv, title, style);
        });

        ColorProviderRegistry.ITEM.register(DISKDrive::getColor, AETItems.DISK_DRIVE_1K, AETItems.DISK_DRIVE_4K, AETItems.DISK_DRIVE_16K, AETItems.DISK_DRIVE_64K);
        ColorProviderRegistry.ITEM.register(BasicStorageCell::getColor, AETItems.FLUID_CELL_256K, AETItems.FLUID_CELL_1024K, AETItems.FLUID_CELL_4096K, AETItems.ITEM_CELL_256K, AETItems.ITEM_CELL_1024K, AETItems.ITEM_CELL_4096K);
        ColorProviderRegistry.ITEM.register(PortableDISKItem::getColor, AETItems.PORTABLE_DISK_1K, AETItems.PORTABLE_DISK_4K, AETItems.PORTABLE_DISK_16K, AETItems.PORTABLE_DISK_64K);
        ColorProviderRegistry.ITEM.register(PortableCellItem::getColor, AETItems.PORTABLE_ITEM_256K, AETItems.PORTABLE_ITEM_1024K, AETItems.PORTABLE_ITEM_4096K, AETItems.PORTABLE_FLUID_256K, AETItems.PORTABLE_FLUID_1024K, AETItems.PORTABLE_FLUID_4096K);
    }
}
