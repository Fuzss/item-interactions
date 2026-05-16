package fuzs.iteminteractions.common.api.v2.world.item.storage;

import fuzs.iteminteractions.common.impl.world.item.container.ItemStorageManager;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

import java.util.Optional;
import java.util.OptionalInt;

/**
 * A holder class for individual {@link ItemStorage} instances, mainly to include additional checks when calling various
 * methods.
 *
 * @param storage the wrapped {@link ItemStorage}
 */
public record ItemStorageHolder(ItemStorage storage) {
    /**
     * The empty instance.
     */
    public static final ItemStorageHolder EMPTY = new ItemStorageHolder(VoidStorage.INSTANCE);

    /**
     * Get a registered holder for an item.
     *
     * @param itemStack the item stack
     * @return the holder that may be empty
     */
    public static ItemStorageHolder ofItem(ItemStack itemStack) {
        return itemStack.isEmpty() ? EMPTY : ItemStorageManager.getHolder(itemStack);
    }

    /**
     * @return the holder that may be empty
     */
    public static ItemStorageHolder ofNullable(@Nullable ItemStorage storage) {
        return storage != null ? new ItemStorageHolder(storage) : EMPTY;
    }

    public boolean overrideStackedOnOther(ItemStack itemStack, Slot slot, ClickAction clickAction, Player player) {
        return this.storage().overrideStackedOnOther(this, itemStack, slot, clickAction, player);
    }

    public boolean overrideOtherStackedOnMe(ItemStack itemStack, ItemStack itemHeldByCursor, Slot slot, ClickAction clickAction, Player player, SlotAccess slotHeldByCursor) {
        return this.storage()
                .overrideOtherStackedOnMe(this,
                        itemStack,
                        itemHeldByCursor,
                        slot,
                        clickAction,
                        player,
                        slotHeldByCursor);
    }

    /**
     * @return is the holder empty
     */
    public boolean isEmpty() {
        return this.storage == VoidStorage.INSTANCE;
    }

    /**
     * Does this provider support item inventory interactions (extracting and adding items) on the given
     * <code>containterStack</code>.
     *
     * @param itemStack the container stack
     * @param player    the player
     * @return are inventory interactions allowed (is a container present on this item)
     */
    public boolean isPresentFor(ItemStack itemStack, Player player) {
        return this.storage().canPlayerInteractWith(itemStack, player);
    }

    /**
     * Can the container item accept another item, checks for the player being allowed to interact as well as the
     * container item being able to hold the other item.
     *
     * @param itemStack  the item stack providing the container to add <code>stackToAdd</code> to
     * @param stackToAdd the stack to be added to the container
     * @param player     the player interacting with both item stacks
     * @return can the item be added
     */
    public boolean canAcceptItem(ItemStack itemStack, ItemStack stackToAdd, Player player) {
        return !stackToAdd.isEmpty() && this.isPresentFor(itemStack, player) && this.storage()
                .isItemAllowedInContainer(stackToAdd);
    }

    /**
     * Is there enough space in the container provided by <code>containerStack</code> to add <code>stack</code> (not
     * necessarily the full stack).
     *
     * @param itemStack  the item stack providing the container to add <code>stack</code> to
     * @param stackToAdd the stack to be added to the container
     * @param player     the player interacting with both items
     * @return is adding any portion of <code>stackToAdd</code> to the container possible
     */
    public boolean canAddItem(ItemStack itemStack, ItemStack stackToAdd, Player player) {
        return this.canAcceptItem(itemStack, stackToAdd, player) && this.storage()
                .canAddItem(itemStack, stackToAdd, player);
    }

    /**
     * Is there any item of the same type as <code>stackToAdd</code> already in the container provided by
     * <code>containerStack</code>.
     *
     * @param itemStack  the item stack providing the container to add <code>stack</code> to
     * @param stackToAdd the stack to be searched for in the container
     * @param player     the player interacting with both items
     * @return is any item of the same type as <code>stackToAdd</code> already in the container
     */
    public boolean hasAnyOf(ItemStack itemStack, ItemStack stackToAdd, Player player, boolean isSameComponents) {
        return this.canAcceptItem(itemStack, stackToAdd, player) && this.getItemContainer(itemStack, player)
                .hasAnyMatching((ItemStack item) -> {
                    if (isSameComponents) {
                        return ItemStack.isSameItemSameComponents(item, stackToAdd);
                    } else {
                        return ItemStack.isSameItem(item, stackToAdd);
                    }
                });
    }

    /**
     * How much space is available in the container provided by <code>containerStack</code> to add
     * <code>stackToAdd</code>.
     *
     * @param itemStack the item stack providing the container to add <code>stackToAdd</code> to
     * @param otherItem the stack to be added to the container
     * @param player    the player interacting with both item stacks
     * @return the portion of <code>stackToAdd</code> that can be added to the container
     */
    public int getAcceptableItemCount(ItemStack itemStack, ItemStack otherItem, Player player) {
        if (this.canAcceptItem(itemStack, otherItem, player)) {
            return this.storage().getAcceptableItemCount(itemStack, otherItem, player);
        } else {
            return 0;
        }
    }

    /**
     * Get the container implementation provided by <code>containerStack</code> as a {@link SimpleContainer}.
     *
     * @param itemStack item stack providing the container
     * @param player    player involved in the interaction
     * @return the provided container
     */
    public Container getItemContainer(ItemStack itemStack, Player player) {
        return this.storage().getItemContainer(itemStack, player);
    }

    public Optional<Optional<TooltipComponent>> getTooltipImage(ItemStack itemStack, Player player) {
        return this.isPresentFor(itemStack, player) ? this.storage().getTooltipImage(itemStack, player) :
                Optional.empty();
    }

    public Optional<Boolean> isBarVisible(ItemStack itemStack, Player player) {
        return this.isPresentFor(itemStack, player) ? this.storage().isBarVisible(itemStack, player) : Optional.empty();
    }

    public OptionalInt getBarWidth(ItemStack itemStack, Player player) {
        return this.isPresentFor(itemStack, player) ? this.storage().getBarWidth(itemStack, player) :
                OptionalInt.empty();
    }

    public OptionalInt getBarColor(ItemStack itemStack, Player player) {
        return this.isPresentFor(itemStack, player) ? this.storage().getBarColor(itemStack, player) :
                OptionalInt.empty();
    }
}
