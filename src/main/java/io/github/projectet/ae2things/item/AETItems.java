package io.github.projectet.ae2things.item;

import appeng.api.client.StorageCellModels;
import appeng.api.stacks.AEKeyType;
import appeng.core.definitions.AEItems;
import appeng.items.materials.StorageComponentItem;
import appeng.items.storage.BasicStorageCell;
import appeng.items.tools.powered.PortableCellItem;
import appeng.menu.me.common.MEStorageMenu;
import io.github.projectet.ae2things.AE2Things;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import net.minecraft.util.registry.Registry;

import java.util.ArrayList;
import java.util.List;

public class AETItems {

    public static final PortableCellItem.StorageTier SIZE_256K = new PortableCellItem.StorageTier("256k", 131072, 27, 2048,
            () -> AETItems.CELL_COMPONENT_256K);
    public static final PortableCellItem.StorageTier SIZE_1024K = new PortableCellItem.StorageTier("1024k", 524288, 18, 8192,
            () -> AETItems.CELL_COMPONENT_1024K);
    public static final PortableCellItem.StorageTier SIZE_4096K = new PortableCellItem.StorageTier("4096k", 2097152, 9, 32768,
            () -> AETItems.CELL_COMPONENT_4096K);

    public static final Identifier MODEL_DISK_DRIVE_1K = AE2Things.id("model/drive/cells/disk_1k");
    public static final Identifier MODEL_DISK_DRIVE_4K = AE2Things.id("model/drive/cells/disk_4k");
    public static final Identifier MODEL_DISK_DRIVE_16K = AE2Things.id("model/drive/cells/disk_16k");
    public static final Identifier MODEL_DISK_DRIVE_64K = AE2Things.id("model/drive/cells/disk_64k");

    public static final FabricItemSettings DEFAULT_SETTINGS = new FabricItemSettings().group(AE2Things.ITEM_GROUP);

    private static final List<Pair<Identifier, ? extends Item>> ITEMS = new ArrayList<>();

    public static final Item DISK_HOUSING = item(new Item(DEFAULT_SETTINGS.maxCount(64).fireproof()),"disk_housing");
    public static final DISKDrive DISK_DRIVE_1K = registerCell(AETItems.MODEL_DISK_DRIVE_1K, new DISKDrive(AEItems.CELL_COMPONENT_1K, 1, 0.5f), "disk_drive_1k");
    public static final DISKDrive DISK_DRIVE_4K = registerCell(AETItems.MODEL_DISK_DRIVE_4K, new DISKDrive(AEItems.CELL_COMPONENT_4K, 4, 1.0f), "disk_drive_4k");
    public static final DISKDrive DISK_DRIVE_16K = registerCell(AETItems.MODEL_DISK_DRIVE_16K, new DISKDrive(AEItems.CELL_COMPONENT_16K, 16, 1.5f), "disk_drive_16k");
    public static final DISKDrive DISK_DRIVE_64K = registerCell(AETItems.MODEL_DISK_DRIVE_64K, new DISKDrive(AEItems.CELL_COMPONENT_64K, 64, 2.0f), "disk_drive_64k");

    public static final StorageComponentItem CELL_COMPONENT_256K = item(new StorageComponentItem(DEFAULT_SETTINGS.maxCount(64), 256),"cell_component_256k");
    public static final StorageComponentItem CELL_COMPONENT_1024K = item(new StorageComponentItem(DEFAULT_SETTINGS.maxCount(64), 1024),"cell_component_1024k");
    public static final StorageComponentItem CELL_COMPONENT_4096K = item(new StorageComponentItem(DEFAULT_SETTINGS.maxCount(64), 4096),"cell_component_4096k");

    public static final BasicStorageCell ITEM_CELL_256K = registerCell(new Identifier("ae2:block/drive/cells/64k_item_cell"), new BasicStorageCell(DEFAULT_SETTINGS.maxCount(1), CELL_COMPONENT_256K, AEItems.ITEM_CELL_HOUSING, 2.5f, 256, 2048,63, AEKeyType.items()), "item_storage_cell_256k");
    public static final BasicStorageCell ITEM_CELL_1024K = registerCell(new Identifier("ae2:block/drive/cells/64k_item_cell"), new BasicStorageCell(DEFAULT_SETTINGS.maxCount(1), CELL_COMPONENT_1024K, AEItems.ITEM_CELL_HOUSING, 3.0f, 1024, 8192,63, AEKeyType.items()), "item_storage_cell_1024k");
    public static final BasicStorageCell ITEM_CELL_4096K = registerCell(new Identifier("ae2:block/drive/cells/64k_item_cell"), new BasicStorageCell(DEFAULT_SETTINGS.maxCount(1), CELL_COMPONENT_4096K, AEItems.ITEM_CELL_HOUSING, 3.5f, 4096, 32768,63, AEKeyType.items()), "item_storage_cell_4096k");

