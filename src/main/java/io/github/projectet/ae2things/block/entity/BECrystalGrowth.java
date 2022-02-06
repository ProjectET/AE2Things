package io.github.projectet.ae2things.block.entity;

import appeng.api.implementations.items.IGrowableCrystal;
import appeng.api.inventories.InternalInventory;
import appeng.api.networking.IGridNode;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.upgrades.IUpgradeInventory;
import appeng.api.upgrades.IUpgradeableObject;
import appeng.api.upgrades.UpgradeInventories;
import appeng.blockentity.grid.AENetworkPowerBlockEntity;
import appeng.core.settings.TickRates;
import appeng.util.inv.AppEngInternalInventory;
import io.github.projectet.ae2things.AE2Things;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;

import java.util.HashSet;
import java.util.Set;

public class BECrystalGrowth extends AENetworkPowerBlockEntity implements IGridTickable, IUpgradeableObject {

    private final AppEngInternalInventory inventory = new AppEngInternalInventory(this, 27);
    private IUpgradeInventory upgrades;

    private int ticks = 0;
    private boolean isWorking;

    private Set<Integer> cachedGrowable = new HashSet<>();

    public BECrystalGrowth(BlockPos pos, BlockState state) {
        super(AE2Things.CRYSTAL_GROWTH_BE, pos, state);
        upgrades = UpgradeInventories.forMachine(AE2Things.CRYSTAL_GROWTH, 3, this::saveChanges);
    }

    @Override
    public TickingRequest getTickingRequest(IGridNode node) {
        return new TickingRequest(TickRates.Inscriber, !this.hasWork(), false);
    }

    private boolean hasWork() {
        return isWorking;
    }

    @Override
    public TickRateModulation tickingRequest(IGridNode node, int ticksSinceLastCall) {

        return hasWork() ? TickRateModulation.URGENT : TickRateModulation.SLEEP;
    }

    @Override
    public InternalInventory getInternalInventory() {
        return inventory;
    }

    @Override
    public void onChangeInventory(InternalInventory inv, int slot) {
        if(inv.getStackInSlot(slot).getItem() instanceof IGrowableCrystal) {
            cachedGrowable.add(slot);
            isWorking = true;
        }
        else {
            cachedGrowable.remove(slot);
            if(cachedGrowable.isEmpty()) {
                isWorking = false;
            }
        }
    }

    @Override
    public IUpgradeInventory getUpgrades() {
        return upgrades;
    }
}
