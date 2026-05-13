package fuzs.iteminteractions.common.api.v1.world.item.storage;

import fuzs.iteminteractions.common.impl.init.ModRegistry;
import fuzs.iteminteractions.common.impl.world.inventory.ItemSlot;
import fuzs.iteminteractions.common.impl.world.item.component.SelectedItem;
import it.unimi.dsi.fastutil.ints.IntLinkedOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.ints.IntSets;
import net.minecraft.util.Mth;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import org.joml.Vector2i;
import org.joml.Vector2ic;

import java.util.function.Predicate;
import java.util.function.ToIntFunction;

public interface ContainerItemStorage extends ItemStorage {

    int getGridWidth(int itemCount);

    int getGridHeight(int itemCount);

    @Override
    default int getSelectedItem(ItemStack itemStack) {
        return itemStack.getOrDefault(ModRegistry.SELECTED_ITEM_DATA_COMPONENT_TYPE.value(), SelectedItem.DEFAULT)
                .selectedItem();
    }

    default void setSelectedItem(ItemStack itemStack, int selectedItem) {
        itemStack.set(ModRegistry.SELECTED_ITEM_DATA_COMPONENT_TYPE.value(),
                selectedItem == SelectedItem.DEFAULT_SELECTED_ITEM ? SelectedItem.DEFAULT :
                        SelectedItem.of(selectedItem));
    }

    @Override
    default void toggleSelectedItem(ItemStack itemStack, int selectedItem) {
        this.setSelectedItem(itemStack, selectedItem);
    }

    @Override
    default int scrollSelectedItem(ItemStack itemStack, Container container, Vector2ic scrollXY) {
        int selectedItem = this.getSelectedItem(itemStack);
        int gridWidth = this.getGridWidth(container.getContainerSize());
        int gridHeight = this.getGridHeight(container.getContainerSize());
        int gridSize = gridWidth * gridHeight;
        for (int slotNum = 0; slotNum < gridSize; slotNum++) {
            int x = selectedItem % gridWidth;
            int y = selectedItem / gridWidth;
            if (scrollXY.x() == 0) {
                y += Mth.sign(scrollXY.y());
                if (y < 0) {
                    y = gridHeight - 1;
                    x--;
                } else if (y >= gridHeight) {
                    y = 0;
                    x++;
                }

                x = Mth.positiveModulo(x, gridWidth);
            } else {
                x += Mth.sign(scrollXY.x());
                if (x < 0) {
                    x = gridWidth - 1;
                    y--;
                } else if (x >= gridWidth) {
                    x = 0;
                    y++;
                }

                y = Mth.positiveModulo(y, gridHeight);
            }

            selectedItem = y * gridWidth + x;
            if (selectedItem < container.getContainerSize() && !container.getItem(selectedItem).isEmpty()) {
                return selectedItem;
            }
        }

        return SelectedItem.DEFAULT_SELECTED_ITEM;
    }

    private int pickSelectedSlot(Container container, ItemStack itemStack, Predicate<ItemStack> itemFilter, ToIntFunction<ItemStack> amountToRemove) {
        int selectedItem = this.getSelectedItem(itemStack);
        if (selectedItem >= 0 && selectedItem < container.getContainerSize()) {
            ItemStack item = container.getItem(selectedItem);
            if (!item.isEmpty() && itemFilter.test(item)) {
                // When we empty the slot, cycle to a different one.
                if (item.getCount() <= amountToRemove.applyAsInt(item)) {
                    int updatedSelectedItem = this.scrollSelectedItem(itemStack, container, new Vector2i(-1, 0));
                    this.setSelectedItem(itemStack, updatedSelectedItem);
                }

                return selectedItem;
            }
        }

        for (int slotNum = container.getContainerSize() - 1; slotNum >= 0; slotNum--) {
            ItemStack item = container.getItem(slotNum);
            if (!item.isEmpty() && itemFilter.test(item)) {
                // When we empty the slot, cycle to a different one.
                if (item.getCount() <= amountToRemove.applyAsInt(item)) {
                    this.setSelectedItem(itemStack, SelectedItem.DEFAULT_SELECTED_ITEM);
                } else {
                    // Otherwise, when not empty, set this as the newly selected item.
                    this.setSelectedItem(itemStack, slotNum);
                }

                return slotNum;
            }
        }

        return SelectedItem.DEFAULT_SELECTED_ITEM;
    }

    /**
     * @see net.minecraft.world.SimpleContainer#addItem(ItemStack)
     */
    default ItemSlot addItem(Container container, ItemStack itemStack, int prioritizedSlot) {
        ItemStack remainingItems = itemStack.copy();
        int slotNum = this.moveItemToOccupiedSlotsWithSameType(container, remainingItems, prioritizedSlot);
        if (remainingItems.isEmpty()) {
            return new ItemSlot(slotNum);
        } else {
            slotNum = this.moveItemToEmptySlots(container, remainingItems, slotNum);
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
    private int moveItemToEmptySlots(Container container, ItemStack sourceStack, int prioritizedSlot) {
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
    private int moveItemToOccupiedSlotsWithSameType(Container container, ItemStack sourceStack, int prioritizedSlot) {
        IntSet slotNums = IntLinkedOpenHashSet.of(prioritizedSlot);
        slotNums.addAll(IntSets.fromTo(0, container.getContainerSize()));
        for (int slotNum : slotNums.toIntArray()) {
            if (slotNum >= 0 && slotNum < container.getContainerSize()) {
                ItemStack targetStack = container.getItem(slotNum);
                if (ItemStack.isSameItemSameComponents(targetStack, sourceStack)) {
                    this.moveItemsBetweenStacks(container, sourceStack, targetStack, slotNum);
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
    private void moveItemsBetweenStacks(Container container, ItemStack sourceStack, ItemStack targetStack, int slotNum) {
        int maxCount = this.getMaxStackSize(container, slotNum, targetStack);
        int diff = Math.min(sourceStack.getCount(), maxCount - targetStack.getCount());
        if (diff > 0) {
            targetStack.grow(diff);
            sourceStack.shrink(diff);
            container.setChanged();
        }
    }

    /**
     * Get the maximum stack size for this item in the current container.
     *
     * @param container the container
     * @param slotNum   the slot index
     * @param itemStack the item stack
     * @return the max stack size
     */
    default int getMaxStackSize(Container container, int slotNum, ItemStack itemStack) {
        return container.getMaxStackSize(itemStack);
    }
}
