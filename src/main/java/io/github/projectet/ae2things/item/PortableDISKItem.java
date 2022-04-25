package io.github.projectet.ae2things.item;

import appeng.api.config.Actionable;
import appeng.api.config.FuzzyMode;
import appeng.api.implementations.menuobjects.IMenuItem;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.AEKeyType;
import appeng.api.storage.StorageCells;
import appeng.api.storage.StorageHelper;
import appeng.api.storage.cells.CellState;
import appeng.api.upgrades.IUpgradeInventory;
import appeng.api.upgrades.IUpgradeableItem;
import appeng.api.upgrades.UpgradeInventories;
import appeng.block.AEBaseBlockItemChargeable;
import appeng.core.AEConfig;
import appeng.core.AELog;
import appeng.core.definitions.AEItems;
import appeng.core.localization.PlayerMessages;
import appeng.hooks.AEToolItem;
import appeng.items.contents.CellConfig;
import appeng.items.materials.StorageComponentItem;
import appeng.items.tools.powered.PortableCellItem;
import appeng.items.tools.powered.powersink.AEBasePoweredItem;
import appeng.me.helpers.PlayerSource;
import appeng.menu.MenuOpener;
import appeng.menu.locator.MenuLocators;
import appeng.menu.me.common.MEStorageMenu;
import appeng.util.ConfigInventory;
import appeng.util.InteractionUtil;
import io.github.projectet.ae2things.AE2Things;
import io.github.projectet.ae2things.gui.cell.PortableDISKMenuHost;
import io.github.projectet.ae2things.storage.DISKCellHandler;
import io.github.projectet.ae2things.storage.IDISKCellItem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.StackReference;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.recipe.CraftingRecipe;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

public class PortableDISKItem extends AEBasePoweredItem implements IDISKCellItem, IMenuItem, IUpgradeableItem, AEToolItem {

    private final StorageComponentItem item;
    private final AEKeyType keyType = AEKeyType.items();
    private final ScreenHandlerType<?> menuType = MEStorageMenu.PORTABLE_ITEM_CELL_TYPE;

    public PortableDISKItem(StorageComponentItem item, Settings props) {
        super(AEConfig.instance().getPortableCellBattery(), props);
        this.item = item;
    }

    public Identifier getRecipeId() {
        return AE2Things.id("tools/" + Objects.requireNonNull(getRegistryName()).getPath());
    }

    @Override
    public double getChargeRate(ItemStack stack) {
        return 80d + 80d * getUpgrades(stack).getInstalledUpgrades(AEItems.ENERGY_CARD);
    }

    public boolean openFromInventory(PlayerEntity player, int inventorySlot) {
        var is = player.getInventory().getStack(inventorySlot);
        if (is.getItem() == this) {
            return MenuOpener.open(menuType, player, MenuLocators.forInventorySlot(inventorySlot));
        } else {
            return false;
        }
    }

    @Nullable
    @Override
    public PortableDISKMenuHost getMenuHost(PlayerEntity player, int inventorySlot, ItemStack stack, @Nullable BlockPos pos) {
        return new PortableDISKMenuHost(player, inventorySlot, this, stack, (p, sm) -> openFromInventory(p, inventorySlot));
    }

    @Override
    public ActionResult onItemUseFirst(ItemStack stack, ItemUsageContext context) {
        return context.shouldCancelInteraction()
                && this.disassembleDrive(stack, context.getWorld(), context.getPlayer())
                ? ActionResult.success(context.getWorld().isClient())
                : ActionResult.PASS;
    }

    private boolean disassembleDrive(ItemStack stack, World level, PlayerEntity player) {
        if (!AEConfig.instance().isPortableCellDisassemblyEnabled()) {
            return false;
        }

        // We refund the crafting recipe ingredients (the first one each)
        var recipe = level.getRecipeManager().get(getRecipeId()).orElse(null);
        if (!(recipe instanceof CraftingRecipe craftingRecipe)) {
            AELog.debug("Cannot disassemble portable cell because it's crafting recipe doesn't exist: %s",
                    getRecipeId());
            return false;
        }

        if (level.isClient()) {
            return true;
        }

        var playerInventory = player.getInventory();
        if (playerInventory.getMainHandStack() != stack) {
            return false;
        }

        var inv = DISKCellHandler.INSTANCE.getCellInventory(stack, null);
        if (inv == null) {
            return false;
        }

        if (inv.getAvailableStacks().isEmpty()) {
            playerInventory.setStack(playerInventory.selectedSlot, ItemStack.EMPTY);

            var remainingEnergy = getAECurrentPower(stack);
            for (var ingredient : craftingRecipe.getIngredients()) {
                var ingredientStack = ingredient.getMatchingStacks()[0].copy();

                // Dump remaining energy into whatever can accept it
                if (remainingEnergy > 0 && ingredientStack.getItem() instanceof AEBaseBlockItemChargeable chargeable) {
                    remainingEnergy = chargeable.injectAEPower(ingredientStack, remainingEnergy, Actionable.MODULATE);
                }

                playerInventory.offerOrDrop(ingredientStack);
            }

            // Drop upgrades
            for (var upgrade : getUpgrades(stack)) {
                playerInventory.offerOrDrop(upgrade);
            }
        } else {
            player.sendSystemMessage(PlayerMessages.OnlyEmptyCellsCanBeDisassembled.text(), Util.NIL_UUID);
        }

        return true;
    }

