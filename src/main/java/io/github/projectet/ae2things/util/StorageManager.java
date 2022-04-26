package io.github.projectet.ae2things.util;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class StorageManager extends SavedData {
    private final Map<UUID, DataStorage> disks;

    public StorageManager() {
        disks = new HashMap<>();
        this.setDirty();
    }

    private StorageManager(Map<UUID, DataStorage> disks) {
        this.disks = disks;
        this.setDirty();
    }

    @Override
    public CompoundTag save(CompoundTag nbt) {
        ListTag diskList = new ListTag();
        for (Map.Entry<UUID, DataStorage> entry : disks.entrySet()) {
            CompoundTag disk = new CompoundTag();

            disk.putUUID(Constants.DISKUUID, entry.getKey());
            disk.put(Constants.DISKDATA, entry.getValue().toNbt());
            diskList.add(disk);
        }

        nbt.put(Constants.DISKLIST, diskList);
        return nbt;
    }

    public static StorageManager readNbt(CompoundTag nbt) {
        Map<UUID, DataStorage> disks = new HashMap<>();
        ListTag diskList = nbt.getList(Constants.DISKLIST, CompoundTag.TAG_COMPOUND);
        for(int i = 0; i < diskList.size(); i++) {
            CompoundTag disk = diskList.getCompound(i);
            disks.put(disk.getUUID(Constants.DISKUUID), DataStorage.fromNbt(disk.getCompound(Constants.DISKDATA)));
        }
        return new StorageManager(disks);
    }

    public void updateDisk(UUID uuid, DataStorage dataStorage) {
        disks.put(uuid, dataStorage);
        setDirty();
    }

    public void removeDisk(UUID uuid) {
        disks.remove(uuid);
        setDirty();
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

    public void modifyDisk(UUID diskID, ListTag stackKeys, long[] stackAmounts, long itemCount) {
        DataStorage diskToModify = getOrCreateDisk(diskID);
        if(stackKeys != null && stackAmounts != null) {
            diskToModify.stackKeys = stackKeys;
            diskToModify.stackAmounts = stackAmounts;
        }
        diskToModify.itemCount = itemCount;

        updateDisk(diskID, diskToModify);
    }

    public static StorageManager getInstance(MinecraftServer server) {
        ServerLevel world = server.getLevel(ServerLevel.OVERWORLD);
        return world.getDataStorage().computeIfAbsent(StorageManager::readNbt, StorageManager::new, Constants.MANAGER_NAME);
    }
}
