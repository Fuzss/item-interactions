package fuzs.iteminteractions.common.impl.world.inventory;

import fuzs.iteminteractions.common.impl.init.ModRegistry;
import fuzs.iteminteractions.common.impl.world.item.component.SelectedItem;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class ContainerSlotHelper {

    public static int findClosestSlotWithContent(Container container, int currentContainerSlot, boolean forwards, boolean scrollRows) {
        int size = container.getContainerSize();
        if (currentContainerSlot == -1) {
            currentContainerSlot = size - 1;
        }

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
        return itemStack.getOrDefault(ModRegistry.SELECTED_ITEM_DATA_COMPONENT_TYPE.value(), SelectedItem.DEFAULT)
                .selectedItem();
    }

    public static void setSelectedItem(ItemStack itemStack, int selectedItem) {
        itemStack.set(ModRegistry.SELECTED_ITEM_DATA_COMPONENT_TYPE.value(),
                SelectedItem.of(selectedItem));
    }

    public static void extractSingleItem(Player player, boolean singleItem) {
        ModRegistry.MOVE_SINGLE_ITEM_ATTACHMENT_TYPE.set(player, singleItem);
    }

    public static boolean extractSingleItemOnly(Player player) {
        return ModRegistry.MOVE_SINGLE_ITEM_ATTACHMENT_TYPE.getOrDefault(player, Boolean.FALSE);
    }
}
