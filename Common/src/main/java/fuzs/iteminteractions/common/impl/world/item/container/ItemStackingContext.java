package fuzs.iteminteractions.common.impl.world.item.container;

import fuzs.iteminteractions.common.api.v1.world.item.storage.ContainerItemStorage;
import fuzs.iteminteractions.common.api.v1.world.item.storage.ItemStorageHolder;
import fuzs.iteminteractions.common.impl.world.inventory.ItemSlot;
import fuzs.iteminteractions.common.impl.world.item.component.SelectedItem;
import it.unimi.dsi.fastutil.ints.IntLinkedOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.ints.IntSets;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.BundleContents;
import org.joml.Vector2i;

public final class ItemStackingContext {
    private final ItemStorageHolder holder;
    private final ContainerItemStorage storage;
    private final Player player;

    public ItemStackingContext(ItemStorageHolder holder, ContainerItemStorage storage, Player player) {
        this.holder = holder;
        this.storage = storage;
        this.player = player;
    }

    private int getItemCountLimit(ItemStack itemStack) {
        int itemLimit = itemStack.getCount();
        return this.storage.extractSingleItemOnly(this.player) ? Math.min(1, itemLimit) : itemLimit;
    }

    private int getItemCountLimit(ItemStack itemStack, ItemStack otherItem) {
        int itemLimit = Math.min(otherItem.getCount(),
                this.holder.getAcceptableItemCount(itemStack, otherItem, this.player));
        return this.storage.extractSingleItemOnly(this.player) ? Math.min(1, itemLimit) : itemLimit;
    }

    private void handleAddItem(ItemStack itemStack, ItemStack otherItem) {
        int transferredCount = this.tryInsert(itemStack, otherItem);
        otherItem.shrink(transferredCount);
        if (!this.storage.extractSingleItemOnly(this.player)) {
            if (transferredCount > 0) {
                this.storage.playInsertSound(this.player);
            } else {
                this.storage.playInsertFailSound(this.player);
            }
        }
    }

    /**
     * @see BundleContents.Mutable#tryInsert(ItemStack)
     */
    public int tryInsert(ItemStack itemStack, ItemStack otherItem) {
        return this.tryInsert(itemStack, otherItem, this.storage.getSelectedItem(itemStack));
    }

    public int tryInsert(ItemStack itemStack, ItemStack otherItem, int prioritizedSlot) {
        int itemLimit = this.getItemCountLimit(itemStack, otherItem);
        if (itemLimit > 0 && otherItem.getCount() > 0) {
            Container container = this.storage.getItemContainer(itemStack, this.player, true);
            ItemStack item = otherItem.copyWithCount(itemLimit);
            ItemSlot itemSlot = this.addItem(container, item, prioritizedSlot);
            this.storage.setSelectedItem(itemStack, itemSlot.slotNum());
            return item.getCount() - itemSlot.item().getCount();
        } else {
            return 0;
        }
    }

    public void handleRemoveItem(ItemStack itemStack, ItemStack otherItem) {
        ItemSlot itemSlot = this.removeOne(itemStack, otherItem);
        if (!itemSlot.item().isEmpty()) {
//            this.setItemInSlot(itemSlot.slotNum(), itemSlot.item());
            if (!this.storage.extractSingleItemOnly(this.player)) {
                this.storage.playRemoveOneSound(this.player);
            }
        }
    }

    /**
     * @see BundleContents.Mutable#removeOne()
     */
    public ItemSlot removeOne(ItemStack itemStack, ItemStack otherItem) {
        Container container = this.storage.getItemContainer(itemStack, this.player, true);
        int slotNum = this.updateSelectedSlot(container, itemStack, otherItem);
        if (slotNum != SelectedItem.DEFAULT_SELECTED_ITEM) {
            ItemStack item = container.getItem(slotNum);
            int removalCount = this.getItemCountLimit(item);
            return new ItemSlot(slotNum, container.removeItem(slotNum, removalCount));
        } else {
            return ItemSlot.EMPTY;
        }
    }

    private int updateSelectedSlot(Container container, ItemStack itemStack, ItemStack otherItem) {
        int selectedItem = this.storage.getSelectedItem(itemStack);
        if (selectedItem >= 0 && selectedItem < container.getContainerSize()) {
            ItemStack item = container.getItem(selectedItem);
            if (!item.isEmpty() && this.canCombineItemInSlot(otherItem, container, selectedItem, item)) {
                // When we empty the slot, cycle to a different one.
                if (item.getCount() <= this.getItemCountLimit(item)) {
                    int updatedSelectedItem = this.storage.scrollSelectedItem(itemStack,
                            container,
                            new Vector2i(-1, 0));
                    this.storage.setSelectedItem(itemStack, updatedSelectedItem);
                }

                return selectedItem;
            }
        }

        for (int slotNum = container.getContainerSize() - 1; slotNum >= 0; slotNum--) {
            ItemStack item = container.getItem(slotNum);
            if (!item.isEmpty() && this.canCombineItemInSlot(otherItem, container, slotNum, item)) {
                // When we empty the slot, cycle to a different one.
                if (item.getCount() <= this.getItemCountLimit(item)) {
                    this.storage.setSelectedItem(itemStack, SelectedItem.DEFAULT_SELECTED_ITEM);
                } else {
                    // Otherwise, when not empty, set this as the newly selected item.
                    this.storage.setSelectedItem(itemStack, slotNum);
                }

                return slotNum;
            }
        }

        return SelectedItem.DEFAULT_SELECTED_ITEM;
    }

    private boolean canCombineItemInSlot(ItemStack itemStack, Container container, int slotNum, ItemStack slotItem) {
        return itemStack.isEmpty() || (ItemStack.isSameItemSameComponents(itemStack, slotItem)
                && itemStack.getCount() < this.storage.getMaxStackSize(container, slotNum, slotItem));
    }

    /**
     * @see net.minecraft.world.SimpleContainer#addItem(ItemStack)
     */
    private ItemSlot addItem(Container container, ItemStack itemStack, int prioritizedSlot) {
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
        int maxCount = this.storage.getMaxStackSize(container, slotNum, targetStack);
        int diff = Math.min(sourceStack.getCount(), maxCount - targetStack.getCount());
        if (diff > 0) {
            targetStack.grow(diff);
            sourceStack.shrink(diff);
            container.setChanged();
        }
    }
}
