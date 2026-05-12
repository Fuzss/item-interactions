package fuzs.iteminteractions.common.impl.world.item.container;

import fuzs.iteminteractions.common.impl.world.inventory.ContainerSlotHelper;
import fuzs.iteminteractions.common.impl.world.inventory.ItemMoveHelper;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.tuple.Pair;

import java.util.OptionalInt;
import java.util.function.*;

public class ItemInteractionHelper {

    public static boolean overrideStackedOnOther(ItemStack itemStack, Supplier<SimpleContainer> containerSupplier, Slot slot, ClickAction clickAction, Player player, ToIntFunction<ItemStack> acceptableItemCount, ToIntBiFunction<Container, ItemStack> maxStackSize) {
        ItemStack itemInHoveredSlot = slot.getItem();
        boolean extractSingleItemOnly = ContainerSlotHelper.extractSingleItemOnly(player);
        if (clickAction == ClickAction.SECONDARY && (itemInHoveredSlot.isEmpty() || extractSingleItemOnly)) {
            BiConsumer<ItemStack, Integer> addToSlot = (ItemStack stackToAdd, Integer index) -> {
                addStack(containerSupplier,
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

    public static boolean overrideOtherStackedOnMe(ItemStack itemStack, Supplier<SimpleContainer> containerSupplier, ItemStack itemHeldByCursor, Slot slot, ClickAction clickAction, Player player, SlotAccess slotAccess, ToIntFunction<ItemStack> acceptableItemCount, ToIntBiFunction<Container, ItemStack> maxStackSize, Runnable onToggleSelectedItem) {
        if (!slot.allowModification(player)) {
            return false;
        } else {
            boolean extractSingleItemOnly = ContainerSlotHelper.extractSingleItemOnly(player);
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

    private static void handleRemoveItem(ItemStack itemStack, Supplier<SimpleContainer> containerSupplier, ItemStack stackOnMe, Player player, boolean extractSingleItemOnly, BiConsumer<ItemStack, Integer> addToSlot, ToIntBiFunction<Container, ItemStack> maxStackSize) {
        Container container = containerSupplier.get();
        ToIntFunction<ItemStack> amountToRemove = stack -> extractSingleItemOnly ? 1 : stack.getCount();
        Predicate<ItemStack> itemFilter = (ItemStack stackInSlot) -> {
            return stackOnMe.isEmpty() || (ItemStack.isSameItemSameComponents(stackOnMe, stackInSlot)
                    && stackOnMe.getCount() < maxStackSize.applyAsInt(container, stackOnMe));
        };
        Pair<ItemStack, Integer> result = removeLastStack(container, itemStack, itemFilter, amountToRemove);
        ItemStack stackToAdd = result.getLeft();
        if (!stackToAdd.isEmpty()) {
            addToSlot.accept(stackToAdd, result.getRight());
            if (!extractSingleItemOnly) {
                player.playSound(SoundEvents.BUNDLE_REMOVE_ONE,
                        0.8F,
                        0.8F + player.level().getRandom().nextFloat() * 0.4F);
            }
        }
    }

    private static void handleAddItem(ItemStack itemStack, Supplier<SimpleContainer> containerSupplier, ClickAction clickAction, Player player, boolean extractSingleItemOnly, ToIntFunction<ItemStack> acceptableItemCount, ItemStack stackInSlot, ToIntBiFunction<Container, ItemStack> maxStackSize) {
        int transferredCount;
        if (clickAction == ClickAction.PRIMARY) {
            transferredCount = addStack(containerSupplier,
                    itemStack,
                    stackInSlot,
                    stack -> Math.min(1, acceptableItemCount.applyAsInt(stack)),
                    maxStackSize);
        } else {
            transferredCount = addStack(containerSupplier, itemStack, stackInSlot, acceptableItemCount, maxStackSize);
        }

        stackInSlot.shrink(transferredCount);
        if (transferredCount > 0 && !extractSingleItemOnly) {
            player.playSound(SoundEvents.BUNDLE_INSERT, 0.8F, 0.8F + player.level().getRandom().nextFloat() * 0.4F);
        }
    }

    private static int addStack(Supplier<SimpleContainer> containerSupplier, ItemStack itemStack, ItemStack newStack, ToIntFunction<ItemStack> acceptableItemCount, ToIntBiFunction<Container, ItemStack> maxStackSize) {
        return addStack(containerSupplier,
                itemStack,
                newStack,
                acceptableItemCount,
                ContainerSlotHelper.getSelectedItem(itemStack),
                maxStackSize);
    }

    private static int addStack(Supplier<SimpleContainer> containerSupplier, ItemStack itemStack, ItemStack newStack, ToIntFunction<ItemStack> acceptableItemCount, int prioritizedSlot, ToIntBiFunction<Container, ItemStack> maxStackSize) {
        if (newStack.isEmpty()) {
            return 0;
        }

        Container container = containerSupplier.get();
        ItemStack stackToAdd = newStack.copy();
        stackToAdd.setCount(Math.min(acceptableItemCount.applyAsInt(newStack), newStack.getCount()));
        if (stackToAdd.isEmpty()) {
            return 0;
        }

        Pair<ItemStack, Integer> result = ItemMoveHelper.addItem(container, stackToAdd, prioritizedSlot, maxStackSize);
        ContainerSlotHelper.setSelectedItem(itemStack, result.getRight());
        return stackToAdd.getCount() - result.getLeft().getCount();
    }

    private static Pair<ItemStack, Integer> removeLastStack(Container container, ItemStack itemStack, Predicate<ItemStack> itemFilter, ToIntFunction<ItemStack> amountToRemove) {
        OptionalInt slotWithContent = findSlotWithContent(container, itemStack, itemFilter, amountToRemove);
        if (slotWithContent.isPresent()) {
            int index = slotWithContent.getAsInt();
            int amount = amountToRemove.applyAsInt(container.getItem(index));
            return Pair.of(container.removeItem(index, amount), index);
        }

        return Pair.of(ItemStack.EMPTY, -1);
    }

    private static OptionalInt findSlotWithContent(Container container, ItemStack itemStack, Predicate<ItemStack> itemFilter, ToIntFunction<ItemStack> amountToRemove) {
        int currentContainerSlot = ContainerSlotHelper.getSelectedItem(itemStack);
        if (currentContainerSlot >= 0 && currentContainerSlot < container.getContainerSize()) {
            ItemStack stackInSlot = container.getItem(currentContainerSlot);
            if (!stackInSlot.isEmpty() && itemFilter.test(stackInSlot)) {
                // did we empty the slot, so cycle to a different one
                if (stackInSlot.getCount() <= amountToRemove.applyAsInt(stackInSlot)) {
                    ContainerSlotHelper.cycleCurrentSlotBackwards(itemStack, container);
                }

                return OptionalInt.of(currentContainerSlot);
            }
        }

        for (int slot = container.getContainerSize() - 1; slot >= 0; slot--) {
            ItemStack stackInSlot = container.getItem(slot);
            if (!stackInSlot.isEmpty() && itemFilter.test(stackInSlot)) {
                // did we empty the slot, so cycle to a different one
                if (stackInSlot.getCount() <= amountToRemove.applyAsInt(stackInSlot)) {
                    ContainerSlotHelper.resetCurrentContainerSlot(itemStack);
                } else {
                    // otherwise if not empty, make sure this is the new current slot
                    ContainerSlotHelper.setSelectedItem(itemStack, slot);
                }

                return OptionalInt.of(slot);
            }
        }

        return OptionalInt.empty();
    }
}
