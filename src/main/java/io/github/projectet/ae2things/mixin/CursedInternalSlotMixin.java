package io.github.projectet.ae2things.mixin;

import io.github.projectet.ae2things.AE2Things;
import io.github.projectet.ae2things.item.DISKDrive;
import io.github.projectet.ae2things.storage.DISKCellInventory;
import io.github.projectet.ae2things.util.Constants;
import io.github.projectet.ae2things.util.DataStorage;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.collection.DefaultedList;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;

@Mixin(ScreenHandler.class)
public abstract class CursedInternalSlotMixin {

    @Final
    @Shadow
    public DefaultedList<Slot> slots;

    @Inject(method = "internalOnSlotClick", at = @At(value = "INVOKE", target = "net/minecraft/screen/slot/Slot.hasStack()Z"), slice = @Slice(from = @At(value = "INVOKE", target = "net/minecraft/item/ItemStack.copy ()Lnet/minecraft/item/ItemStack;")), cancellable = true)
    public void CLONE(int slotIndex, int button, SlotActionType actionType, PlayerEntity player, CallbackInfo ci) {
        Slot i = this.slots.get(slotIndex);
        if(i.getStack().getItem() instanceof DISKDrive && i.getStack().hasNbt()) {
                DataStorage storage = AE2Things.STORAGE_INSTANCE.getOrCreateDisk(i.getStack().getOrCreateNbt().getUuid(Constants.DISKUUID));
                ItemStack newStack = new ItemStack(i.getStack().getItem());
                UUID id = UUID.randomUUID();
                newStack.getOrCreateNbt().putUuid(Constants.DISKUUID, id);
                newStack.getOrCreateNbt().putLong(DISKCellInventory.ITEM_COUNT_TAG, storage.itemCount);
                AE2Things.STORAGE_INSTANCE.updateDisk(id, storage);

                newStack.setCount(newStack.getMaxCount());
                this.setCursorStack(newStack);
                ci.cancel();
        }
    }

    @Shadow
    public abstract void setCursorStack(ItemStack slot);
}
