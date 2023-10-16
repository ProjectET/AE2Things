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
import appeng.api.storage.MEStorage;
import appeng.api.upgrades.IUpgradeInventory;
import appeng.api.upgrades.IUpgradeableObject;
import appeng.api.upgrades.UpgradeInventories;
import appeng.blockentity.grid.AENetworkPowerBlockEntity;
import appeng.core.definitions.AEItems;
import appeng.me.helpers.MachineSource;
import appeng.util.inv.AppEngInternalInventory;
import appeng.util.inv.CombinedInternalInventory;
import appeng.util.inv.FilteredInternalInventory;
import appeng.util.inv.filter.IAEItemFilter;
import io.github.projectet.ae2things.AE2Things;
import io.github.projectet.ae2things.item.AETItems;
import io.github.projectet.ae2things.recipe.CrystalGrowthRecipe;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.util.*;

public class BECrystalGrowth extends AENetworkPowerBlockEntity implements IGridTickable, IUpgradeableObject {
    private final InternalInventory extInventory;

    private final AppEngInternalInventory topRow = new AppEngInternalInventory(this, 4, 64);
    private final AppEngInternalInventory midRow = new AppEngInternalInventory(this, 4, 64);
    private final AppEngInternalInventory botRow = new AppEngInternalInventory(this, 4, 64);

    private final Random r = new Random();

    private final CombinedInternalInventory combInv = new CombinedInternalInventory(topRow, midRow, botRow);

    private final Map<AppEngInternalInventory, Integer> progress = new IdentityHashMap<>(Map.of(topRow, 0, midRow, 0, botRow, 0));
    private final Map<AppEngInternalInventory, CrystalGrowthRecipe> cachedRecipes = new WeakHashMap<>();

    private IUpgradeInventory upgrades;

    private boolean isWorking;

    private final Set<Integer> cachedGrowable = new HashSet<>();

    public BECrystalGrowth(BlockPos pos, BlockState state) {
        super(AE2Things.CRYSTAL_GROWTH_BE, pos, state);
        upgrades = UpgradeInventories.forMachine(AE2Things.CRYSTAL_GROWTH, 3, this::saveChanges);
        var filter = new FilteredInventory();
        this.extInventory = new FilteredInternalInventory(combInv, filter);

        this.getMainNode()
                .setExposedOnSides(EnumSet.allOf(Direction.class))
                .setIdlePowerUsage(0)
                .addService(IGridTickable.class, this);
        this.setInternalMaxPower(1600);
    }

    @Override
    public TickingRequest getTickingRequest(IGridNode node) {
        return new TickingRequest(1, 20, !this.hasWork() && combInv.isEmpty(), true);
    }

    public int getTopProg() {
        return progress.get(topRow);
    }

    public int getMidProg() {
        return progress.get(midRow);
    }

    public int getBotProg() {
        return progress.get(botRow);
    }

    public boolean isWorking() {
        return isWorking;
    }

    public boolean hasWork() {
        return hasFluixIngredients() || !cachedGrowable.isEmpty();
    }

    private boolean hasFluixIngredients() {
            boolean hasRedstone = combInv.simulateRemove(1, new ItemStack(Items.REDSTONE), null).getCount() == 1;
            boolean hasChargedCertus = combInv.simulateRemove(1, new ItemStack(AEItems.CERTUS_QUARTZ_CRYSTAL_CHARGED.asItem()), null).getCount() == 1;
            boolean hasQuartz = combInv.simulateRemove(1, new ItemStack(Items.QUARTZ), null).getCount() == 1;

            return hasRedstone && hasChargedCertus && hasQuartz;

    }

    private ItemStack multiplyYield(ItemStack output) {
        output.setCount(output.getCount() * this.upgrades.getInstalledUpgrades(AETItems.FORTUNE_CARD));
        return output;
    }

    private boolean outputIsEmpty() {
        for (AppEngInternalInventory inv: progress.keySet()) {
            if(inv.getStackInSlot(3) != ItemStack.EMPTY)
                return false;
        }
        return true;
    }

