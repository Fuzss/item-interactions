package fuzs.iteminteractions.mixin;

import fuzs.iteminteractions.api.v1.provider.ItemContainerBehavior;
import fuzs.iteminteractions.impl.world.item.container.ItemContainerProviders;
import fuzs.iteminteractions.impl.world.item.container.ItemInteractionHelper;
import fuzs.puzzleslib.api.core.v1.Proxy;
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
        ItemStack containerStack = ItemStack.class.cast(this);
        ItemContainerBehavior behavior = ItemContainerProviders.INSTANCE.get(containerStack);
        if (behavior.allowsPlayerInteractions(containerStack, player)) {
            boolean result = ItemInteractionHelper.overrideStackedOnOther(() -> behavior.getItemContainer(containerStack,
                            player
                    ),
                    slot,
                    clickAction,
                    player,
                    stack -> behavior.getAcceptableItemCount(containerStack, stack, player),
                    behavior.provider()::getMaxStackSize
            );
            if (result) behavior.provider().broadcastContainerChanges(containerStack, player);
            callback.setReturnValue(result);
        }
    }

    @Inject(method = "overrideOtherStackedOnMe", at = @At("HEAD"), cancellable = true)
    public void overrideOtherStackedOnMe(ItemStack stackOnMe, Slot slot, ClickAction clickAction, Player player, SlotAccess slotAccess, CallbackInfoReturnable<Boolean> callback) {
        ItemStack containerStack = ItemStack.class.cast(this);
        ItemContainerBehavior behavior = ItemContainerProviders.INSTANCE.get(containerStack);
        if (behavior.allowsPlayerInteractions(containerStack, player)) {
            boolean result = ItemInteractionHelper.overrideOtherStackedOnMe(() -> behavior.getItemContainer(
                            containerStack,
                            player
                    ),
                    stackOnMe,
                    slot,
                    clickAction,
                    player,
                    slotAccess,
                    stack -> behavior.getAcceptableItemCount(containerStack, stack, player),
                    behavior.provider()::getMaxStackSize
            );
            if (result) behavior.provider().broadcastContainerChanges(containerStack, player);
            callback.setReturnValue(result);
        }
    }

    @Inject(method = "getTooltipImage", at = @At("HEAD"), cancellable = true)
    public void getTooltipImage(CallbackInfoReturnable<Optional<TooltipComponent>> callback) {
        ItemStack containerStack = ItemStack.class.cast(this);
        ItemContainerBehavior behavior = ItemContainerProviders.INSTANCE.get(containerStack);
        if (behavior.canProvideTooltipImage(containerStack, Proxy.INSTANCE.getClientPlayer())) {
            callback.setReturnValue(behavior.getTooltipImage(containerStack, Proxy.INSTANCE.getClientPlayer()));
        }
    }
}
