package io.github.projectet.ae2things.util;

import io.github.projectet.ae2things.storage.DISKCellInventory;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

public class DataStorage {

    public static final DataStorage EMPTY = new DataStorage();

    public ListTag stackKeys;
    public long[] stackAmounts;
    public long itemCount;

    public DataStorage() {
        stackKeys = new ListTag();
        stackAmounts = new long[0];
        itemCount = 0;
    }

    public DataStorage(ListTag stackKeys, long[] stackAmounts, long itemCount) {
        this.stackKeys = stackKeys;
        this.stackAmounts = stackAmounts;
        this.itemCount = itemCount;
    }

    public CompoundTag toNbt() {
        CompoundTag nbt = new CompoundTag();
        nbt.put(DISKCellInventory.STACK_KEYS, stackKeys);
        nbt.putLongArray(DISKCellInventory.STACK_AMOUNTS, stackAmounts);
        if(itemCount != 0) nbt.putLong(DISKCellInventory.ITEM_COUNT_TAG, itemCount);

        return nbt;
    }

    public static DataStorage fromNbt(CompoundTag nbt) {
        long itemCount = 0;
        ListTag stackKeys = nbt.getList(DISKCellInventory.STACK_KEYS, Tag.TAG_COMPOUND);
        long[] stackAmounts = nbt.getLongArray(DISKCellInventory.STACK_AMOUNTS);
        if(nbt.contains(DISKCellInventory.ITEM_COUNT_TAG))
            itemCount = nbt.getLong(DISKCellInventory.ITEM_COUNT_TAG);

        return new DataStorage(stackKeys, stackAmounts, itemCount);
    }
}
