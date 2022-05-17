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
import java.util.List;

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

    private int finalStep;

    private final InternalInventory inv = new CombinedInternalInventory(this.topItemHandler, this.botItemHandler, this.sideItemHandler);

    private final IUpgradeInventory upgrades;
    private InscriberRecipe cachedTask;
    private int processingTime = 0;
    private final int maxProcessingTime = 100;
    private boolean smash;
    private long clientStart;


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
            this.setProcessingTime(0);
        }

        if (!this.isSmash()) {
            this.markForUpdate();
        }

        this.cachedTask = null;
        getMainNode().ifPresent((grid, node) -> grid.getTickManager().wakeDevice(node));
    }

    @Override
    public IUpgradeInventory getUpgrades() {
        return upgrades;
    }

    private void setClientStart(long clientStart) {
        this.clientStart = clientStart;
    }

    @Override
    protected boolean readFromStream(FriendlyByteBuf data) {
        var c = super.readFromStream(data);

        var oldSmash = isSmash();
        var newSmash = data.readBoolean();

        if (oldSmash != newSmash && newSmash) {
            setSmash(true);
            setClientStart(System.currentTimeMillis());
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

        data.writeBoolean(isSmash());
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

    public boolean isSmash() {
        return smash;
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
        return this.isSmash();
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
        if (this.isSmash()) {
            this.finalStep++;
            if (this.finalStep == 8) {
                final InscriberRecipe out = this.getTask();
                if (out != null) {
                    final ItemStack outputCopy = out.getResultItem().copy();

                    if (this.sideItemHandler.insertItem(1, outputCopy, false).isEmpty()) {
                        this.setProcessingTime(0);
                        if (out.getProcessType() == InscriberProcessType.PRESS) {
                            this.topItemHandler.extractItem(0, 1, false);
                            this.botItemHandler.extractItem(0, 1, false);
                        }
                        this.sideItemHandler.extractItem(0, 1, false);
                    }

                    if (sideItemHandler.getStackInSlot(1).getItem() != Items.AIR) {
                        ItemStack outStack = sideItemHandler.getStackInSlot(1);
                        AEKey itemKey = AEItemKey.of(outStack);
                        long inserted = getMainNode().getGrid().getStorageService().getInventory().insert(itemKey, outStack.getCount(), Actionable.MODULATE, new MachineSource(this));
                        sideItemHandler.extractItem(1, (int) inserted, false);
                    }
                }
                this.saveChanges();
            } else if (this.finalStep == 16) {
                this.finalStep = 0;
                this.setSmash(false);
                this.markForUpdate();
            }
        } else {
            getMainNode().ifPresent(grid -> {
                IEnergyService eg = grid.getEnergyService();
                IEnergySource src = this;

                // Base 1, increase by 1 for each card
                final int speedFactor = 1 + this.upgrades.getInstalledUpgrades(AEItems.SPEED_CARD);
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
                        this.setSmash(true);
                        this.finalStep = 0;
                        this.markForUpdate();
                    }
                }
            }
        }

        return this.hasWork() ? TickRateModulation.URGENT : TickRateModulation.SLEEP;
    }

    public int getMaxProcessingTime() {
        return maxProcessingTime;
    }

    public int getProcessingTime() {
        return this.processingTime;
    }

    public void setSmash(boolean smash) {
        this.smash = smash;
    }


    public class FilteredInventory implements IAEItemFilter {
        @Override
        public boolean allowExtract(InternalInventory inv, int slot, int amount) {
            if (BEAdvancedInscriber.this.isSmash()) {
                return false;
            }

            return slot == 1;
        }

        @Override
        public boolean allowInsert(InternalInventory inv, int slot, ItemStack stack) {
            // output slot
            if (slot == 1) {
                return false;
            }

            if (BEAdvancedInscriber.this.isSmash()) {
                return false;
            }

            if (inv == BEAdvancedInscriber.this.topItemHandler || inv == BEAdvancedInscriber.this.botItemHandler) {
                ItemStack bot = BEAdvancedInscriber.this.botItemHandler.getStackInSlot(0);
                ItemStack top = BEAdvancedInscriber.this.topItemHandler.getStackInSlot(0);

                if (AEItems.NAME_PRESS.isSameAs(stack)) {
                    return true;
                }

                if (inv == BEAdvancedInscriber.this.topItemHandler) {
                    if (bot == ItemStack.EMPTY || bot == null) {
                        return InscriberRecipes.isValidOptionalIngredient(getLevel(), stack);
                    }
                    return InscriberRecipes.isValidOptionalIngredientCombination(getLevel(), stack, bot);
                }
                else {
                    if (top == ItemStack.EMPTY || top == null) {
                        return InscriberRecipes.isValidOptionalIngredient(getLevel(), stack);
                    }
                    return InscriberRecipes.isValidOptionalIngredientCombination(getLevel(), stack, top);
                }
                //return InscriberRecipes.isValidOptionalIngredient(getWorld(), stack);
            }
            return true;
        }
    }
}
