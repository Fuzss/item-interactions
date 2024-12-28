package fuzs.iteminteractions.impl.world.item.container;

import fuzs.iteminteractions.impl.world.inventory.ContainerSlotHelper;
import fuzs.iteminteractions.impl.world.inventory.ItemMoveHelper;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.tuple.Pair;

import java.util.OptionalInt;
import java.util.function.*;

public class ItemInteractionHelper {

    public static boolean overrideStackedOnOther(Supplier<SimpleContainer> containerSupplier, Slot slot, ClickAction clickAction, Player player, ToIntFunction<ItemStack> acceptableItemCount, ToIntBiFunction<Container, ItemStack> maxStackSize) {
        if (slot.container instanceof CraftingContainer) {
            return false;
        } else {
            ItemStack stackBelowMe = slot.getItem();
            boolean extractSingleItemOnly = ContainerSlotHelper.extractSingleItemOnly(player);
            if (clickAction == ClickAction.SECONDARY && (stackBelowMe.isEmpty() || extractSingleItemOnly)) {
                BiConsumer<ItemStack, Integer> addToSlot = (stackToAdd, index) -> {
                    addStack(containerSupplier,
                            player,
                            slot.safeInsert(stackToAdd),
                            acceptableItemCount,
                            index,
                            maxStackSize);
                };
                handleRemoveItem(containerSupplier,
                        stackBelowMe,
                        player,
                        extractSingleItemOnly,
                        addToSlot,
                        maxStackSize);
                return true;
            } else if (clickAction == ClickAction.SECONDARY || extractSingleItemOnly) {
                ItemStack stackInSlot = slot.safeTake(stackBelowMe.getCount(), stackBelowMe.getCount(), player);
                handleAddItem(containerSupplier, clickAction, player, acceptableItemCount, stackInSlot, maxStackSize);
                slot.safeInsert(stackInSlot);
                return true;
            } else {
                return false;
            }
        }
    }

    public static boolean overrideOtherStackedOnMe(Supplier<SimpleContainer> containerSupplier, ItemStack stackOnMe, Slot slot, ClickAction clickAction, Player player, SlotAccess slotAccess, ToIntFunction<ItemStack> acceptableItemCount, ToIntBiFunction<Container, ItemStack> maxStackSize, Runnable onToggleSelectedItem) {
        if (!slot.allowModification(player) || slot.container instanceof CraftingContainer) {
            return false;
        } else {
            boolean extractSingleItemOnly = ContainerSlotHelper.extractSingleItemOnly(player);
            if (clickAction == ClickAction.SECONDARY && (stackOnMe.isEmpty() || extractSingleItemOnly)) {
                BiConsumer<ItemStack, Integer> addToSlot = (stackToAdd, index) -> {
                    ItemStack stackInSlot = slotAccess.get();
                    if (stackInSlot.isEmpty()) {
                        slotAccess.set(stackToAdd);
                    } else {
                        stackInSlot.grow(stackToAdd.getCount());
                        slotAccess.set(stackInSlot);
                    }
                };
                handleRemoveItem(containerSupplier, stackOnMe, player, extractSingleItemOnly, addToSlot, maxStackSize);
                return true;
            } else if (clickAction == ClickAction.SECONDARY || extractSingleItemOnly) {
                handleAddItem(containerSupplier, clickAction, player, acceptableItemCount, stackOnMe, maxStackSize);
                return true;
            } else {
                if (clickAction == ClickAction.PRIMARY && stackOnMe.isEmpty()) onToggleSelectedItem.run();
                return false;
            }
        }
    }

    private static void handleRemoveItem(Supplier<SimpleContainer> containerSupplier, ItemStack stackOnMe, Player player, boolean extractSingleItemOnly, BiConsumer<ItemStack, Integer> addToSlot, ToIntBiFunction<Container, ItemStack> maxStackSize) {
        SimpleContainer container = containerSupplier.get();
        ToIntFunction<ItemStack> amountToRemove = stack -> extractSingleItemOnly ? 1 : stack.getCount();
        Predicate<ItemStack> itemFilter = stackInSlot -> {
            return stackOnMe.isEmpty() || (ItemStack.isSameItemSameComponents(stackOnMe, stackInSlot) &&
                    stackOnMe.getCount() < maxStackSize.applyAsInt(container, stackOnMe));
        };
        Pair<ItemStack, Integer> result = removeLastStack(container, player, itemFilter, amountToRemove);
        ItemStack stackToAdd = result.getLeft();
        if (!stackToAdd.isEmpty()) {
            addToSlot.accept(stackToAdd, result.getRight());
            player.playSound(SoundEvents.BUNDLE_REMOVE_ONE, 0.8F, 0.8F + player.level().getRandom().nextFloat() * 0.4F);
        }
    }

