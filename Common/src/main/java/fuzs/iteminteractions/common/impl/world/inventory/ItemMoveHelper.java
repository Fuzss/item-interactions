package fuzs.iteminteractions.common.impl.world.inventory;

import it.unimi.dsi.fastutil.ints.IntLinkedOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.ints.IntSets;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;

import java.util.function.ToIntBiFunction;

public class ItemMoveHelper {

    /**
     * @see net.minecraft.world.SimpleContainer#addItem(ItemStack)
     */
    public static ItemSlot addItem(Container container, ItemStack itemStack, int prioritizedSlot, ToIntBiFunction<Container, ItemStack> maxStackSize) {
        ItemStack remainingItems = itemStack.copy();
        int slotNum = moveItemToOccupiedSlotsWithSameType(container, remainingItems, prioritizedSlot, maxStackSize);
        if (remainingItems.isEmpty()) {
            return new ItemSlot(slotNum);
        } else {
            slotNum = moveItemToEmptySlots(container, remainingItems, slotNum);
            if (remainingItems.isEmpty()) {
                return new ItemSlot(slotNum);
            } else {
                return new ItemSlot(slotNum, remainingItems);
            }
        }
    }

    /**
     * @see net.minecraft.world.SimpleContainer#moveItemToEmptySlots(ItemStack)
     */
    private static int moveItemToEmptySlots(Container container, ItemStack sourceStack, int prioritizedSlot) {
        IntSet slotNums = IntLinkedOpenHashSet.of(prioritizedSlot);
        slotNums.addAll(IntSets.fromTo(0, container.getContainerSize()));
        for (int slotNum : slotNums.toIntArray()) {
            if (slotNum >= 0 && slotNum < container.getContainerSize()) {
                ItemStack targetStack = container.getItem(slotNum);
                if (targetStack.isEmpty()) {
                    container.setItem(slotNum, sourceStack.copyAndClear());
                    return slotNum;
                }
            }
        }

        return -1;
    }

    /**
     * @see net.minecraft.world.SimpleContainer#moveItemToOccupiedSlotsWithSameType(ItemStack)
     */
    private static int moveItemToOccupiedSlotsWithSameType(Container container, ItemStack sourceStack, int prioritizedSlot, ToIntBiFunction<Container, ItemStack> maxStackSize) {
        IntSet slotNums = IntLinkedOpenHashSet.of(prioritizedSlot);
        slotNums.addAll(IntSets.fromTo(0, container.getContainerSize()));
        for (int slotNum : slotNums.toIntArray()) {
            if (slotNum >= 0 && slotNum < container.getContainerSize()) {
                ItemStack targetStack = container.getItem(slotNum);
                if (ItemStack.isSameItemSameComponents(targetStack, sourceStack)) {
                    moveItemsBetweenStacks(container, sourceStack, targetStack, maxStackSize);
                    if (sourceStack.isEmpty()) {
                        return slotNum;
                    }
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
