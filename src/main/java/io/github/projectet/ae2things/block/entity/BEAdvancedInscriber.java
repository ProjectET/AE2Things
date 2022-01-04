package io.github.projectet.ae2things.block.entity;

import appeng.api.inventories.InternalInventory;
import appeng.blockentity.grid.AENetworkPowerBlockEntity;
import appeng.blockentity.misc.InscriberRecipes;
import appeng.core.definitions.AEItems;
import appeng.recipes.handlers.InscriberRecipe;
import appeng.util.inv.AppEngInternalInventory;
import appeng.util.inv.CombinedInternalInventory;
import appeng.util.inv.FilteredInternalInventory;
import appeng.util.inv.filter.IAEItemFilter;
import io.github.projectet.ae2things.AE2Things;
import io.github.projectet.ae2things.gui.advancedInscriber.AdvancedInscriberMenu;
import io.github.projectet.ae2things.inventory.CombinedInventory;
import io.github.projectet.ae2things.inventory.DefaultInventory;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import javax.annotation.Nullable;
import java.util.EnumSet;

public class BEAdvancedInscriber extends AENetworkPowerBlockEntity implements ExtendedScreenHandlerFactory, CombinedInventory {

    // cycles from 0 - 16, at 8 it preforms the action, at 16 it re-enables the
    // normal routine.
    private final AppEngInternalInventory topItemHandler = new AppEngInternalInventory(this, 1, 64);
    private final AppEngInternalInventory bottomItemHandler = new AppEngInternalInventory(this, 1, 64);
    private final AppEngInternalInventory sideItemHandler = new AppEngInternalInventory(this, 2, 64);

    // The externally visible inventories (with filters applied)
    private final InternalInventory topItemHandlerExtern;
    private final InternalInventory bottomItemHandlerExtern;
    private final InternalInventory sideItemHandlerExtern;

    private final InternalInventory inv = new CombinedInternalInventory(this.topItemHandler,
            this.bottomItemHandler, this.sideItemHandler);

//    private final DefinitionUpgradeInventory upgrades;
    private InscriberRecipe cachedTask;


    public BEAdvancedInscriber(BlockPos pos, BlockState state) {
        super(AE2Things.ADVANCED_INSCRIBER_BE, pos, state);

 //       this.upgrades = new DefinitionUpgradeInventory(AE2Things.ADVANCED_INSCRIBER, this, 3);

        this.sideItemHandler.setMaxStackSize(1, 64);

        var filter = new FilteredInventory();
        this.topItemHandlerExtern = new FilteredInternalInventory(this.topItemHandler, filter);
        this.bottomItemHandlerExtern = new FilteredInternalInventory(this.bottomItemHandler, filter);
        this.sideItemHandlerExtern = new FilteredInternalInventory(this.sideItemHandler, filter);
    }

    @Override
    public InternalInventory getInternalInventory() {
        return inv;
    }

    @Override
    public void onChangeInventory(InternalInventory inv, int slot) {

    }

    @Override
    public void setOrientation(final Direction inForward, final Direction inUp) {
        setPowerSides(EnumSet.allOf(Direction.class));
        getMainNode().setExposedOnSides(EnumSet.allOf(Direction.class));
    }

    @Override
    public void onReady() {
        this.getMainNode().setExposedOnSides(EnumSet.allOf(Direction.class));
        this.getMainNode().create(getWorld(), getBlockEntity().getPos());
    }

    public boolean isSmash() {

        return false;
    }

    @Nullable
    public InscriberRecipe getTask() {
        if (this.cachedTask == null && world != null) {
            ItemStack input = this.sideItemHandler.getStackInSlot(0);
            ItemStack plateA = this.topItemHandler.getStackInSlot(0);
            ItemStack plateB = this.bottomItemHandler.getStackInSlot(0);
            if (input.isEmpty()) {
                return null; // No input to handle
            }

            this.cachedTask = InscriberRecipes.findRecipe(world, input, plateA, plateB, true);
        }
        return this.cachedTask;
    }

    @Override
    public Text getDisplayName() {
        return new LiteralText("Advanced Inscriber");
    }

    @Nullable
    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory inv, PlayerEntity player) {
        return new AdvancedInscriberMenu(syncId, inv, this, getPos());
    }

    @Override
    public void writeScreenOpeningData(ServerPlayerEntity player, PacketByteBuf buf) {
        buf.writeBlockPos(getPos());
    }

    @Override
    public CombinedInternalInventory getItems() {
        return (CombinedInternalInventory) inv;
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

            if (inv == BEAdvancedInscriber.this.topItemHandler || inv == BEAdvancedInscriber.this.bottomItemHandler) {
                if (AEItems.NAME_PRESS.isSameAs(stack)) {
                    return true;
                }
                return InscriberRecipes.isValidOptionalIngredient(getWorld(), stack);
            }
            return true;
        }
    }
}
