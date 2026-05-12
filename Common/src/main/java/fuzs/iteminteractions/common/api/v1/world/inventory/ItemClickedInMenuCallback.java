package fuzs.iteminteractions.common.api.v1.world.inventory;

import fuzs.puzzleslib.common.api.event.v1.core.EventInvoker;
import fuzs.puzzleslib.common.api.event.v1.core.EventResult;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

@FunctionalInterface
public interface ItemClickedInMenuCallback {
    EventInvoker<ItemClickedInMenuCallback> EVENT = EventInvoker.lookup(ItemClickedInMenuCallback.class);

    /**
     * Called in
     * {@link net.minecraft.world.inventory.AbstractContainerMenu#tryItemClickBehaviourOverride(Player, ClickAction,
     * Slot, ItemStack, ItemStack)} when an item stack is clicked onto another.
     * <p>
     * Allows for overriding the behaviors defined by each item, both the item held by the cursor and in the clicked
     * slot.
     *
     * @param hoveredItem      the item in the slot
     * @param hoveredSlot      the slot the cursor is hovering
     * @param itemHeldByCursor the item carried by the cursor
     * @param slotHeldByCursor the slot access for the cursor item
     * @param clickAction      the mouse button
     * @param player           the player
     * @return <ul>
     *         <li>{@link EventResult#INTERRUPT INTERRUPT} to prevent vanilla from handling the item interaction</li>
     *         <li>{@link EventResult#PASS PASS} to allow item-specific overrides to run</li>
     *         </ul>
     *
     * @see ItemStack#overrideStackedOnOther(Slot, ClickAction, Player)
     * @see ItemStack#overrideOtherStackedOnMe(ItemStack, Slot, ClickAction, Player, SlotAccess)
     */
    EventResult onItemClickedInMenu(ItemStack hoveredItem, Slot hoveredSlot, ItemStack itemHeldByCursor, SlotAccess slotHeldByCursor, ClickAction clickAction, Player player);
}
