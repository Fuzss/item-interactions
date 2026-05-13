package fuzs.iteminteractions.common.mixin;

import fuzs.iteminteractions.common.api.v1.world.item.storage.ItemStorageHolder;
import fuzs.puzzleslib.common.api.util.v1.CommonHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(ItemStack.class)
abstract class ItemStackMixin {

    @Inject(method = "getTooltipImage", at = @At("HEAD"), cancellable = true)
    public void getTooltipImage(CallbackInfoReturnable<Optional<TooltipComponent>> callback) {
        ItemStack itemStack = ItemStack.class.cast(this);
        ItemStorageHolder holder = ItemStorageHolder.ofItem(itemStack);
        Player player = CommonHelper.getClientPlayer();
        Optional<Optional<TooltipComponent>> tooltipImage = holder.getTooltipImage(itemStack, player);
        tooltipImage.ifPresent(callback::setReturnValue);
    }
}
