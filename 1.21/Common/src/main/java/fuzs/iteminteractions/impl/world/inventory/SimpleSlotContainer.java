package fuzs.iteminteractions.impl.world.inventory;

import fuzs.puzzleslib.api.container.v1.ContainerSerializationHelper;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;

/**
 * Copied from {@link net.minecraft.world.inventory.PlayerEnderChestContainer}, as over there only difference from simple container is that slot ids are saved along with
 * items.
 */
public class SimpleSlotContainer extends SimpleContainer {

    public SimpleSlotContainer(int inventorySize) {
        super(inventorySize);
    }

    @Override
    public void fromTag(ListTag listTag, HolderLookup.Provider registries) {
        ContainerSerializationHelper.fromTag(listTag, this.getContainerSize(), (ItemStack itemStack, int value) -> {
            this.setItem(value, itemStack);
        }, registries);
    }

    @Override
    public ListTag createTag(HolderLookup.Provider registries) {
        return ContainerSerializationHelper.createTag(this.getContainerSize(), this::getItem, registries);
    }
}
