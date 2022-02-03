package io.github.projectet.ae2things.block.entity;

import appeng.api.inventories.InternalInventory;
import appeng.api.networking.IGridNode;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.upgrades.IUpgradeInventory;
import appeng.api.upgrades.IUpgradeableObject;
import appeng.api.upgrades.UpgradeInventories;
import appeng.blockentity.grid.AENetworkPowerBlockEntity;
import appeng.util.inv.AppEngInternalInventory;
import io.github.projectet.ae2things.AE2Things;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;

public class BECrystalGrowth extends AENetworkPowerBlockEntity implements IGridTickable, IUpgradeableObject {

    private final AppEngInternalInventory inventory = new AppEngInternalInventory(this, 27);
    private IUpgradeInventory upgrades;

    public BECrystalGrowth(BlockPos pos, BlockState state) {
        super(AE2Things.CRYSTAL_GROWTH_BE, pos, state);
        upgrades = UpgradeInventories.forMachine(AE2Things.CRYSTAL_GROWTH, 3, this::saveChanges);
    }

    @Override
    public TickingRequest getTickingRequest(IGridNode node) {
        return null;
    }

    @Override
    public TickRateModulation tickingRequest(IGridNode node, int ticksSinceLastCall) {
        return null;
    }

    @Override
    public InternalInventory getInternalInventory() {
        return inventory;
    }

    @Override
    public void onChangeInventory(InternalInventory inv, int slot) {

    }

    @Override
    public IUpgradeInventory getUpgrades() {
        return upgrades;
    }
}
