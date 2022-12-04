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
import appeng.block.networking.EnergyCellBlockItem;
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
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

public class PortableDISKItem extends AEBasePoweredItem implements IDISKCellItem, IMenuItem, IUpgradeableItem, AEToolItem {

    private final StorageComponentItem item;
    private final AEKeyType keyType = AEKeyType.items();
    private final MenuType<?> menuType = MEStorageMenu.PORTABLE_ITEM_CELL_TYPE;

    public PortableDISKItem(StorageComponentItem item, Properties props) {
        super(AEConfig.instance().getPortableCellBattery(), props);
        this.item = item;
    }

    public ResourceLocation getRecipeId() {
        return AE2Things.id("tools/" + Objects.requireNonNull(getRegistryName()).getPath());
    }

    @Override
    public double getChargeRate(ItemStack stack) {
        return 80d + 80d * getUpgrades(stack).getInstalledUpgrades(AEItems.ENERGY_CARD);
    }

    public boolean openFromInventory(Player player, int inventorySlot) {
        var is = player.getInventory().getItem(inventorySlot);
        if (is.getItem() == this) {
            return MenuOpener.open(menuType, player, MenuLocators.forInventorySlot(inventorySlot));
        } else {
            return false;
        }
    }

    @Nullable
    @Override
    public PortableDISKMenuHost getMenuHost(Player player, int inventorySlot, ItemStack stack, @Nullable BlockPos pos) {
        return new PortableDISKMenuHost(player, inventorySlot, this, stack, (p, sm) -> openFromInventory(p, inventorySlot));
    }

    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        return context.isSecondaryUseActive()
                && this.disassembleDrive(stack, context.getLevel(), context.getPlayer())
                ? InteractionResult.sidedSuccess(context.getLevel().isClientSide())
                : InteractionResult.PASS;
    }

    private boolean disassembleDrive(ItemStack stack, Level level, Player player) {
        if (!AEConfig.instance().isPortableCellDisassemblyEnabled()) {
            return false;
        }

        // We refund the crafting recipe ingredients (the first one each)
        var recipe = level.getRecipeManager().byKey(getRecipeId()).orElse(null);
        if (!(recipe instanceof CraftingRecipe craftingRecipe)) {
            AELog.debug("Cannot disassemble portable cell because it's crafting recipe doesn't exist: %s",
                    getRecipeId());
            return false;
        }

        if (level.isClientSide()) {
            return true;
        }

        var playerInventory = player.getInventory();
        if (playerInventory.getSelected() != stack) {
            return false;
        }

        var inv = DISKCellHandler.INSTANCE.getCellInventory(stack, null);
        if (inv == null) {
            return false;
        }

        if (inv.getAvailableStacks().isEmpty()) {
            playerInventory.setItem(playerInventory.selected, ItemStack.EMPTY);

            var remainingEnergy = getAECurrentPower(stack);
            for (var ingredient : craftingRecipe.getIngredients()) {
                var ingredientStack = ingredient.getItems()[0].copy();

                // Dump remaining energy into whatever can accept it
                if (remainingEnergy > 0 && ingredientStack.getItem() instanceof EnergyCellBlockItem chargeable) {
                    remainingEnergy = chargeable.injectAEPower(ingredientStack, remainingEnergy, Actionable.MODULATE);
                }

                playerInventory.placeItemBackInInventory(ingredientStack);
            }

            // Drop upgrades
            for (var upgrade : getUpgrades(stack)) {
                playerInventory.placeItemBackInInventory(upgrade);
            }
        } else {
            player.sendSystemMessage(PlayerMessages.OnlyEmptyCellsCanBeDisassembled.text());
        }

        return true;
    }

    @Override
    public AEKeyType getKeyType() {
        return AEKeyType.items();
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void appendHoverText(ItemStack stack, Level level, List<Component> lines,
                              TooltipFlag advancedTooltips) {
        super.appendHoverText(stack, level, lines, advancedTooltips);
        addCellInformationToTooltip(stack, lines);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (!InteractionUtil.isInAlternateUseMode(player)
                || !disassembleDrive(player.getItemInHand(hand), level, player)) {
            if (!level.isClientSide()) {
                MenuOpener.open(getMenuType(), player, MenuLocators.forHand(player, hand));
            }
        }
        return new InteractionResultHolder<>(InteractionResult.sidedSuccess(level.isClientSide()),
                player.getItemInHand(hand));
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
        final String fz = is.getOrCreateTag().getString("FuzzyMode");
        try {
            return FuzzyMode.valueOf(fz);
        } catch (Throwable t) {
            return FuzzyMode.IGNORE_ALL;
        }
    }

    @Override
    public void setFuzzyMode(ItemStack is, FuzzyMode fzMode) {
        is.getOrCreateTag().putString("FuzzyMode", fzMode.name());
    }

    @Override
    public boolean allowNbtUpdateAnimation(Player player, InteractionHand hand, ItemStack oldStack,
                                           ItemStack newStack) {
        return false;
    }

    public long insert(Player player, ItemStack itemStack, AEKey what, long amount, Actionable mode) {
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

    public MenuType<?> getMenuType() {
        return menuType;
    }

    @Override
    public boolean overrideStackedOnOther(ItemStack stack, Slot slot, ClickAction action, Player player) {
        if (action != ClickAction.SECONDARY || !slot.allowModification(player)) {
            return false;
        }

        var other = slot.getItem();
        if (other.isEmpty()) {
            return true;
        }
        AEKey key = AEItemKey.of(other);
        int inserted = (int) insert(player, stack, key, other.getCount(), Actionable.MODULATE);
        other.shrink(inserted);

        return true;
    }

    /**
     * Allows directly inserting items and fluids into portable cells by right-clicking the cell with the item or bucket
     * in hand.
     */
    @Override
    public boolean overrideOtherStackedOnMe(ItemStack stack, ItemStack other, Slot slot, ClickAction action,
                             Player player, SlotAccess access) {
        if (action != ClickAction.SECONDARY || !slot.allowModification(player)) {
            return false;
        }

        if (other.isEmpty()) {
            return false;
        }

        AEKey key = AEItemKey.of(other);
        int inserted = (int) insert(player, stack, key, other.getCount(), Actionable.MODULATE);
        other.shrink(inserted);
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
