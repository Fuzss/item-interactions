package fuzs.iteminteractions.common.impl.handler;

import fuzs.iteminteractions.common.api.v1.world.item.storage.ItemStorageHolder;
import fuzs.puzzleslib.common.api.event.v1.core.EventResult;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class ContainerClickInputHandler {

    /**
     * The event doesn't allow for properly overriding existing item behavior, as it only allows for returning
     * {@code true} from
     * {@link net.minecraft.world.inventory.AbstractContainerMenu#tryItemClickBehaviourOverride(Player, ClickAction,
     * Slot, ItemStack, ItemStack)}.
     * <p>
     * This works mostly, as long as in the case of bundles (which are the only vanilla items implementing this
     * behavior) we match the logic exactly with our override.
     */
    public static EventResult onContainerItemClick(ItemStack hoveredItem, Slot hoveredSlot, ItemStack itemHeldByCursor, SlotAccess slotHeldByCursor, ClickAction clickAction, Player player) {
        ItemStorageHolder holderHeldByCursor = ItemStorageHolder.ofItem(itemHeldByCursor);
        if (!holderHeldByCursor.isEmpty() && holderHeldByCursor.storage().hasContents(itemHeldByCursor)
                && holderHeldByCursor.overrideStackedOnOther(itemHeldByCursor, hoveredSlot, clickAction, player)) {
            return EventResult.INTERRUPT;
        }

        ItemStorageHolder hoveredHolder = ItemStorageHolder.ofItem(hoveredItem);
        if (!hoveredHolder.isEmpty() && hoveredHolder.storage().hasContents(hoveredItem)
                && hoveredHolder.overrideOtherStackedOnMe(hoveredItem,
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
