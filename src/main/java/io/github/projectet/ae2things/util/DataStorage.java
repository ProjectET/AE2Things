package io.github.projectet.ae2things.util;

import io.github.projectet.ae2things.storage.DISKCellInventory;
import net.minecraft.nbt.NbtCompound;

//TODO: Impl into StorageManager
public class DataStorage {
    private NbtCompound stackKeys;
    private long[] stackAmounts;
    private long itemCount;

    public DataStorage() {
        stackKeys = new NbtCompound();
        stackAmounts = new long[0];
        itemCount = 0;
    }

    public DataStorage(NbtCompound stackKeys, long[] stackAmounts, long itemCount) {
        this.stackKeys = stackKeys;
        this.stackAmounts = stackAmounts;
        this.itemCount = itemCount;
    }

    public NbtCompound toNbt() {
        NbtCompound nbt = new NbtCompound();
        nbt.put(DISKCellInventory.STACK_KEYS, stackKeys);
        nbt.putLongArray(DISKCellInventory.STACK_AMOUNTS, stackAmounts);
        nbt.putLong(DISKCellInventory.ITEM_COUNT_TAG, itemCount);

        return nbt;
    }

    public static DataStorage fromNbt(NbtCompound nbt) {
        NbtCompound stackKeys = nbt.getCompound(DISKCellInventory.STACK_KEYS);
        long[] stackAmounts = nbt.getLongArray(DISKCellInventory.STACK_AMOUNTS);
        long itemCount = nbt.getLong(DISKCellInventory.ITEM_COUNT_TAG);

        return new DataStorage(stackKeys, stackAmounts, itemCount);
    }

    public long getItemCount() {
        return itemCount;
    }

    public long[] getStackAmounts() {
        return stackAmounts;
    }

    public long getStackAmountAt(int i) {
        return getStackAmounts()[i];
    }

    public NbtCompound getStackKeys() {
        return stackKeys;
    }
}
