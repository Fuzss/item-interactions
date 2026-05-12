package fuzs.iteminteractions.fabric.mixin;

import fuzs.iteminteractions.fabric.impl.ItemInteractionsFabric;
import fuzs.puzzleslib.common.api.event.v1.core.EventResult;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractContainerMenu.class)
abstract class AbstractContainerMenuFabricMixin {

    @Inject(method = "tryItemClickBehaviourOverride", at = @At("HEAD"), cancellable = true)
    private void tryItemClickBehaviourOverride(Player player, ClickAction clickAction, Slot slot, ItemStack clicked, ItemStack carried, CallbackInfoReturnable<Boolean> callback) {
        EventResult eventResult = ItemInteractionsFabric.ITEM_CLICKED_IN_MENU_EVENT.invoker()
                .onItemClickedInMenu(clicked, slot, carried, this.createCarriedSlotAccess(), clickAction, player);
        if (eventResult.isInterrupt()) {
            callback.setReturnValue(true);
        }
    }

    @Shadow
    private SlotAccess createCarriedSlotAccess() {
        throw new RuntimeException();
    }
}
