package io.github.projectet.ae2things.block.entity;

import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.api.implementations.items.IGrowableCrystal;
import appeng.api.inventories.ISegmentedInventory;
import appeng.api.inventories.InternalInventory;
import appeng.api.networking.IGridNode;
import appeng.api.networking.energy.IEnergyService;
import appeng.api.networking.energy.IEnergySource;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.storage.MEStorage;
import appeng.api.upgrades.IUpgradeInventory;
import appeng.api.upgrades.IUpgradeableObject;
import appeng.api.upgrades.UpgradeInventories;
import appeng.blockentity.grid.AENetworkPowerBlockEntity;
import appeng.core.definitions.AEItems;
import appeng.core.settings.TickRates;
import appeng.items.misc.CrystalSeedItem;
import appeng.me.helpers.MachineSource;
import appeng.util.inv.AppEngInternalInventory;
import appeng.util.inv.FilteredInternalInventory;
import appeng.util.inv.filter.IAEItemFilter;
import io.github.projectet.ae2things.AE2Things;
import io.github.projectet.ae2things.inventory.CrystalGrowthSlot;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.block.BlockState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.*;

public class BECrystalGrowth extends AENetworkPowerBlockEntity implements IGridTickable, IUpgradeableObject {

    private final AppEngInternalInventory inventory = new AppEngInternalInventory(this, 27);
    private final InternalInventory extInventory;

    private IUpgradeInventory upgrades;

    private boolean isWorking;

    private final Set<Integer> cachedGrowable = new HashSet<>();

    public BECrystalGrowth(BlockPos pos, BlockState state) {
        super(AE2Things.CRYSTAL_GROWTH_BE, pos, state);
        upgrades = UpgradeInventories.forMachine(AE2Things.CRYSTAL_GROWTH, 3, this::saveChanges);

        var filter = new FilteredInventory();
        this.extInventory = new FilteredInternalInventory(inventory, filter);

        this.getMainNode()
                .setExposedOnSides(EnumSet.allOf(Direction.class))
                .setIdlePowerUsage(0)
                .addService(IGridTickable.class, this);
        this.setInternalMaxPower(1600);
    }

    @Override
    public TickingRequest getTickingRequest(IGridNode node) {
        return new TickingRequest(TickRates.Inscriber, !this.hasWork(), true);
    }

    public boolean isWorking() {
        return isWorking;
    }

    public boolean hasWork() {
        return hasFluixIngredients() || !cachedGrowable.isEmpty();
    }

    private boolean hasFluixIngredients() {
        Storage<ItemVariant> inv = inventory.toStorage();
        try(Transaction transaction = Transaction.openOuter()) {
            boolean hasRedstone = inv.simulateExtract(ItemVariant.of(Items.REDSTONE), 1, transaction) == 1;
            boolean hasChargedCertus = inv.simulateExtract(ItemVariant.of(AEItems.CERTUS_QUARTZ_CRYSTAL_CHARGED.asItem()), 1, transaction) == 1;;
            boolean hasQuartz = inv.simulateExtract(ItemVariant.of(Items.QUARTZ), 1, transaction) == 1;
            transaction.commit();

            return hasRedstone && hasChargedCertus && hasQuartz;
        }

    }

    @Override
    public TickRateModulation tickingRequest(IGridNode node, int ticksSinceLastCall) {
        if(hasWork()) {
            final int speedFactor = 1 + this.upgrades.getInstalledUpgrades(AEItems.SPEED_CARD);
            final IEnergyService[] eg = new IEnergyService[1];
            IEnergySource src = this;
            getMainNode().ifPresent(iGrid -> {
                eg[0] = iGrid.getEnergyService();
            });
            if(eg[0] == null) {
                return TickRateModulation.IDLE;
            }
            final int powerConsumption = 10 * speedFactor;
            final double powerThreshold = powerConsumption - 0.01;
            double powerReq = this.extractAEPower(powerConsumption, Actionable.SIMULATE, PowerMultiplier.CONFIG);
            if (powerReq <= powerThreshold) {
                src = eg[0];
                powerReq = eg[0].extractAEPower(powerConsumption, Actionable.SIMULATE, PowerMultiplier.CONFIG);
            }

            if (powerReq > powerThreshold) {
                if(!isWorking()) {
                    isWorking = true;
                    markForUpdate();
                }
                src.extractAEPower(powerConsumption, Actionable.MODULATE, PowerMultiplier.CONFIG);
            } else {
                if(isWorking()) {
                    isWorking = false;
                    this.markForUpdate();
                }
                return TickRateModulation.IDLE;
            }

            for (Integer slot : cachedGrowable.stream().toList()) {
                ItemStack crystal = inventory.getStackInSlot(slot);
                if(!(crystal.getItem() instanceof IGrowableCrystal)) {
                    cachedGrowable.remove(slot);
                    continue;
                }
                inventory.setItemDirect(slot, triggerGrowth(crystal, 20, speedFactor));
                this.saveChanges();
            }
            if(hasFluixIngredients()) {
                try(Transaction context = Transaction.openOuter()) {
                    int redstone = inventory.removeItems(1, new ItemStack(Items.REDSTONE), null).getCount();
                    int chargedCertus = inventory.removeItems(1, new ItemStack(AEItems.CERTUS_QUARTZ_CRYSTAL_CHARGED.asItem()), null).getCount();
                    int quartz = inventory.removeItems(1, new ItemStack(Items.QUARTZ), null).getCount();

                    inventory.addItems(new ItemStack(AEItems.FLUIX_DUST, 2));
                    if (redstone == 1 && chargedCertus == 1 && quartz == 1) {
                        context.commit();
                        this.saveChanges();
                    }
                }
            }
            if(cachedGrowable.isEmpty() && !hasFluixIngredients()) {
                isWorking = false;
                markForUpdate();
            }
        }
        if(!inventory.isEmpty()) {
            MEStorage gridStorage = getMainNode().getGrid().getStorageService().getInventory();
            for(ItemStack stack: inventory) {
                if(stack.equals(ItemStack.EMPTY) || stack.getItem().equals(Items.AIR))
                    continue;
                if(!FilteredInventory.canTransfer(stack.getItem())) {
                    AEItemKey item = AEItemKey.of(stack);
                    long inserted = gridStorage.insert(item, stack.getCount(), Actionable.MODULATE, new MachineSource(this));
                    stack.decrement((int) inserted);
                }
            }
        }
        return hasWork() ? TickRateModulation.URGENT : TickRateModulation.SLEEP;
    }

