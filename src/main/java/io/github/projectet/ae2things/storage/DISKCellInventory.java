package io.github.projectet.ae2things.storage;

import appeng.api.config.IncludeExclude;
import appeng.api.implementations.items.IUpgradeModule;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.AEKeyType;
import appeng.api.storage.cells.CellState;
import appeng.api.storage.cells.IBasicCellItem;
import appeng.api.storage.cells.ISaveProvider;
import appeng.api.storage.cells.StorageCell;
import appeng.util.prioritylist.FuzzyPriorityList;
import appeng.util.prioritylist.IPartitionList;
import io.github.projectet.ae2things.AE2Things;
import io.github.projectet.ae2things.item.DISKDrive;
import io.github.projectet.ae2things.util.Constants;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;

import java.util.Objects;

public class DISKCellInventory implements StorageCell {

    private final IBasicCellItem cellType;
    public static final String ITEM_COUNT_TAG = "ic";
    public static final String STACK_KEYS = "keys";
    public static final String STACK_AMOUNTS = "amts";

    private final ISaveProvider container;
    private final AEKeyType keyType;
    private IPartitionList partitionList;
    private IncludeExclude partitionListMode;
    private int maxItemTypes;
    private int storedItems;
    private long storedItemCount;
    private Object2LongMap<AEKey> storedAmounts;
    private final ItemStack i;
    private boolean isPersisted = true;

    public DISKCellInventory(DISKDrive cellType, ItemStack stack, ISaveProvider saveProvider) {
        this.cellType = cellType;
        this.i = stack;
        this.container = saveProvider;
        this.keyType = cellType.getKeyType();
        this.maxItemTypes = this.cellType.getTotalTypes(this.i);
        this.storedItems = getTag().getLongArray(STACK_AMOUNTS).length;
        this.storedItemCount = getTag().getLong(ITEM_COUNT_TAG);
        this.storedAmounts = null;
    }

    private void updateFilter() {
        var builder = IPartitionList.builder();

        var upgrades = getUpgradesInventory();
        var config = getConfigInventory();

        boolean hasInverter = false;

        for (var upgrade : upgrades) {
            var u = IUpgradeModule.getTypeFromStack(upgrade);
            if (u != null) {
                switch (u) {
                    case FUZZY -> builder.fuzzyMode(getFuzzyMode());
                    case INVERTER -> hasInverter = true;
                    default -> {
                    }
                }
            }
        }

        builder.addAll(config.keySet());

        partitionListMode = (hasInverter ? IncludeExclude.BLACKLIST : IncludeExclude.WHITELIST);
        partitionList = builder.build();
    }

    private NbtCompound getTag() {
        return AE2Things.STORAGE_INSTANCE.getOrCreateDisk(i.getNbt().getUuid(Constants.DISKUUID));
    }

    public IncludeExclude getPartitionListMode() {
        return partitionListMode;
    }

    public boolean isPreformatted() {
        return !partitionList.isEmpty();
    }

    public boolean isFuzzy() {
        return partitionList instanceof FuzzyPriorityList;
    }

    @Override
    public CellState getStatus() {
        if (this.getStoredItemTypes() == 0) {
            return CellState.EMPTY;
        }
        if (this.canHoldNewItem()) {
            return CellState.NOT_EMPTY;
        }
        return CellState.FULL;
    }

    @Override
    public double getIdleDrain() {
        return 0;
    }

    @Override
    public void persist() {

    }

    @Override
    public Text getDescription() {
        return null;
    }

    public static DISKCellInventory createInventory(ItemStack stack, ISaveProvider saveProvider) {
        Objects.requireNonNull(stack, "Cannot create cell inventory for null itemstack");

        if (!(stack.getItem() instanceof DISKDrive cellType)) {
            return null;
        }

        if (!cellType.isStorageCell(stack)) {
            // This is not an error. Items may decide to not be a storage cell temporarily.
            return null;
        }

        // The cell type's channel matches, so this cast is safe
        return new DISKCellInventory(cellType, stack, saveProvider);
    }

    public long getTotalBytes() {
        return this.cellType.getBytes(this.i);
    }

    public long getFreeBytes() {
        return this.getTotalBytes() - this.getUsedBytes();
    }

    public long getTotalItemTypes() {
        return this.maxItemTypes;
    }

    public long getStoredItemCount() {
        return this.storedItemCount;
    }

    public long getStoredItemTypes() {
        return this.storedItems;
    }

    public boolean canHoldNewItem() {
        final long bytesFree = this.getFreeBytes();
        return (bytesFree > this.getBytesPerType()
                || bytesFree == this.getBytesPerType() && this.getUnusedItemCount() > 0);
    }
}
