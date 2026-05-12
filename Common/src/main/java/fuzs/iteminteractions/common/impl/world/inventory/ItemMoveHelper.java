package fuzs.iteminteractions.common.impl.world.inventory;

import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.tuple.Pair;

import java.util.function.ToIntBiFunction;

public class ItemMoveHelper {

    /**
     * @see net.minecraft.world.SimpleContainer#addItem(ItemStack)
     */
    public static Pair<ItemStack, Integer> addItem(Container container, ItemStack itemStack, int prioritizedSlot, ToIntBiFunction<Container, ItemStack> maxStackSize) {
        ItemStack remainingItems = itemStack.copy();
        prioritizedSlot = moveItemToOccupiedSlotsWithSameType(container, remainingItems, prioritizedSlot, maxStackSize);
        if (remainingItems.isEmpty()) {
            remainingItems = ItemStack.EMPTY;
        } else {
            prioritizedSlot = moveItemToEmptySlots(container, remainingItems, prioritizedSlot);
            if (remainingItems.isEmpty()) {
                remainingItems = ItemStack.EMPTY;
            }
        }

        return Pair.of(remainingItems, prioritizedSlot);
    }

    /**
     * @see net.minecraft.world.SimpleContainer#moveItemToEmptySlots(ItemStack)
     */
    private static int moveItemToEmptySlots(Container container, ItemStack sourceStack, int prioritizedSlot) {
        prioritizedSlot = setItemInSlot(container, sourceStack, prioritizedSlot);
        if (prioritizedSlot != -1) {
            return prioritizedSlot;
        }

        for (int i = 0; i < container.getContainerSize(); ++i) {
            prioritizedSlot = setItemInSlot(container, sourceStack, i);
            if (prioritizedSlot != -1) {
                return prioritizedSlot;
            }
        }

        return -1;
    }

    /**
     * @see net.minecraft.world.SimpleContainer#moveItemToEmptySlots(ItemStack)
     */
    private static int setItemInSlot(Container container, ItemStack sourceStack, int slotIndex) {
        if (slotIndex != -1) {
            ItemStack targetStack = container.getItem(slotIndex);
            if (targetStack.isEmpty()) {
                container.setItem(slotIndex, sourceStack.copyAndClear());
                return slotIndex;
            }
        }

        return -1;
    }

    /**
     * @see net.minecraft.world.SimpleContainer#moveItemToOccupiedSlotsWithSameType(ItemStack)
     */
    private static int moveItemToOccupiedSlotsWithSameType(Container container, ItemStack sourceStack, int prioritizedSlot, ToIntBiFunction<Container, ItemStack> maxStackSize) {
        prioritizedSlot = addItemToSlot(container, sourceStack, prioritizedSlot, maxStackSize);
        if (prioritizedSlot != -1) {
            return prioritizedSlot;
        }

        for (int i = 0; i < container.getContainerSize(); ++i) {
            prioritizedSlot = addItemToSlot(container, sourceStack, i, maxStackSize);
            if (prioritizedSlot != -1) {
                return prioritizedSlot;
            }
        }

        return -1;
    }

    /**
     * @see net.minecraft.world.SimpleContainer#moveItemToOccupiedSlotsWithSameType(ItemStack)
     */
    private static int addItemToSlot(Container container, ItemStack sourceStack, int slotIndex, ToIntBiFunction<Container, ItemStack> maxStackSize) {
        if (slotIndex != -1) {
            ItemStack targetStack = container.getItem(slotIndex);
            if (ItemStack.isSameItemSameComponents(targetStack, sourceStack)) {
                moveItemsBetweenStacks(container, sourceStack, targetStack, maxStackSize);
                if (sourceStack.isEmpty()) {
                    return slotIndex;
                }
            }
        }
        return -1;
    }

    /**
     * @see net.minecraft.world.SimpleContainer#moveItemsBetweenStacks(ItemStack, ItemStack)
     */
    private static void moveItemsBetweenStacks(Container container, ItemStack sourceStack, ItemStack targetStack, ToIntBiFunction<Container, ItemStack> maxStackSize) {
        int maxCount = Math.min(container.getMaxStackSize(), maxStackSize.applyAsInt(container, targetStack));
        int diff = Math.min(sourceStack.getCount(), maxCount - targetStack.getCount());
        if (diff > 0) {
            targetStack.grow(diff);
            sourceStack.shrink(diff);
            container.setChanged();
        }
    }
}
