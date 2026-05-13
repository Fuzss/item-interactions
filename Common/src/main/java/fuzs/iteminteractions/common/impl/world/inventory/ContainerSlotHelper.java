package fuzs.iteminteractions.common.impl.world.inventory;

import fuzs.iteminteractions.common.impl.init.ModRegistry;
import fuzs.iteminteractions.common.impl.world.item.component.SelectedItem;
import net.minecraft.util.Mth;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;

@Deprecated
public class ContainerSlotHelper {

    public static int scrollSelectedItem(Container container, int selectedItem, int scrollDirection) {
        for (int slotNum = 1; slotNum <= container.getContainerSize(); slotNum++) {
            int updatedSelectedItem = Mth.positiveModulo(selectedItem + Mth.sign(scrollDirection),
                    container.getContainerSize());
            if (!container.getItem(updatedSelectedItem).isEmpty()) {
                return updatedSelectedItem;
            }
        }

        return -1;
    }

    public static int getSelectedItem(ItemStack itemStack) {
        return itemStack.getOrDefault(ModRegistry.SELECTED_ITEM_DATA_COMPONENT_TYPE.value(), SelectedItem.DEFAULT)
                .selectedItem();
    }

    public static void setSelectedItem(ItemStack itemStack, int selectedItem) {
        itemStack.set(ModRegistry.SELECTED_ITEM_DATA_COMPONENT_TYPE.value(),
                selectedItem == SelectedItem.DEFAULT_SELECTED_ITEM ? SelectedItem.DEFAULT :
                        SelectedItem.of(selectedItem));
    }
}
