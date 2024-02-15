package fuzs.iteminteractions.impl.world.inventory;

import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.tuple.Pair;

import java.util.function.ToIntBiFunction;

public class ItemMoveHelper {

    public static Pair<ItemStack, Integer> addItem(Container container, ItemStack stack, int prioritizedSlot, ToIntBiFunction<Container, ItemStack> maxStackSize) {
        ItemStack itemStack = stack.copy();
        prioritizedSlot = moveItemToOccupiedSlotsWithSameType(container, itemStack, prioritizedSlot, maxStackSize);
        if (itemStack.isEmpty()) {
            itemStack = ItemStack.EMPTY;
        } else {
            prioritizedSlot = moveItemToEmptySlots(container, itemStack, prioritizedSlot);
            if (itemStack.isEmpty()) {
                itemStack = ItemStack.EMPTY;
            }
        }
        return Pair.of(itemStack, prioritizedSlot);
    }

    private static int moveItemToEmptySlots(Container container, ItemStack stack, int prioritizedSlot) {
        prioritizedSlot = setItemInSlot(container, stack, prioritizedSlot);
        if (prioritizedSlot != -1) return prioritizedSlot;
        for (int i = 0; i < container.getContainerSize(); ++i) {
            prioritizedSlot = setItemInSlot(container, stack, i);
            if (prioritizedSlot != -1) return prioritizedSlot;
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

    private static int moveItemToOccupiedSlotsWithSameType(Container container, ItemStack stack, int prioritizedSlot, ToIntBiFunction<Container, ItemStack> maxStackSize) {
        prioritizedSlot = addItemToSlot(container, stack, prioritizedSlot, maxStackSize);
        if (prioritizedSlot != -1) return prioritizedSlot;
        for (int i = 0; i < container.getContainerSize(); ++i) {
            prioritizedSlot = addItemToSlot(container, stack, i, maxStackSize);
            if (prioritizedSlot != -1) return prioritizedSlot;
        }
        return -1;
    }

    private static int addItemToSlot(Container container, ItemStack stack, int slotIndex, ToIntBiFunction<Container, ItemStack> maxStackSize) {
        if (slotIndex != -1) {
            ItemStack itemStack = container.getItem(slotIndex);
            if (ItemStack.isSameItemSameTags(itemStack, stack)) {
                moveItemsBetweenStacks(container, stack, itemStack, maxStackSize);
                if (stack.isEmpty()) {
                    return slotIndex;
                }
            }
        }
        return -1;
    }

    private static void moveItemsBetweenStacks(Container container, ItemStack stack, ItemStack other, ToIntBiFunction<Container, ItemStack> maxStackSize) {
        int i = Math.min(container.getMaxStackSize(), maxStackSize.applyAsInt(container, other));
        int j = Math.min(stack.getCount(), i - other.getCount());
        if (j > 0) {
            other.grow(j);
            stack.shrink(j);
            container.setChanged();
        }
    }
}
