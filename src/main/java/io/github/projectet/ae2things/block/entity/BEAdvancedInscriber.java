package io.github.projectet.ae2things.block.entity;

import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
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
import appeng.api.upgrades.IUpgradeInventory;
import appeng.api.upgrades.IUpgradeableObject;
import appeng.api.upgrades.UpgradeInventories;
import appeng.blockentity.grid.AENetworkPowerBlockEntity;
import appeng.blockentity.misc.InscriberRecipes;
import appeng.core.definitions.AEItems;
import appeng.core.settings.TickRates;
import appeng.me.helpers.MachineSource;
import appeng.recipes.handlers.InscriberProcessType;
import appeng.recipes.handlers.InscriberRecipe;
import appeng.util.inv.AppEngInternalInventory;
import appeng.util.inv.CombinedInternalInventory;
import appeng.util.inv.FilteredInternalInventory;
import appeng.util.inv.filter.IAEItemFilter;
import io.github.projectet.ae2things.AE2Things;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.util.EnumSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

public class BEAdvancedInscriber extends AENetworkPowerBlockEntity implements IGridTickable, IUpgradeableObject {

    // cycles from 0 - 16, at 8 it preforms the action, at 16 it re-enables the
    // normal routine.
    private final AppEngInternalInventory topItemHandler = new AppEngInternalInventory(this, 1, 64);
    private final AppEngInternalInventory botItemHandler = new AppEngInternalInventory(this, 1, 64);
    private final AppEngInternalInventory sideItemHandler = new AppEngInternalInventory(this, 2, 64);

    // The externally visible inventories (with filters applied)
    private final InternalInventory topItemHandlerExtern;
    private final InternalInventory botItemHandlerExtern;
    private final InternalInventory sideItemHandlerExtern;

    private final InternalInventory combinedExtInventory;

    private final InternalInventory inv = new CombinedInternalInventory(this.topItemHandler, this.botItemHandler, this.sideItemHandler);

    private final IUpgradeInventory upgrades;
    private InscriberRecipe cachedTask;
    private int processingTime = 0;
    private final int maxProcessingTime = 100;
    private boolean working;

    private final Map<InternalInventory, ItemStack> lastStacks = new IdentityHashMap<>(Map.of(
            topItemHandler, ItemStack.EMPTY,
            botItemHandler, ItemStack.EMPTY,
            sideItemHandler, ItemStack.EMPTY));


    public BEAdvancedInscriber(BlockPos pos, BlockState state) {
        super(AE2Things.ADVANCED_INSCRIBER_BE, pos, state);

        this.upgrades = UpgradeInventories.forMachine(AE2Things.ADVANCED_INSCRIBER, 5, this::saveChanges);

        this.sideItemHandler.setMaxStackSize(1, 64);

        this.getMainNode()
                .setExposedOnSides(EnumSet.allOf(Direction.class))
                .setIdlePowerUsage(0)
                .addService(IGridTickable.class, this);
        this.setInternalMaxPower(1600);

        var filter = new FilteredInventory();
        this.topItemHandlerExtern = new FilteredInternalInventory(this.topItemHandler, filter);
        this.botItemHandlerExtern = new FilteredInternalInventory(this.botItemHandler, filter);
        this.sideItemHandlerExtern = new FilteredInternalInventory(this.sideItemHandler, filter);

        this.combinedExtInventory = new CombinedInternalInventory(topItemHandlerExtern, botItemHandlerExtern, sideItemHandlerExtern);
    }

    @Override
    public InternalInventory getInternalInventory() {
        return inv;
    }

    @Override
    public InternalInventory getExposedInventoryForSide(Direction facing)  {
        return combinedExtInventory;
    }

    @Override
    public void onChangeInventory(InternalInventory inv, int slot) {
        if (slot == 0) {
            boolean isEmpty = inv.getStackInSlot(0).isEmpty();
            boolean wasEmpty = lastStacks.get(inv).isEmpty();
            lastStacks.put(inv, inv.getStackInSlot(0).copy());
            if (isEmpty != wasEmpty) {
                this.setProcessingTime(0);
            }
        }

        if (!this.isWorking()) {
            this.markForUpdate();
        }

        this.cachedTask = null;
        getMainNode().ifPresent((grid, node) -> grid.getTickManager().wakeDevice(node));
    }

    @Override
    public IUpgradeInventory getUpgrades() {
        return upgrades;
    }

    @Override
    protected boolean readFromStream(FriendlyByteBuf data) {
        var c = super.readFromStream(data);

        var oldWorking = isWorking();
        var newWorking = data.readBoolean();

        if (oldWorking != newWorking && newWorking) {
            working = true;
        }

        for (int i = 0; i < this.inv.size(); i++) {
            this.inv.setItemDirect(i, data.readItem());
        }
        this.cachedTask = null;

        return c;
    }

    @Override
    protected void writeToStream(FriendlyByteBuf data) {
        super.writeToStream(data);

        data.writeBoolean(isWorking());
        for (int i = 0; i < this.inv.size(); i++) {
            data.writeItem(inv.getStackInSlot(i));
        }
    }

    @Nullable
    @Override
    public InternalInventory getSubInventory(ResourceLocation id) {
        if (id.equals(ISegmentedInventory.STORAGE)) {
            return this.getInternalInventory();
        } else if (id.equals(ISegmentedInventory.UPGRADES)) {
            return this.upgrades;
        }

        return super.getSubInventory(id);
    }

    @Nullable
    public InscriberRecipe getTask() {
        if (this.cachedTask == null && level != null) {
            ItemStack input = this.sideItemHandler.getStackInSlot(0);
            ItemStack plateA = this.topItemHandler.getStackInSlot(0);
            ItemStack plateB = this.botItemHandler.getStackInSlot(0);
            if (input.isEmpty()) {
                return null; // No input to handle
            }

            this.cachedTask = InscriberRecipes.findRecipe(level, input, plateA, plateB, true);
        }
        return this.cachedTask;
    }