    @Override
    public void setOrientation(final Direction inForward, final Direction inUp) {
        setPowerSides(EnumSet.allOf(Direction.class));
    }

    @Override
    public void onReady() {
        this.getMainNode().setExposedOnSides(EnumSet.allOf(Direction.class));
        super.onReady();
    }

    @Override
    protected boolean readFromStream(PacketByteBuf data) {
        var c = super.readFromStream(data);

        for (int i = 0; i < this.inventory.size(); i++) {
            this.inventory.setItemDirect(i, data.readItemStack());
        }

        return c;
    }

    @Override
    protected void writeToStream(PacketByteBuf data) {
        super.writeToStream(data);

        for (int i = 0; i < this.inventory.size(); i++) {
            data.writeItemStack(inventory.getStackInSlot(i));
        }
    }

    @Nullable
    @Override
    public InternalInventory getSubInventory(Identifier id) {
        if (id.equals(ISegmentedInventory.STORAGE)) {
            return this.getInternalInventory();
        } else if (id.equals(ISegmentedInventory.UPGRADES)) {
            return this.upgrades;
        }

        return super.getSubInventory(id);
    }

    @Override
    public void writeNbt(NbtCompound data) {
        super.writeNbt(data);
        this.upgrades.writeToNBT(data, "upgrades");
        data.putIntArray("cache", this.cachedGrowable.stream().toList());
        data.putBoolean("working", isWorking);
    }

    @Override
    public void loadTag(NbtCompound data) {
        super.loadTag(data);
        this.upgrades.readFromNBT(data, "upgrades");
        this.cachedGrowable.addAll(Arrays.stream(data.getIntArray("cache")).boxed().toList());
        this.isWorking = data.getBoolean("working");
    }

    @Override
    public void addAdditionalDrops(World level, BlockPos pos, List<ItemStack> drops) {
        super.addAdditionalDrops(level, pos, drops);

        for (var upgrade : upgrades) {
            drops.add(upgrade);
        }
    }

    @Override
    public InternalInventory getInternalInventory() {
        return inventory;
    }

    @Override
    public InternalInventory getExposedInventoryForSide(Direction side) {
        return extInventory;
    }

    @Override
    public void onChangeInventory(InternalInventory inv, int slot) {
        if(inv.getStackInSlot(slot).getItem() instanceof IGrowableCrystal) {
            cachedGrowable.add(slot);
        }
        this.markForUpdate();
        getMainNode().ifPresent((grid, node) -> grid.getTickManager().wakeDevice(node));
    }

    @Override
    public IUpgradeInventory getUpgrades() {
        return upgrades;
    }

    public ItemStack triggerGrowth(ItemStack seedItem, int ticks, int multiplier) {
        if(seedItem.getItem() instanceof CrystalSeedItem crystalSeedItem) {
            final int growthTicks = CrystalSeedItem.getGrowthTicks(seedItem) + (ticks * multiplier);
            CrystalSeedItem.setGrowthTicks(seedItem, growthTicks);
            if(CrystalSeedItem.getGrowthTicks(seedItem) >= CrystalSeedItem.GROWTH_TICKS_REQUIRED) {
                return crystalSeedItem.triggerGrowth(seedItem);
            }
        }
        return seedItem;
    }

    public class FilteredInventory implements IAEItemFilter {
        public static boolean canTransfer(Item item) {
            return CrystalGrowthSlot.validItems.contains(item) || (item instanceof CrystalSeedItem);
        }

        @Override
        public boolean allowExtract(InternalInventory inv, int slot, int amount) {
            return !canTransfer(inv.getStackInSlot(slot).getItem());
        }

        @Override
        public boolean allowInsert(InternalInventory inv, int slot, ItemStack stack) {
            return canTransfer(stack.getItem());
        }
    }
}
