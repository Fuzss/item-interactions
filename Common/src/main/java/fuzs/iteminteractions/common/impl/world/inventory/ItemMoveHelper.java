package fuzs.iteminteractions.common.impl.world.inventory;

import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.tuple.Pair;

import java.util.function.ToIntBiFunction;

public class ItemMoveHelper {

    public static Pair<ItemStack, Integer> addItem(Container container, ItemStack itemStack, int prioritizedSlot, ToIntBiFunction<Container, ItemStack> maxStackSize) {
        ItemStack copiedItem = itemStack.copy();
        prioritizedSlot = moveItemToOccupiedSlotsWithSameType(container, copiedItem, prioritizedSlot, maxStackSize);
        if (copiedItem.isEmpty()) {
            copiedItem = ItemStack.EMPTY;
        } else {
            prioritizedSlot = moveItemToEmptySlots(container, copiedItem, prioritizedSlot);
            if (copiedItem.isEmpty()) {
                copiedItem = ItemStack.EMPTY;
            }
        }

        return Pair.of(copiedItem, prioritizedSlot);
    }

    private static int moveItemToEmptySlots(Container container, ItemStack stack, int prioritizedSlot) {
        prioritizedSlot = setItemInSlot(container, stack, prioritizedSlot);
        if (prioritizedSlot != -1) return prioritizedSlot;
        for (int i = 0; i < container.getContainerSize(); ++i) {
            prioritizedSlot = setItemInSlot(container, stack, i);
            if (prioritizedSlot != -1) {
                return prioritizedSlot;
            }
        }

        return -1;
    }

    private static int setItemInSlot(Container container, ItemStack stack, int slotIndex) {
        if (slotIndex != -1) {
            ItemStack itemStack = container.getItem(slotIndex);
            if (itemStack.isEmpty()) {
                container.setItem(slotIndex, stack.copy());
                stack.setCount(0);
                return slotIndex;
            }
        }

        return -1;
    }

    private static int moveItemToOccupiedSlotsWithSameType(Container container, ItemStack itemStack, int prioritizedSlot, ToIntBiFunction<Container, ItemStack> maxStackSize) {
        prioritizedSlot = addItemToSlot(container, itemStack, prioritizedSlot, maxStackSize);
        if (prioritizedSlot != -1) {
            return prioritizedSlot;
        }

        for (int i = 0; i < container.getContainerSize(); ++i) {
            prioritizedSlot = addItemToSlot(container, itemStack, i, maxStackSize);
            if (prioritizedSlot != -1) {
                return prioritizedSlot;
            }
        }

        return -1;
    }

    private static int addItemToSlot(Container container, ItemStack itemStack, int slotIndex, ToIntBiFunction<Container, ItemStack> maxStackSize) {
        if (slotIndex != -1) {
            ItemStack itemAtSlot = container.getItem(slotIndex);
            if (ItemStack.isSameItemSameComponents(itemAtSlot, itemStack)) {
                moveItemsBetweenStacks(container, itemStack, itemAtSlot, maxStackSize);
                if (itemStack.isEmpty()) {
                    return slotIndex;
                }
            }
        }
        return -1;
    }

    private static void moveItemsBetweenStacks(Container container, ItemStack itemStack, ItemStack otherItem, ToIntBiFunction<Container, ItemStack> maxStackSize) {
        int i = Math.min(container.getMaxStackSize(), maxStackSize.applyAsInt(container, otherItem));
        int j = Math.min(itemStack.getCount(), i - otherItem.getCount());
        if (j > 0) {
            otherItem.grow(j);
            itemStack.shrink(j);
            container.setChanged();
        }
    }
}