    @Override
    public AEKeyType getKeyType() {
        return AEKeyType.items();
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void appendTooltip(ItemStack stack, World level, List<Text> lines,
                              TooltipContext advancedTooltips) {
        super.appendTooltip(stack, level, lines, advancedTooltips);
        addCellInformationToTooltip(stack, lines);
    }

    @Override
    public TypedActionResult<ItemStack> use(World level, PlayerEntity player, Hand hand) {
        if (!InteractionUtil.isInAlternateUseMode(player)
                || !disassembleDrive(player.getStackInHand(hand), level, player)) {
            if (!level.isClient()) {
                MenuOpener.open(getMenuType(), player, MenuLocators.forHand(player, hand));
            }
        }
        return new TypedActionResult<>(ActionResult.success(level.isClient()),
                player.getStackInHand(hand));
    }

    @Override
    public int getBytes(ItemStack cellItem) {
        return this.item.getBytes(cellItem);
    }

    @Override
    public double getIdleDrain() {
        return 0.5;
    }

    @Override
    public boolean isEditable(ItemStack is) {
        return true;
    }

    private void onUpgradesChanged(ItemStack stack, IUpgradeInventory upgrades) {
        var energyCards = upgrades.getInstalledUpgrades(AEItems.ENERGY_CARD);
        // The energy card is crafted with a dense energy cell, while the portable cell just uses a normal energy cell
        // Since the dense cells capacity is 8x the normal capacity, the result should be 9x normal.
        setAEMaxPowerMultiplier(stack, 1 + energyCards * 8);
    }

    @Override
    public IUpgradeInventory getUpgrades(ItemStack is) {
        return UpgradeInventories.forItem(is, 2, this::onUpgradesChanged);
    }

    @Override
    public ConfigInventory getConfigInventory(ItemStack is) {
        return CellConfig.create(keyType.filter(), is);
    }

    @Override
    public FuzzyMode getFuzzyMode(ItemStack is) {
        final String fz = is.getOrCreateNbt().getString("FuzzyMode");
        try {
            return FuzzyMode.valueOf(fz);
        } catch (Throwable t) {
            return FuzzyMode.IGNORE_ALL;
        }
    }

    @Override
    public void setFuzzyMode(ItemStack is, FuzzyMode fzMode) {
        is.getOrCreateNbt().putString("FuzzyMode", fzMode.name());
    }

    @Override
    public boolean allowNbtUpdateAnimation(PlayerEntity player, Hand hand, ItemStack oldStack,
                                           ItemStack newStack) {
        return false;
    }

    public long insert(PlayerEntity player, ItemStack itemStack, AEKey what, long amount, Actionable mode) {
        if (keyType.tryCast(what) == null) {
            return 0;
        }

        var host = getMenuHost(player, -1, itemStack, null);
        if (host == null) {
            return 0;
        }

        var inv = host.getInventory();
        if (inv != null) {
            return StorageHelper.poweredInsert(
                    host,
                    inv,
                    what,
                    amount,
                    new PlayerSource(player),
                    mode);
        }
        return 0;
    }

    public ScreenHandlerType<?> getMenuType() {
        return menuType;
    }

    @Override
    public boolean onStackClicked(ItemStack stack, Slot slot, ClickType action, PlayerEntity player) {
        if (action != ClickType.RIGHT || !slot.canTakePartial(player)) {
            return false;
        }

        var other = slot.getStack();
        if (other.isEmpty()) {
            return true;
        }
        AEKey key = AEItemKey.of(other);
        int inserted = (int) insert(player, stack, key, other.getCount(), Actionable.MODULATE);
        other.decrement(inserted);

        return true;
    }

    /**
     * Allows directly inserting items and fluids into portable cells by right-clicking the cell with the item or bucket
     * in hand.
     */
    @Override
    public boolean onClicked(ItemStack stack, ItemStack other, Slot slot, ClickType action,
                             PlayerEntity player, StackReference access) {
        if (action != ClickType.RIGHT || !slot.canTakePartial(player)) {
            return false;
        }

        if (other.isEmpty()) {
            return false;
        }

        AEKey key = AEItemKey.of(other);
        int inserted = (int) insert(player, stack, key, other.getCount(), Actionable.MODULATE);
        other.decrement(inserted);
        return true;
    }

    public static int getColor(ItemStack stack, int tintIndex) {
        if (tintIndex == 1 && stack.getItem() instanceof PortableCellItem portableCellItem) {
            // If the cell is out of power, always display empty
            if (portableCellItem.getAECurrentPower(stack) <= 0) {
                return CellState.ABSENT.getStateColor();
            }

            // Determine LED color
            var cellInv = StorageCells.getCellInventory(stack, null);
            var cellStatus = cellInv != null ? cellInv.getStatus() : CellState.EMPTY;
            return cellStatus.getStateColor();
        } else {
            // White
            return 0xFFFFFF;
        }
    }
}
