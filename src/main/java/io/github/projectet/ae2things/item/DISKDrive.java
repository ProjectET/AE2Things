package io.github.projectet.ae2things.item;

import appeng.api.config.FuzzyMode;
import appeng.api.stacks.AEKeyType;
import appeng.api.storage.cells.CellState;
import appeng.api.upgrades.IUpgradeInventory;
import appeng.api.upgrades.UpgradeInventories;
import appeng.hooks.AEToolItem;
import appeng.items.contents.CellConfig;
import appeng.util.ConfigInventory;
import appeng.util.InteractionUtil;
import io.github.projectet.ae2things.AE2Things;
import io.github.projectet.ae2things.storage.DISKCellHandler;
import io.github.projectet.ae2things.storage.IDISKCellItem;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static appeng.api.storage.StorageCells.getCellInventory;

public class DISKDrive extends Item implements IDISKCellItem, AEToolItem {

    private final int bytes;
    private final double idleDrain;
    private final ItemConvertible coreItem;

    public DISKDrive(ItemConvertible coreItem, int kilobytes, double idleDrain) {
        super(new FabricItemSettings().maxCount(1).group(AE2Things.ITEM_GROUP).fireproof());
        this.bytes = kilobytes * 1000;
        this.coreItem = coreItem;
        this.idleDrain = idleDrain;
    }

    @Override
    public AEKeyType getKeyType() {
        return AEKeyType.items();
    }

    @Override
    public int getBytes(ItemStack cellItem) {
        return bytes;
    }

    @Override
    public double getIdleDrain() {
        return idleDrain;
    }

    @Override
    public boolean isEditable(ItemStack is) {
        return true;
    }

    @Override
    public ConfigInventory getConfigInventory(ItemStack is) {
        return CellConfig.create(getKeyType().filter(), is);
    }

    @Override
    public FuzzyMode getFuzzyMode(final ItemStack is) {
        final String fz = is.getOrCreateNbt().getString("FuzzyMode");
        if (fz.isEmpty()) {
            return FuzzyMode.IGNORE_ALL;
        }
        try {
            return FuzzyMode.valueOf(fz);
        } catch (final Throwable t) {
            return FuzzyMode.IGNORE_ALL;
        }
    }

    @Override
    public void setFuzzyMode(final ItemStack is, final FuzzyMode fzMode) {
        is.getOrCreateNbt().putString("FuzzyMode", fzMode.name());
    }

    @Override
    public TypedActionResult<ItemStack> use(final World level, final PlayerEntity player, final Hand hand) {
        this.disassembleDrive(player.getStackInHand(hand), level, player);

        return new TypedActionResult<>(ActionResult.success(level.isClient()),
                player.getStackInHand(hand));
    }

    @Nullable
    @Override
    public IUpgradeInventory getUpgrades(ItemStack is) {
        return UpgradeInventories.forItem(is, 2);
    }

    private boolean disassembleDrive(final ItemStack stack, final World level, final PlayerEntity player) {
        if (InteractionUtil.isInAlternateUseMode(player)) {
            if (level.isClient()) {
                return false;
            }

            final PlayerInventory playerInventory = player.getInventory();
            var inv = getCellInventory(stack, null);
            if (inv != null && playerInventory.getMainHandStack() == stack) {
                var list = inv.getAvailableStacks();
                if (list.isEmpty()) {
                    playerInventory.setStack(playerInventory.selectedSlot, ItemStack.EMPTY);

                    // drop core
                    playerInventory.offerOrDrop(new ItemStack(coreItem));

                    // drop upgrades
                    for (ItemStack upgrade : this.getUpgrades(stack)) {
                        playerInventory.offerOrDrop(upgrade);
                    }

                    // drop empty storage cell case
                    playerInventory.offerOrDrop(new ItemStack(AETItems.DISK_HOUSING));

                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public ActionResult onItemUseFirst(ItemStack stack, ItemUsageContext context) {
        return this.disassembleDrive(stack, context.getWorld(), context.getPlayer())
                ? ActionResult.success(context.getWorld().isClient())
                : ActionResult.PASS;
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        tooltip.add(new LiteralText("Deep Item Storage disK - Storage for dummies").formatted(Formatting.DARK_GRAY, Formatting.ITALIC));
        addCellInformationToTooltip(stack, tooltip);
    }

    public static int getColor(ItemStack stack, int tintIndex) {
        if (tintIndex == 1) {
            // Determine LED color
            var cellInv = DISKCellHandler.INSTANCE.getCellInventory(stack, null);
            var cellStatus = cellInv != null ? cellInv.getClientStatus() : CellState.EMPTY;
            return cellStatus.getStateColor();
        } else {
            // White
            return 0xFFFFFF;
        }
    }
}