    @Override
    public void saveAdditional(CompoundTag data) {
        super.saveAdditional(data);
        this.upgrades.writeToNBT(data, "upgrades");
    }

    @Override
    public void loadTag(CompoundTag data) {
        super.loadTag(data);
        this.upgrades.readFromNBT(data, "upgrades");
    }

    @Override
    public void addAdditionalDrops(Level level, BlockPos pos, List<ItemStack> drops) {
        super.addAdditionalDrops(level, pos, drops);

        for (var upgrade : upgrades) {
            drops.add(upgrade);
        }
    }

    private boolean hasWork() {
        if (this.getTask() != null) {
            return true;
        }

        this.setProcessingTime(0);
        return false;
    }

    public boolean isWorking() {
        return working;
    }

    private void matchWork() {
        if(isWorking() != hasWork()) {
            working = hasWork();
            this.markForUpdate();
        }
    }

    private void setProcessingTime(int processingTime) {
        this.processingTime = processingTime;
    }

    @Override
    public TickingRequest getTickingRequest(IGridNode node) {
        return new TickingRequest(TickRates.Inscriber, !this.hasWork(), false);
    }

    @Override
    public TickRateModulation tickingRequest(IGridNode node, int ticksSinceLastCall) {
        matchWork();
        getMainNode().ifPresent(grid -> {
            IEnergyService eg = grid.getEnergyService();
            IEnergySource src = this;
            // Base 1, increase by 1 for each card

            final int speedFactor = 1 + (this.upgrades.getInstalledUpgrades(AEItems.SPEED_CARD) * 3);
            final int powerConsumption = 10 * speedFactor;
            final double powerThreshold = powerConsumption - 0.01;
            double powerReq = this.extractAEPower(powerConsumption, Actionable.SIMULATE, PowerMultiplier.CONFIG);
            if (powerReq <= powerThreshold) {
                src = eg;
                powerReq = eg.extractAEPower(powerConsumption, Actionable.SIMULATE, PowerMultiplier.CONFIG);
            }

            if (powerReq > powerThreshold) {
                src.extractAEPower(powerConsumption, Actionable.MODULATE, PowerMultiplier.CONFIG);
                if (this.getProcessingTime() == 0) {
                    this.setProcessingTime(this.getProcessingTime() + speedFactor);
                } else {
                    this.setProcessingTime(this.getProcessingTime() + ticksSinceLastCall * speedFactor);
                }
            }
        });

        if (this.getProcessingTime() > this.getMaxProcessingTime()) {
            this.setProcessingTime(this.getMaxProcessingTime());
            final InscriberRecipe out = this.getTask();
            if (out != null) {
                final ItemStack outputCopy = out.getResultItem().copy();
                if (this.sideItemHandler.insertItem(1, outputCopy, true).isEmpty()) {
                    this.sideItemHandler.insertItem(1, outputCopy, false);
                    this.setProcessingTime(0);
                    if (out.getProcessType() == InscriberProcessType.PRESS) {
                        this.topItemHandler.extractItem(0, 1, false);
                        this.botItemHandler.extractItem(0, 1, false);
                    }
                    this.sideItemHandler.extractItem(0, 1, false);

                    if (sideItemHandler.getStackInSlot(1).getItem() != Items.AIR) {
                        ItemStack outStack = sideItemHandler.getStackInSlot(1);
                        AEKey itemKey = AEItemKey.of(outStack);
                        long inserted = getMainNode().getGrid().getStorageService().getInventory().insert(itemKey, outStack.getCount(), Actionable.MODULATE, new MachineSource(this));
                        sideItemHandler.extractItem(1, (int) inserted, false);
                    }
                    this.saveChanges();
                }
            }
        }
        matchWork();
        return this.hasWork() ? TickRateModulation.URGENT : TickRateModulation.SLEEP;
    }

    public int getMaxProcessingTime() {
        return maxProcessingTime;
    }

    public int getProcessingTime() {
        return this.processingTime;
    }

    public class FilteredInventory implements IAEItemFilter {
        @Override
        public boolean allowExtract(InternalInventory inv, int slot, int amount) {
            return slot == 1;
        }

        @Override
        public boolean allowInsert(InternalInventory inv, int slot, ItemStack stack) {
            // output slot
            if (slot == 1) {
                return false;
            }

            // only allow if is a proper recipe match
            ItemStack bot = botItemHandler.getStackInSlot(0);
            ItemStack middle = sideItemHandler.getStackInSlot(0);
            ItemStack top = topItemHandler.getStackInSlot(0);

            if (inv == botItemHandler)
                bot = stack;
            if (inv == sideItemHandler)
                middle = stack;
            if (inv == topItemHandler)
                top = stack;

            for (var recipe : InscriberRecipes.getRecipes(getLevel())) {
                if (!middle.isEmpty() && !recipe.getMiddleInput().test(middle)) {
                    continue;
                }
                if(bot.isEmpty() && top.isEmpty())
                    return true;
                else if (bot.isEmpty()) {
                    if (recipe.getTopOptional().test(top) || recipe.getBottomOptional().test(top)) {
                        return true;
                    }
                } else if (top.isEmpty()) {
                    if (recipe.getBottomOptional().test(bot) || recipe.getTopOptional().test(bot)) {
                        return true;
                    }
                } else {
                    if ((recipe.getTopOptional().test(top) && recipe.getBottomOptional().test(bot))
                            || (recipe.getBottomOptional().test(top) && recipe.getTopOptional().test(bot))) {
                        return true;
                    }
                }
            }
            return false;
        }
    }
}
