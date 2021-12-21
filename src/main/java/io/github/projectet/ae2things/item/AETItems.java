package io.github.projectet.ae2things.item;

import appeng.core.definitions.AEItems;
import io.github.projectet.ae2things.AE2Things;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.item.Item;

public class AETItems {

    public static final Item DISK_HOUSING = new Item(new FabricItemSettings().maxCount(64).group(AE2Things.ITEM_GROUP));
    public static final Item DISK_DRIVE_1K = new DISKDrive(new FabricItemSettings().maxCount(1), AEItems.CELL_COMPONENT_1K.asItem(), 1, 0.5f);
    public static final Item DISK_DRIVE_4K = new DISKDrive(new FabricItemSettings().maxCount(1), AEItems.CELL_COMPONENT_4K.asItem(), 4, 1.0f);
    public static final Item DISK_DRIVE_16K = new DISKDrive(new FabricItemSettings().maxCount(1), AEItems.CELL_COMPONENT_16K.asItem(), 16, 1.5f);
    public static final Item DISK_DRIVE_64K = new DISKDrive(new FabricItemSettings().maxCount(1), AEItems.CELL_COMPONENT_64K.asItem(), 64, 2.0f);

    public static void init() {

    }
}
