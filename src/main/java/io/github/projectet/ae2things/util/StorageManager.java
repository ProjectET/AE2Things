package io.github.projectet.ae2things.util;

import io.github.projectet.ae2things.storage.DISKCellInventory;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.PersistentState;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class StorageManager extends PersistentState {
    private final Map<UUID, NbtCompound> disks;

    public StorageManager() {
        disks = new HashMap<>();
    }

    private StorageManager(Map<UUID, NbtCompound> disks) {
        this.disks = disks;
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt) {
        NbtList diskList = new NbtList();
        for (Map.Entry<UUID, NbtCompound> entry : disks.entrySet()) {
            NbtCompound disk = new NbtCompound();
            NbtCompound data = new NbtCompound();

            data.put(DISKCellInventory.STACK_KEYS, entry.getValue().get(DISKCellInventory.STACK_KEYS));
            data.putLongArray(DISKCellInventory.STACK_AMOUNTS, entry.getValue().getLongArray(DISKCellInventory.STACK_AMOUNTS));

            disk.putUuid(Constants.DISKUUID, entry.getKey());
            disk.put(Constants.DISKDATA, data);
            diskList.add(disk);
        }

        nbt.put(Constants.DISKLIST, diskList);
        return nbt;
    }

    public static StorageManager readNbt(NbtCompound nbt) {
        Map<UUID, NbtCompound> disks = new HashMap<>();
        NbtList diskList = nbt.getList(Constants.DISKLIST, NbtCompound.COMPOUND_TYPE);
        for(int i = 0; i < diskList.size(); i++) {
            NbtCompound disk = diskList.getCompound(i);
            disks.put(disk.getUuid(Constants.DISKUUID), disk.getCompound(Constants.DISKDATA));
        }
        return new StorageManager(disks);
    }

    public void addorUpdateDisk(UUID uuid, NbtCompound nbt) {
        disks.put(uuid, nbt);
        markDirty();
    }

    public void removeDisk(UUID uuid) {
        disks.remove(uuid);
        markDirty();
    }

    public NbtCompound getOrCreateDisk(UUID uuid) {
        if(!disks.containsKey(uuid)) {
            addorUpdateDisk(uuid, new NbtCompound());
        }
        return disks.get(uuid);
    }

    public static StorageManager getInstance(MinecraftServer server) {
        ServerWorld world = server.getWorld(ServerWorld.OVERWORLD);
        return world.getPersistentStateManager().getOrCreate(StorageManager::readNbt, StorageManager::new, Constants.MANAGER_NAME);
    }
}
