package fuzs.iteminteractions.common.impl.world.item.container;

import fuzs.iteminteractions.common.impl.init.ModRegistry;
import fuzs.iteminteractions.common.impl.world.inventory.ContainerSlotHelper;
import fuzs.iteminteractions.common.impl.world.inventory.ItemMoveHelper;
import fuzs.iteminteractions.common.impl.world.inventory.ItemSlot;
import fuzs.iteminteractions.common.impl.world.item.component.SelectedItem;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.Container;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.function.*;

public class ItemInteractionHelper {

    public static boolean overrideStackedOnOther(ItemStack itemStack, Supplier<? extends Container> containerSupplier, Slot slot, ClickAction clickAction, Player player, ToIntFunction<ItemStack> acceptableItemCount, ToIntBiFunction<Container, ItemStack> maxStackSize) {
        ItemStack itemInHoveredSlot = slot.getItem();
        boolean extractSingleItemOnly = ModRegistry.MOVE_SINGLE_ITEM_ATTACHMENT_TYPE.getOrDefault(player,
                Boolean.FALSE);
        if (clickAction == ClickAction.SECONDARY && (itemInHoveredSlot.isEmpty() || extractSingleItemOnly)) {
            BiConsumer<ItemStack, Integer> addToSlot = (ItemStack stackToAdd, Integer index) -> {
                addItem(containerSupplier,
                        itemStack,
                        slot.safeInsert(stackToAdd),
                        acceptableItemCount,
                        index,
                        maxStackSize);
            };
            handleRemoveItem(itemStack,
                    containerSupplier,
                    itemInHoveredSlot,
                    player,
                    extractSingleItemOnly,
                    addToSlot,
                    maxStackSize);
            return true;
        } else if (clickAction == ClickAction.SECONDARY || extractSingleItemOnly) {
            ItemStack stackInSlot = slot.safeTake(itemInHoveredSlot.getCount(), itemInHoveredSlot.getCount(), player);
            handleAddItem(itemStack,
                    containerSupplier,
                    clickAction,
                    player,
                    extractSingleItemOnly,
                    acceptableItemCount,
                    stackInSlot,
                    maxStackSize);
            slot.safeInsert(stackInSlot);
            return true;
        } else {
            return false;
        }
    }

    public static boolean overrideOtherStackedOnMe(ItemStack itemStack, Supplier<? extends Container> containerSupplier, ItemStack itemHeldByCursor, Slot slot, ClickAction clickAction, Player player, SlotAccess slotAccess, ToIntFunction<ItemStack> acceptableItemCount, ToIntBiFunction<Container, ItemStack> maxStackSize, Runnable onToggleSelectedItem) {
        if (!slot.allowModification(player)) {
            return false;
        } else {
            boolean extractSingleItemOnly = ModRegistry.MOVE_SINGLE_ITEM_ATTACHMENT_TYPE.getOrDefault(player,
                    Boolean.FALSE);
            if (clickAction == ClickAction.SECONDARY && (itemHeldByCursor.isEmpty() || extractSingleItemOnly)) {
                BiConsumer<ItemStack, Integer> addToSlot = (ItemStack stackToAdd, Integer index) -> {
                    ItemStack stackInSlot = slotAccess.get();
                    if (stackInSlot.isEmpty()) {
                        slotAccess.set(stackToAdd);
                    } else {
                        stackInSlot.grow(stackToAdd.getCount());
                        slotAccess.set(stackInSlot);
                    }
                };
                handleRemoveItem(itemStack,
                        containerSupplier,
                        itemHeldByCursor,
                        player,
                        extractSingleItemOnly,
                        addToSlot,
                        maxStackSize);
                return true;
            } else if (clickAction == ClickAction.SECONDARY || extractSingleItemOnly) {
                handleAddItem(itemStack,
                        containerSupplier,
                        clickAction,
                        player,
                        extractSingleItemOnly,
                        acceptableItemCount,
                        itemHeldByCursor,
                        maxStackSize);
                return true;
            } else {
                if (clickAction == ClickAction.PRIMARY) {
                    onToggleSelectedItem.run();
                }

                return false;
            }
        }
    }

    private static void handleAddItem(ItemStack itemStack, Supplier<? extends Container> containerSupplier, ClickAction clickAction, Player player, boolean extractSingleItemOnly, ToIntFunction<ItemStack> acceptableItemCount, ItemStack stackInSlot, ToIntBiFunction<Container, ItemStack> maxStackSize) {
        int transferredCount;
        if (clickAction == ClickAction.PRIMARY) {
            transferredCount = addItem(containerSupplier,
                    itemStack,
                    stackInSlot,
                    stack -> Math.min(1, acceptableItemCount.applyAsInt(stack)),
                    maxStackSize);
        } else {
            transferredCount = addItem(containerSupplier, itemStack, stackInSlot, acceptableItemCount, maxStackSize);
        }

        stackInSlot.shrink(transferredCount);
        if (transferredCount > 0 && !extractSingleItemOnly) {
            player.playSound(SoundEvents.BUNDLE_INSERT, 0.8F, 0.8F + player.level().getRandom().nextFloat() * 0.4F);
        }
    }

