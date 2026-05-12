package fuzs.iteminteractions.common.mixin;

import fuzs.iteminteractions.common.api.v1.world.item.storage.ItemStorageHolder;
import fuzs.iteminteractions.common.impl.world.item.container.ItemContentsProviders;
import fuzs.iteminteractions.common.impl.world.item.container.ItemInteractionHelper;
import fuzs.puzzleslib.common.api.util.v1.CommonHelper;
import net.minecraft.world.Container;
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
        ItemStorageHolder holder = ItemContentsProviders.get(itemStack);
        if (holder.allowsPlayerInteractions(itemStack, player)) {
            boolean broadcastChanges = ItemInteractionHelper.overrideStackedOnOther(itemStack,
                    () -> holder.getMutableContainer(itemStack, player),
                    slot,
                    clickAction,
                    player,
                    (ItemStack item) -> {
                        return holder.getAcceptableItemCount(itemStack, item, player);
                    },
                    (Container container, ItemStack item) -> holder.storage().getMaxStackSize(container, -1, item));
            if (broadcastChanges) {
                holder.storage().broadcastChangesOnContainerMenu(itemStack, player);
            }

            callback.setReturnValue(broadcastChanges);
        }
    }

    @Inject(method = "overrideOtherStackedOnMe", at = @At("HEAD"), cancellable = true)
    public void overrideOtherStackedOnMe(ItemStack other, Slot slot, ClickAction clickAction, Player player, SlotAccess carriedItem, CallbackInfoReturnable<Boolean> callback) {
        ItemStack itemStack = ItemStack.class.cast(this);
        ItemStorageHolder holder = ItemContentsProviders.get(itemStack);
        if (holder.allowsPlayerInteractions(itemStack, player)) {
            boolean broadcastChanges = ItemInteractionHelper.overrideOtherStackedOnMe(itemStack,
                    () -> holder.getMutableContainer(itemStack, player),
                    other,
                    slot,
                    clickAction,
                    player,
                    carriedItem,
                    (ItemStack item) -> {
                        return holder.getAcceptableItemCount(itemStack, item, player);
                    },
                    (Container container, ItemStack item) -> holder.storage().getMaxStackSize(container, -1, item),
                    () -> holder.storage().onToggleSelectedItem(itemStack, 0, -1));
            if (broadcastChanges) {
                holder.storage().broadcastChangesOnContainerMenu(itemStack, player);
            }

            callback.setReturnValue(broadcastChanges);
        }
    }

    @Inject(method = "getTooltipImage", at = @At("HEAD"), cancellable = true)
    public void getTooltipImage(CallbackInfoReturnable<Optional<TooltipComponent>> callback) {
        ItemStack itemStack = ItemStack.class.cast(this);
        ItemStorageHolder holder = ItemContentsProviders.get(itemStack);
        Player player = CommonHelper.getClientPlayer();
        if (holder.canProvideTooltipImage(itemStack, player)) {
            callback.setReturnValue(holder.getTooltipImage(itemStack, player));
        }
    }
}
