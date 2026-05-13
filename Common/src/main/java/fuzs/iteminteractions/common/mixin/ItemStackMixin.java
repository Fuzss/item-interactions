package fuzs.iteminteractions.common.mixin;

import fuzs.iteminteractions.common.api.v1.world.item.storage.ItemStorageHolder;
import fuzs.puzzleslib.common.api.util.v1.CommonHelper;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(ItemStack.class)
abstract class ItemStackMixin {

    @Inject(method = "overrideStackedOnOther", at = @At("HEAD"), cancellable = true)
    public void overrideStackedOnOther(Slot slot, ClickAction clickAction, Player player, CallbackInfoReturnable<Boolean> callback) {
        ItemStack itemStack = ItemStack.class.cast(this);
        ItemStorageHolder holder = ItemStorageHolder.ofItem(itemStack);
        if (holder.canPlayerInteractWith(itemStack, player)) {
            if (holder.overrideStackedOnOther(itemStack, slot, clickAction, player)) {
                holder.storage().broadcastChangesOnContainerMenu(itemStack, player);
                callback.setReturnValue(Boolean.TRUE);
            } else {
                callback.setReturnValue(Boolean.FALSE);
            }
        }
    }

    @Inject(method = "overrideOtherStackedOnMe", at = @At("HEAD"), cancellable = true)
    public void overrideOtherStackedOnMe(ItemStack itemHeldByCursor, Slot slot, ClickAction clickAction, Player player, SlotAccess slotHeldByCursor, CallbackInfoReturnable<Boolean> callback) {
        ItemStack itemStack = ItemStack.class.cast(this);
        ItemStorageHolder holder = ItemStorageHolder.ofItem(itemStack);
        if (holder.canPlayerInteractWith(itemStack, player)) {
            if (holder.overrideOtherStackedOnMe(itemStack,
                    itemHeldByCursor,
                    slot,
                    clickAction,
                    player,
                    slotHeldByCursor)) {
                holder.storage().broadcastChangesOnContainerMenu(itemStack, player);
                callback.setReturnValue(Boolean.TRUE);
            } else {
                callback.setReturnValue(Boolean.FALSE);
            }
        }
    }

    @Inject(method = "getTooltipImage", at = @At("HEAD"), cancellable = true)
    public void getTooltipImage(CallbackInfoReturnable<Optional<TooltipComponent>> callback) {
        ItemStack itemStack = ItemStack.class.cast(this);
        ItemStorageHolder holder = ItemStorageHolder.ofItem(itemStack);
        Player player = CommonHelper.getClientPlayer();
        Optional<Optional<TooltipComponent>> tooltipImage = holder.getTooltipImage(itemStack, player);
        tooltipImage.ifPresent(callback::setReturnValue);
    }
}