    private static int addItem(Supplier<? extends Container> containerSupplier, ItemStack itemStack, ItemStack newStack, ToIntFunction<ItemStack> acceptableItemCount, ToIntBiFunction<Container, ItemStack> maxStackSize) {
        return addItem(containerSupplier,
                itemStack,
                newStack,
                acceptableItemCount,
                ContainerSlotHelper.getSelectedItem(itemStack),
                maxStackSize);
    }

    private static int addItem(Supplier<? extends Container> containerSupplier, ItemStack itemStack, ItemStack newStack, ToIntFunction<ItemStack> acceptableItemCount, int prioritizedSlot, ToIntBiFunction<Container, ItemStack> maxStackSize) {
        if (newStack.isEmpty()) {
            return 0;
        }

        Container container = containerSupplier.get();
        ItemStack stackToAdd = newStack.copy();
        stackToAdd.setCount(Math.min(acceptableItemCount.applyAsInt(newStack), newStack.getCount()));
        if (stackToAdd.isEmpty()) {
            return 0;
        }

        ItemSlot itemSlot = ItemMoveHelper.addItem(container, stackToAdd, prioritizedSlot, maxStackSize);
        ContainerSlotHelper.setSelectedItem(itemStack, itemSlot.slotNum());
        return stackToAdd.getCount() - itemSlot.item().getCount();
    }

    private static void handleRemoveItem(ItemStack itemStack, Supplier<? extends Container> containerSupplier, ItemStack stackOnMe, Player player, boolean extractSingleItemOnly, BiConsumer<ItemStack, Integer> addToSlot, ToIntBiFunction<Container, ItemStack> maxStackSize) {
        Container container = containerSupplier.get();
        ToIntFunction<ItemStack> amountToRemove = stack -> extractSingleItemOnly ? 1 : stack.getCount();
        Predicate<ItemStack> itemFilter = (ItemStack stackInSlot) -> {
            return stackOnMe.isEmpty() || (ItemStack.isSameItemSameComponents(stackOnMe, stackInSlot)
                    && stackOnMe.getCount() < maxStackSize.applyAsInt(container, stackOnMe));
        };
        ItemSlot itemSlot = removeItem(container, itemStack, itemFilter, amountToRemove);
        ItemStack stackToAdd = itemSlot.item();
        if (!stackToAdd.isEmpty()) {
            addToSlot.accept(stackToAdd, itemSlot.slotNum());
            if (!extractSingleItemOnly) {
                player.playSound(SoundEvents.BUNDLE_REMOVE_ONE,
                        0.8F,
                        0.8F + player.level().getRandom().nextFloat() * 0.4F);
            }
        }
    }

    private static ItemSlot removeItem(Container container, ItemStack itemStack, Predicate<ItemStack> itemFilter, ToIntFunction<ItemStack> amountToRemove) {
        int slotNum = pickSelectedSlot(container, itemStack, itemFilter, amountToRemove);
        if (slotNum != SelectedItem.DEFAULT_SELECTED_ITEM) {
            int amount = amountToRemove.applyAsInt(container.getItem(slotNum));
            return new ItemSlot(slotNum, container.removeItem(slotNum, amount));
        } else {
            return ItemSlot.EMPTY;
        }
    }

    private static int pickSelectedSlot(Container container, ItemStack itemStack, Predicate<ItemStack> itemFilter, ToIntFunction<ItemStack> amountToRemove) {
        int selectedItem = ContainerSlotHelper.getSelectedItem(itemStack);
        if (selectedItem >= 0 && selectedItem < container.getContainerSize()) {
            ItemStack item = container.getItem(selectedItem);
            if (!item.isEmpty() && itemFilter.test(item)) {
                // When we empty the slot, cycle to a different one.
                if (item.getCount() <= amountToRemove.applyAsInt(item)) {
                    int updatedSelectedItem = ContainerSlotHelper.scrollSelectedItem(container,
                            selectedItem,
                            -1);
                    ContainerSlotHelper.setSelectedItem(itemStack, updatedSelectedItem);
                }

                return selectedItem;
            }
        }

        for (int slotNum = container.getContainerSize() - 1; slotNum >= 0; slotNum--) {
            ItemStack item = container.getItem(slotNum);
            if (!item.isEmpty() && itemFilter.test(item)) {
                // When we empty the slot, cycle to a different one.
                if (item.getCount() <= amountToRemove.applyAsInt(item)) {
                    ContainerSlotHelper.setSelectedItem(itemStack, SelectedItem.DEFAULT_SELECTED_ITEM);
                } else {
                    // Otherwise, when not empty, set this as the newly selected item.
                    ContainerSlotHelper.setSelectedItem(itemStack, slotNum);
                }

                return slotNum;
            }
        }

        return SelectedItem.DEFAULT_SELECTED_ITEM;
    }
}
