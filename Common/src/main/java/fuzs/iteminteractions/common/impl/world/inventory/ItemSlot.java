package fuzs.iteminteractions.common.impl.world.inventory;

import net.minecraft.world.item.ItemStack;

public record ItemSlot(int slotNum, ItemStack item) {
    public static final ItemSlot EMPTY = new ItemSlot(-1);

    public ItemSlot(int slotNum) {
        this(slotNum, ItemStack.EMPTY);
    }
}
