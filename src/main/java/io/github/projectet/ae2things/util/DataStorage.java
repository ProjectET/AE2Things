package io.github.projectet.ae2things.util;

import io.github.projectet.ae2things.storage.DISKCellInventory;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;

public class DataStorage {

    public static final DataStorage EMPTY = new DataStorage();

    public NbtList stackKeys;
    public long[] stackAmounts;
    public long itemCount;

    public DataStorage() {
        stackKeys = new NbtList();
        stackAmounts = new long[0];
        itemCount = 0;
    }

    public DataStorage(NbtList stackKeys, long[] stackAmounts, long itemCount) {
        this.stackKeys = stackKeys;
        this.stackAmounts = stackAmounts;
        this.itemCount = itemCount;
    }

    public NbtCompound toNbt() {
        NbtCompound nbt = new NbtCompound();
        nbt.put(DISKCellInventory.STACK_KEYS, stackKeys);
        nbt.putLongArray(DISKCellInventory.STACK_AMOUNTS, stackAmounts);
        if(itemCount != 0) nbt.putLong(DISKCellInventory.ITEM_COUNT_TAG, itemCount);

        return nbt;
    }

    public static DataStorage fromNbt(NbtCompound nbt) {
        long itemCount = 0;
        NbtList stackKeys = nbt.getList(DISKCellInventory.STACK_KEYS, NbtElement.COMPOUND_TYPE);
        long[] stackAmounts = nbt.getLongArray(DISKCellInventory.STACK_AMOUNTS);
        if(nbt.contains(DISKCellInventory.ITEM_COUNT_TAG))
            itemCount = nbt.getLong(DISKCellInventory.ITEM_COUNT_TAG);

        return new DataStorage(stackKeys, stackAmounts, itemCount);
    }
}
