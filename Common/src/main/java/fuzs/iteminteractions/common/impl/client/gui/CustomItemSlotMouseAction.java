package fuzs.iteminteractions.common.impl.client.gui;

import fuzs.puzzleslib.common.api.event.v1.core.EventResult;
import net.minecraft.client.gui.ItemSlotMouseAction;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.OptionalInt;

/**
 * An extension for {@link ItemSlotMouseAction} that is also passed the
 * {@link AbstractContainerMenu#getCarried() carried item}.
 */
public interface CustomItemSlotMouseAction extends ItemSlotMouseAction {

    static EventResult onBeforeMouseScroll(AbstractContainerScreen<?> screen, double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        ItemStack itemHeldByCursor = screen.getMenu().getCarried();
        if (!itemHeldByCursor.isEmpty()) {
            for (ItemSlotMouseAction itemMouseAction : screen.itemSlotMouseActions) {
                if (itemMouseAction instanceof CustomItemSlotMouseAction customMouseAction && customMouseAction.matches(
                        itemHeldByCursor)) {
                    customMouseAction.onMouseScrolled(horizontalAmount,
                            verticalAmount,
                            OptionalInt.empty(),
                            itemHeldByCursor);
                    return EventResult.INTERRUPT;
                }
            }
        }

        return EventResult.PASS;
    }

    static EventResult onBeforeKeyPress(AbstractContainerScreen<?> screen, KeyEvent keyEvent) {
        ItemStack itemHeldByCursor = screen.getMenu().getCarried();
        if (!itemHeldByCursor.isEmpty()) {
            for (ItemSlotMouseAction itemMouseAction : screen.itemSlotMouseActions) {
                if (itemMouseAction instanceof CustomItemSlotMouseAction customMouseAction && customMouseAction.matches(
                        itemHeldByCursor) && customMouseAction.onKeyPressed(keyEvent,
                        OptionalInt.empty(),
                        itemHeldByCursor)) {
                    return EventResult.INTERRUPT;
                }
            }
        }

        if (screen.hoveredSlot != null && screen.hoveredSlot.hasItem()) {
            for (ItemSlotMouseAction itemMouseAction : screen.itemSlotMouseActions) {
                if (itemMouseAction.matches(screen.hoveredSlot)
                        && itemMouseAction instanceof CustomItemSlotMouseAction customMouseAction
                        && customMouseAction.onKeyPressed(keyEvent,
                        OptionalInt.of(screen.hoveredSlot.index),
                        screen.hoveredSlot.getItem())) {
                    return EventResult.INTERRUPT;
                }
            }
        }

        return EventResult.PASS;
    }

    default boolean matches(Slot slot) {
        return this.matches(slot.getItem());
    }

    boolean matches(ItemStack itemStack);

    default boolean onMouseScrolled(double scrollX, double scrollY, int slotIndex, ItemStack itemStack) {
        this.onMouseScrolled(scrollX, scrollY, OptionalInt.of(slotIndex), itemStack);
        // Handle this always on purpose, so that other existing behavior for the same item will not run instead.
        return true;
    }

    boolean onMouseScrolled(double scrollX, double scrollY, OptionalInt slotIndex, ItemStack itemStack);

    boolean onKeyPressed(KeyEvent event, OptionalInt slotIndex, ItemStack itemStack);
}
