package fuzs.iteminteractions.common.impl.handler;

import fuzs.iteminteractions.common.api.v1.world.item.storage.ItemStorageHolder;
import fuzs.puzzleslib.common.api.event.v1.core.EventResult;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class ContainerClickInputHandler {

    public static EventResult onContainerItemClick(ItemStack hoveredItem, Slot hoveredSlot, ItemStack itemHeldByCursor, SlotAccess slotHeldByCursor, ClickAction clickAction, Player player) {
        ItemStorageHolder holderHeldByCursor = ItemStorageHolder.ofItem(itemHeldByCursor);
        if (!holderHeldByCursor.isEmpty() && holderHeldByCursor.overrideStackedOnOther(itemHeldByCursor,
                hoveredSlot,
                clickAction,
                player)) {
            return EventResult.INTERRUPT;
        }

        ItemStorageHolder hoveredHolder = ItemStorageHolder.ofItem(hoveredItem);
        if (!hoveredHolder.isEmpty() && hoveredHolder.overrideOtherStackedOnMe(hoveredItem,
                itemHeldByCursor,
                hoveredSlot,
                clickAction,
                player,
                slotHeldByCursor)) {
            return EventResult.INTERRUPT;
        }

        return EventResult.PASS;
    }
}
