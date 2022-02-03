package io.github.projectet.ae2things.item;

import appeng.api.client.StorageCellModels;
import appeng.core.definitions.AEItems;
import io.github.projectet.ae2things.AE2Things;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class AETItems {

    public static final Item DISK_HOUSING = new Item(new FabricItemSettings().maxCount(64).group(AE2Things.ITEM_GROUP).fireproof());
    public static final Item DISK_DRIVE_1K = new DISKDrive(AEItems.CELL_COMPONENT_1K.asItem(), 1, 0.5f);
    public static final Item DISK_DRIVE_4K = new DISKDrive(AEItems.CELL_COMPONENT_4K.asItem(), 4, 1.0f);
    public static final Item DISK_DRIVE_16K = new DISKDrive(AEItems.CELL_COMPONENT_16K.asItem(), 16, 1.5f);
    public static final Item DISK_DRIVE_64K = new DISKDrive(AEItems.CELL_COMPONENT_64K.asItem(), 64, 2.0f);

    public static final Identifier MODEL_DISK_DRIVE_1K = new Identifier("ae2things:model/drive/cells/disk_1k");
    public static final Identifier MODEL_DISK_DRIVE_4K = new Identifier("ae2things:model/drive/cells/disk_4k");
    public static final Identifier MODEL_DISK_DRIVE_16K = new Identifier("ae2things:model/drive/cells/disk_16k");
    public static final Identifier MODEL_DISK_DRIVE_64K = new Identifier("ae2things:model/drive/cells/disk_64k");

    public static void init() {
        item(DISK_HOUSING, AE2Things.id("disk_housing"));
        item(DISK_DRIVE_1K, AE2Things.id("disk_drive_1k"));
        item(DISK_DRIVE_4K, AE2Things.id("disk_drive_4k"));
        item(DISK_DRIVE_16K, AE2Things.id("disk_drive_16k"));
        item(DISK_DRIVE_64K, AE2Things.id("disk_drive_64k"));

        StorageCellModels.registerModel(DISK_DRIVE_1K, MODEL_DISK_DRIVE_1K);
        StorageCellModels.registerModel(DISK_DRIVE_4K, MODEL_DISK_DRIVE_4K);
        StorageCellModels.registerModel(DISK_DRIVE_16K, MODEL_DISK_DRIVE_16K);
        StorageCellModels.registerModel(DISK_DRIVE_64K, MODEL_DISK_DRIVE_64K);
    }

    private static void item(Item item, Identifier id) {
        Registry.register(Registry.ITEM, id, item);
    }
}
