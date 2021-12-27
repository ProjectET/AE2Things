package io.github.projectet.ae2things.item;

import appeng.api.config.FuzzyMode;
import appeng.api.implementations.IUpgradeInventory;
import appeng.api.stacks.AEKeyType;
import appeng.hooks.AEToolItem;
import appeng.items.contents.CellConfig;
import appeng.items.contents.CellUpgrades;
import appeng.util.ConfigInventory;
import appeng.util.InteractionUtil;
import com.google.common.base.Preconditions;
import io.github.projectet.ae2things.AE2Things;
import io.github.projectet.ae2things.storage.DISKCellHandler;
import io.github.projectet.ae2things.storage.DISKCellInventory;
import io.github.projectet.ae2things.storage.IDISKCellItem;
import io.github.projectet.ae2things.util.Constants;
import io.github.projectet.ae2things.util.StorageManager;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

import static appeng.api.storage.StorageCells.getCellInventory;

//Acronym is Deep Item Storage disK Drive
public class DISKDrive extends Item implements IDISKCellItem, AEToolItem {

    private final int bytes;
    private final double idleDrain;
    private final ItemConvertible coreItem;

    public DISKDrive(Settings settings, ItemConvertible coreItem, int kilobytes, double idleDrain) {
        super(settings.group(AE2Things.ITEM_GROUP));
        this.bytes = kilobytes * 1000;
        this.coreItem = coreItem;
        this.idleDrain = idleDrain;
    }

    @Override
    public AEKeyType getKeyType() {
        return AEKeyType.items();
    }

    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
        super.inventoryTick(stack, world, entity, slot, selected);
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
    public IUpgradeInventory getUpgradesInventory(ItemStack is) {
        return new CellUpgrades(is, 2);
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
                    for (ItemStack upgrade : this.getUpgradesInventory(stack)) {
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
        if(stack.getOrCreateNbt().contains(Constants.DISKUUID)) {
            tooltip.add(new LiteralText("Disk UUID: " + stack.getOrCreateNbt().getUuid(Constants.DISKUUID)));
            addCellInformationToTooltip(stack, tooltip);
        }
    }
}