    @Override
    public TickRateModulation tickingRequest(IGridNode node, int ticksSinceLastCall) {
        if (hasWork()) {
            final int speedFactor = 1 + this.upgrades.getInstalledUpgrades(AEItems.SPEED_CARD);
            final IEnergyService[] eg = new IEnergyService[1];
            IEnergySource src = this;
            getMainNode().ifPresent(iGrid -> {
                eg[0] = iGrid.getEnergyService();
            });
            if (eg[0] == null) {
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
                if (!isWorking()) {
                    isWorking = true;
                    markForUpdate();
                }
                src.extractAEPower(powerConsumption, Actionable.MODULATE, PowerMultiplier.CONFIG);
            } else {
                if (isWorking()) {
                    isWorking = false;
                    this.markForUpdate();
                }
                return TickRateModulation.IDLE;
            }
            cachedRecipes.forEach((inventory, recipe) -> {
                if(recipe != null) {
                    int d = progress.get(inventory);
                    if(d >= 100) {
                        ItemStack resultItem = multiplyYield(recipe.getResultItem());
                        if(inventory.insertItem(3, resultItem, true).getCount() != 0)
                            return;
                        int i = 2;
                        while(inventory.getStackInSlot(i) == ItemStack.EMPTY && i > 0) {
                            i--;
                        }
                        ItemStack stack = inventory.getStackInSlot(i);
                        if(stack != ItemStack.EMPTY) {
                            Item item = recipe.nextStage(stack);
                            if(r.nextInt(15) == 0) {
                                inventory.extractItem(i, 1, false);
                                if(item != Items.AIR && i != 2) {
                                    inventory.setItemDirect(i + 1, new ItemStack(item));
                                }
                            }
                        }
                        inventory.insertItem(3, resultItem, false);
                        progress.put(inventory, 0);
                    }
                    else {
                        progress.put(inventory, d + speedFactor);
                    }
                }
            });
            /*
            for (Integer slot : cachedGrowable.stream().toList()) {
                ItemStack crystal = inventory.getStackInSlot(slot);
                if (!(crystal.getItem() instanceof IGrowableCrystal)) {
                    cachedGrowable.remove(slot);
                    continue;
                }
                inventory.setItemDirect(slot, triggerGrowth(crystal, 20, speedFactor));
                this.saveChanges();
            }
            if (hasFluixIngredients()) {
                inventory.removeItems(1, new ItemStack(Items.REDSTONE), null);
                inventory.removeItems(1, new ItemStack(AEItems.CERTUS_QUARTZ_CRYSTAL_CHARGED.asItem()), null);
                inventory.removeItems(1, new ItemStack(Items.QUARTZ), null);
                inventory.addItems(new ItemStack(AEItems.FLUIX_DUST, 2));
                this.saveChanges();
            }
            if (cachedGrowable.isEmpty() && !hasFluixIngredients()) {
                isWorking = false;
                markForUpdate();
            }*/
        }
        if (!outputIsEmpty()) {
            MEStorage gridStorage = getMainNode().getGrid().getStorageService().getInventory();
            for (ItemStack stack: combInv) {
                if (stack.equals(ItemStack.EMPTY) || stack.getItem().equals(Items.AIR))
                    continue;
                if (!FilteredInventory.canTransfer(stack.getItem())) {
                    AEItemKey item = AEItemKey.of(stack);
                    long inserted = gridStorage.insert(item, stack.getCount(), Actionable.MODULATE, new MachineSource(this));
                    stack.shrink((int) inserted);
                }
            }
        }
        return hasWork() ? TickRateModulation.URGENT : !combInv.isEmpty() ? TickRateModulation.SLOWER : TickRateModulation.SLEEP;
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
    protected boolean readFromStream(FriendlyByteBuf data) {
        var c = super.readFromStream(data);

        for (int i = 0; i < this.combInv.size(); i++) {
            this.combInv.setItemDirect(i, data.readItem());
        }

        return c;
    }

    @Override
    protected void writeToStream(FriendlyByteBuf data) {
        super.writeToStream(data);

        for (int i = 0; i < this.combInv.size(); i++) {
            data.writeItem(combInv.getStackInSlot(i));
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

    @Override
    public void saveAdditional(CompoundTag data) {
        super.saveAdditional(data);
        this.upgrades.writeToNBT(data, "upgrades");
        data.putIntArray("cache", this.cachedGrowable.stream().toList());
        data.putBoolean("working", isWorking);
    }

    @Override
    public void loadTag(CompoundTag data) {
        super.loadTag(data);
        this.upgrades.readFromNBT(data, "upgrades");
        this.cachedGrowable.addAll(Arrays.stream(data.getIntArray("cache")).boxed().toList());
        this.isWorking = data.getBoolean("working");
    }

    @Override
    public void addAdditionalDrops(Level level, BlockPos pos, List<ItemStack> drops) {
        super.addAdditionalDrops(level, pos, drops);

        for (var upgrade : upgrades) {
            drops.add(upgrade);
        }
    }

    @Override
    public InternalInventory getInternalInventory() {
        return combInv;
    }

    @Override
    public InternalInventory getExposedInventoryForSide(Direction side) {
        return extInventory;
    }

    @Override
    public void onChangeInventory(InternalInventory inv, int slot) {

        //if(inv.getStackInSlot(slot).getItem() instanceof IGrowableCrystal) {
        //    cachedGrowable.add(slot);
        //}
        this.markForUpdate();
        getMainNode().ifPresent((grid, node) -> grid.getTickManager().wakeDevice(node));
    }

    @Override
    public IUpgradeInventory getUpgrades() {
        return upgrades;
    }

    public class FilteredInventory implements IAEItemFilter {
        public static boolean canTransfer(Item item) {
            return true;
            //return CrystalGrowthSlot.validItems.contains(item) || (item instanceof CrystalSeedItem);
        }

        @Override
        public boolean allowExtract(InternalInventory inv, int slot, int amount) {
            return slot == 3;
        }

        @Override
        public boolean allowInsert(InternalInventory inv, int slot, ItemStack stack) {
            return !(slot == 3) && CrystalGrowthRecipe.getRecipefromStack(getLevel(), stack) != null;
        }
    }
}