    public static final BasicStorageCell FLUID_CELL_256K = registerCell(new Identifier("ae2:block/drive/cells/64k_fluid_cell"), new BasicStorageCell(DEFAULT_SETTINGS.maxCount(1), CELL_COMPONENT_256K, AEItems.FLUID_CELL_HOUSING, 2.5f, 256, 2048,5, AEKeyType.fluids()),"fluid_storage_cell_256k");
    public static final BasicStorageCell FLUID_CELL_1024K = registerCell(new Identifier("ae2:block/drive/cells/64k_fluid_cell"), new BasicStorageCell(DEFAULT_SETTINGS.maxCount(1), CELL_COMPONENT_1024K, AEItems.FLUID_CELL_HOUSING, 3.0f, 1024, 8192,5, AEKeyType.fluids()),"fluid_storage_cell_1024k");
    public static final BasicStorageCell FLUID_CELL_4096K = registerCell(new Identifier("ae2:block/drive/cells/64k_fluid_cell"), new BasicStorageCell(DEFAULT_SETTINGS.maxCount(1), CELL_COMPONENT_4096K, AEItems.FLUID_CELL_HOUSING, 3.5f, 4096, 32768,5, AEKeyType.fluids()),"fluid_storage_cell_4096k");

    public static final Item PORTABLE_DISK_1K = registerPortableDISK("portable_disk_1k", AEItems.CELL_COMPONENT_1K.asItem());
    public static final Item PORTABLE_DISK_4K = registerPortableDISK("portable_disk_4k", AEItems.CELL_COMPONENT_4K.asItem());
    public static final Item PORTABLE_DISK_16K = registerPortableDISK("portable_disk_16k", AEItems.CELL_COMPONENT_16K.asItem());
    public static final Item PORTABLE_DISK_64K = registerPortableDISK("portable_disk_64k", AEItems.CELL_COMPONENT_64K.asItem());

    public static final Item PORTABLE_ITEM_256K = registerPortableItemCell("portable_item_cell_256k", SIZE_256K);
    public static final Item PORTABLE_ITEM_1024K = registerPortableItemCell("portable_item_cell_1024k", SIZE_1024K);
    public static final Item PORTABLE_ITEM_4096K = registerPortableItemCell("portable_item_cell_4096k", SIZE_4096K);

    public static final Item PORTABLE_FLUID_256K = registerPortableFluidCell("portable_fluid_cell_256k", SIZE_256K);
    public static final Item PORTABLE_FLUID_1024K = registerPortableFluidCell("portable_fluid_cell_1024k", SIZE_1024K);
    public static final Item PORTABLE_FLUID_4096K = registerPortableFluidCell("portable_fluid_cell_4096k", SIZE_4096K);

    public static void init() {
        for(Pair<Identifier, ? extends Item> pair : ITEMS) {
            Registry.register(Registry.ITEM, pair.getLeft(), pair.getRight());
        }
    }

    private static <T extends Item> T item(T item, String id) {
        ITEMS.add(new Pair<>(AE2Things.id(id), item));

        return item;
    }

    private static <T extends Item> T registerCell(Identifier model, T item, String id) {
        StorageCellModels.registerModel(item, model);

        return item(item, id);
    }

    private static Item registerPortableDISK(String id, StorageComponentItem sizeComponent) {
        return item(new PortableDISKItem(sizeComponent, DEFAULT_SETTINGS.maxCount(1).fireproof()), id);
    }

    private static Item registerPortableItemCell(String id, PortableCellItem.StorageTier tier) {
        return item(new PortableCellItem(AEKeyType.items(), MEStorageMenu.PORTABLE_ITEM_CELL_TYPE, tier, DEFAULT_SETTINGS.maxCount(1)), id);
    }

    private static Item registerPortableFluidCell(String id, PortableCellItem.StorageTier tier) {
        return item(new PortableCellItem(AEKeyType.fluids(), MEStorageMenu.PORTABLE_FLUID_CELL_TYPE, tier, DEFAULT_SETTINGS.maxCount(1)), id);
    }
}
