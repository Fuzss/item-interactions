package fuzs.iteminteractions.common.impl.client.gui;

import fuzs.puzzleslib.common.api.event.v1.core.EventResult;
import net.minecraft.client.gui.ItemSlotMouseAction;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.OptionalInt;

public interface CustomItemSlotMouseAction extends ItemSlotMouseAction {

    static EventResult onBeforeMouseScroll(AbstractContainerScreen<?> screen, double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        ItemStack itemHeldByCursor = screen.getMenu().getCarried();
        if (!itemHeldByCursor.isEmpty()) {
            for (ItemSlotMouseAction itemMouseAction : screen.itemSlotMouseActions) {
                if (itemMouseAction instanceof CustomItemSlotMouseAction action && action.matches(itemHeldByCursor)
                        && action.onMouseScrolled(horizontalAmount,
                        verticalAmount,
                        OptionalInt.empty(),
                        itemHeldByCursor)) {
                    return EventResult.INTERRUPT;
                }
            }
        }

        return EventResult.PASS;
    }

    boolean matches(ItemStack itemStack);

    default boolean matches(Slot slot) {
        return this.matches(slot.getItem());
    }

    boolean onMouseScrolled(double scrollX, double scrollY, OptionalInt slotIndex, ItemStack itemStack);

    default boolean onMouseScrolled(double scrollX, double scrollY, int slotIndex, ItemStack itemStack) {
        return this.onMouseScrolled(scrollX, scrollY, OptionalInt.of(slotIndex), itemStack);
    }
}
