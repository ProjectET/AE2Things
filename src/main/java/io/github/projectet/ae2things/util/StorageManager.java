package io.github.projectet.ae2things.util;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.PersistentState;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class StorageManager extends PersistentState {
    private final Map<UUID, DataStorage> disks;

    public StorageManager() {
        disks = new HashMap<>();
        this.markDirty();
    }

    private StorageManager(Map<UUID, DataStorage> disks) {
        this.disks = disks;
        this.markDirty();
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt) {
        NbtList diskList = new NbtList();
        for (Map.Entry<UUID, DataStorage> entry : disks.entrySet()) {
            NbtCompound disk = new NbtCompound();

            disk.putUuid(Constants.DISKUUID, entry.getKey());
            disk.put(Constants.DISKDATA, entry.getValue().toNbt());
            diskList.add(disk);
        }

        nbt.put(Constants.DISKLIST, diskList);
        return nbt;
    }

    public static StorageManager readNbt(NbtCompound nbt) {
        Map<UUID, DataStorage> disks = new HashMap<>();
        NbtList diskList = nbt.getList(Constants.DISKLIST, NbtCompound.COMPOUND_TYPE);
        for(int i = 0; i < diskList.size(); i++) {
            NbtCompound disk = diskList.getCompound(i);
            disks.put(disk.getUuid(Constants.DISKUUID), DataStorage.fromNbt(disk.getCompound(Constants.DISKDATA)));
        }
        return new StorageManager(disks);
    }

    public void updateDisk(UUID uuid, DataStorage dataStorage) {
        disks.put(uuid, dataStorage);
        markDirty();
    }

    public void removeDisk(UUID uuid) {
        disks.remove(uuid);
        markDirty();
    }

    public boolean hasUUID(UUID uuid) {
        return disks.containsKey(uuid);
    }

    public DataStorage getOrCreateDisk(UUID uuid) {
        if(!disks.containsKey(uuid)) {
            updateDisk(uuid, new DataStorage());
        }
        return disks.get(uuid);
    }

    public void modifyDisk(UUID diskID, NbtList stackKeys, long[] stackAmounts, long itemCount) {
        DataStorage diskToModify = getOrCreateDisk(diskID);
        if(stackKeys != null && stackAmounts != null) {
            diskToModify.stackKeys = stackKeys;
            diskToModify.stackAmounts = stackAmounts;
        }
        diskToModify.itemCount = itemCount;

        updateDisk(diskID, diskToModify);
    }

    public static StorageManager getInstance(MinecraftServer server) {
        ServerWorld world = server.getWorld(ServerWorld.OVERWORLD);
        return world.getPersistentStateManager().getOrCreate(StorageManager::readNbt, StorageManager::new, Constants.MANAGER_NAME);
    }
}
