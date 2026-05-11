package fuzs.iteminteractions.common.impl.world.inventory;

import fuzs.iteminteractions.common.impl.init.ModRegistry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BundleItem;
import net.minecraft.world.item.ItemStack;

public class ContainerSlotHelper {

    public static int findClosestSlotWithContent(Container container, int currentContainerSlot, boolean forwards, boolean scrollRows) {
        int size = container.getContainerSize();
        if (currentContainerSlot == -1) currentContainerSlot = size - 1;
        // TODO should not be hardcoded to 9, instead make container grid width and height methods on all providers
        for (int i = scrollRows ? 9 : 1; i <= size; i++) {
            int currentIndex = ((currentContainerSlot + (forwards ? i : -i)) % size + size) % size;
            if (!container.getItem(currentIndex).isEmpty()) {
                return currentIndex;
            }
        }

        return -1;
    }

    public static void cycleCurrentSlotBackwards(ItemStack itemStack, Container container) {
        int currentContainerSlot = getSelectedItem(itemStack);
        currentContainerSlot = findClosestSlotWithContent(container, currentContainerSlot, false, false);
        setSelectedItem(itemStack, currentContainerSlot);
    }

    public static void resetCurrentContainerSlot(ItemStack itemStack) {
        setSelectedItem(itemStack, -1);
    }

    public static int getSelectedItem(ItemStack itemStack) {
        if (itemStack.has(DataComponents.BUNDLE_CONTENTS)) {
            return BundleItem.getSelectedItemIndex(itemStack);
        } else {
            return itemStack.getOrDefault(ModRegistry.SELECTED_ITEM_DATA_COMPONENT_TYPE.value(), -1);
        }
    }

    public static void setSelectedItem(ItemStack itemStack, int selectedItem) {
        if (itemStack.has(DataComponents.BUNDLE_CONTENTS)) {
            BundleItem.toggleSelectedItem(itemStack, selectedItem);
        } else {
            if (selectedItem != -1) {
                itemStack.set(ModRegistry.SELECTED_ITEM_DATA_COMPONENT_TYPE.value(), selectedItem);
            } else {
                itemStack.remove(ModRegistry.SELECTED_ITEM_DATA_COMPONENT_TYPE.value());
            }
        }
    }

    public static void extractSingleItem(Player player, boolean singleItem) {
        ModRegistry.MOVE_SINGLE_ITEM_ATTACHMENT_TYPE.set(player, singleItem);
    }

    public static boolean extractSingleItemOnly(Player player) {
        return ModRegistry.MOVE_SINGLE_ITEM_ATTACHMENT_TYPE.getOrDefault(player, Boolean.FALSE);
    }
}
