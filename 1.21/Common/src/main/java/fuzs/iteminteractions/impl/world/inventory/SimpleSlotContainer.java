package fuzs.iteminteractions.impl.world.inventory;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;

/**
 * Copied from ender chest, as over there only difference from simple container is that slot ids are saved along with
 * items.
 */
public class SimpleSlotContainer extends SimpleContainer {

    public SimpleSlotContainer(int inventorySize) {
        super(inventorySize);
    }

    @Override
    public void fromTag(ListTag listTag) {
        this.clearContent();
        for (int k = 0; k < listTag.size(); ++k) {
            CompoundTag compoundtag = listTag.getCompound(k);
            int slot = compoundtag.getByte("Slot") & 255;
            if (slot < this.getContainerSize()) {
                this.setItem(slot, ItemStack.of(compoundtag));
            }
        }
    }

    @Override
    public ListTag createTag() {
        ListTag listtag = new ListTag();
        for (int i = 0; i < this.getContainerSize(); ++i) {
            ItemStack itemstack = this.getItem(i);
            if (!itemstack.isEmpty()) {
                CompoundTag compoundtag = new CompoundTag();
                compoundtag.putByte("Slot", (byte) i);
                itemstack.save(compoundtag);
                listtag.add(compoundtag);
            }
        }
        return listtag;
    }
}