    private static void handleAddItem(Supplier<SimpleContainer> containerSupplier, ClickAction clickAction, Player player, ToIntFunction<ItemStack> acceptableItemCount, ItemStack stackInSlot, ToIntBiFunction<Container, ItemStack> maxStackSize) {
        int transferredCount;
        if (clickAction == ClickAction.PRIMARY) {
            transferredCount = addStack(containerSupplier,
                    player,
                    stackInSlot,
                    stack -> Math.min(1, acceptableItemCount.applyAsInt(stack)),
                    maxStackSize);
        } else {
            transferredCount = addStack(containerSupplier, player, stackInSlot, acceptableItemCount, maxStackSize);
        }
        stackInSlot.shrink(transferredCount);
        if (transferredCount > 0) {
            player.playSound(SoundEvents.BUNDLE_INSERT, 0.8F, 0.8F + player.level().getRandom().nextFloat() * 0.4F);
        }
    }

    private static int addStack(Supplier<SimpleContainer> containerSupplier, Player player, ItemStack newStack, ToIntFunction<ItemStack> acceptableItemCount, ToIntBiFunction<Container, ItemStack> maxStackSize) {
        return addStack(containerSupplier,
                player,
                newStack,
                acceptableItemCount,
                ContainerSlotHelper.getCurrentContainerSlot(player),
                maxStackSize);
    }

    private static int addStack(Supplier<SimpleContainer> containerSupplier, Player player, ItemStack newStack, ToIntFunction<ItemStack> acceptableItemCount, int prioritizedSlot, ToIntBiFunction<Container, ItemStack> maxStackSize) {
        if (newStack.isEmpty()) return 0;
        SimpleContainer container = containerSupplier.get();
        ItemStack stackToAdd = newStack.copy();
        stackToAdd.setCount(Math.min(acceptableItemCount.applyAsInt(newStack), newStack.getCount()));
        if (stackToAdd.isEmpty()) return 0;
        Pair<ItemStack, Integer> result = ItemMoveHelper.addItem(container, stackToAdd, prioritizedSlot, maxStackSize);
        ContainerSlotHelper.setCurrentContainerSlot(player, result.getRight());
        return stackToAdd.getCount() - result.getLeft().getCount();
    }

    private static Pair<ItemStack, Integer> removeLastStack(SimpleContainer container, Player player, Predicate<ItemStack> itemFilter, ToIntFunction<ItemStack> amountToRemove) {
        OptionalInt slotWithContent = findSlotWithContent(container, player, itemFilter, amountToRemove);
        if (slotWithContent.isPresent()) {
            int index = slotWithContent.getAsInt();
            int amount = amountToRemove.applyAsInt(container.getItem(index));
            return Pair.of(container.removeItem(index, amount), index);
        }

        return Pair.of(ItemStack.EMPTY, -1);
    }

    private static OptionalInt findSlotWithContent(SimpleContainer container, Player player, Predicate<ItemStack> itemFilter, ToIntFunction<ItemStack> amountToRemove) {
        int currentContainerSlot = ContainerSlotHelper.getCurrentContainerSlot(player);
        if (currentContainerSlot >= 0 && currentContainerSlot < container.getContainerSize()) {
            ItemStack stackInSlot = container.getItem(currentContainerSlot);
            if (!stackInSlot.isEmpty() && itemFilter.test(stackInSlot)) {
                // did we empty the slot, so cycle to a different one
                if (stackInSlot.getCount() <= amountToRemove.applyAsInt(stackInSlot)) {
                    ContainerSlotHelper.cycleCurrentSlotBackwards(player, container);
                }
                return OptionalInt.of(currentContainerSlot);
            }
        }

        for (int i = container.getContainerSize() - 1; i >= 0; i--) {
            ItemStack stackInSlot = container.getItem(i);
            if (!stackInSlot.isEmpty() && itemFilter.test(stackInSlot)) {
                // did we empty the slot, so cycle to a different one
                if (stackInSlot.getCount() <= amountToRemove.applyAsInt(stackInSlot)) {
                    ContainerSlotHelper.resetCurrentContainerSlot(player);
                } else {
                    // otherwise if not empty make sure this is the new current slot
                    ContainerSlotHelper.setCurrentContainerSlot(player, i);
                }
                return OptionalInt.of(i);
            }
        }

        return OptionalInt.empty();
    }
}
