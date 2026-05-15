package fuzs.iteminteractions.common.impl.handler;

import fuzs.iteminteractions.common.api.v1.world.item.storage.ItemStorageHolder;
import fuzs.puzzleslib.common.api.event.v1.core.EventResult;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class ContainerClickInputHandler {

    public static EventResult onContainerItemClicked(ItemStack hoveredItem, Slot hoveredSlot, ItemStack itemHeldByCursor, SlotAccess slotHeldByCursor, ClickAction clickAction, Player player) {
        ItemStorageHolder holderHeldByCursor = ItemStorageHolder.ofItem(itemHeldByCursor);
        if (holderHeldByCursor.isPresentFor(itemHeldByCursor, player)) {
            return holderHeldByCursor.overrideStackedOnOther(itemHeldByCursor, hoveredSlot, clickAction, player) ?
                    EventResult.ALLOW : EventResult.DENY;
        }

        ItemStorageHolder hoveredHolder = ItemStorageHolder.ofItem(hoveredItem);
        if (hoveredHolder.isPresentFor(hoveredItem, player)) {
            return hoveredHolder.overrideOtherStackedOnMe(hoveredItem,
                    itemHeldByCursor,
                    hoveredSlot,
                    clickAction,
                    player,
                    slotHeldByCursor) ? EventResult.ALLOW : EventResult.DENY;
        }

        return EventResult.PASS;
    }
}
