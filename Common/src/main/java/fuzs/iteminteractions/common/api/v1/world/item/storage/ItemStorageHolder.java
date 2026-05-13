package fuzs.iteminteractions.common.api.v1.world.item.storage;

import fuzs.iteminteractions.common.impl.world.item.component.SelectedItem;
import fuzs.iteminteractions.common.impl.world.item.container.ItemInteractionHelper;
import fuzs.iteminteractions.common.impl.world.item.container.ItemStorageManager;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

import java.util.Optional;

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
        return itemStack.isEmpty() ? EMPTY : ItemStorageManager.get(itemStack);
    }

    /**
     * @return the holder that may be empty
     */
    public static ItemStorageHolder ofNullable(@Nullable ItemStorage storage) {
        return storage != null ? new ItemStorageHolder(storage) : EMPTY;
    }

    /**
     * @see net.minecraft.world.item.BundleItem#overrideStackedOnOther(ItemStack, Slot, ClickAction, Player)
     */
    public boolean overrideStackedOnOther(ItemStack itemStack, Slot slot, ClickAction clickAction, Player player) {
        return ItemInteractionHelper.overrideStackedOnOther(itemStack,
                () -> this.getMutableContainer(itemStack, player),
                slot,
                clickAction,
                player,
                (ItemStack item) -> {
                    return this.getAcceptableItemCount(itemStack, item, player);
                },
                // TODO this must call the method from the item storage implementation
                Container::getMaxStackSize);
    }

    /**
     * @see net.minecraft.world.item.BundleItem#overrideOtherStackedOnMe(ItemStack, ItemStack, Slot, ClickAction,
     *         Player, SlotAccess)
     */
    public boolean overrideOtherStackedOnMe(ItemStack itemStack, ItemStack itemHeldByCursor, Slot slot, ClickAction clickAction, Player player, SlotAccess slotHeldByCursor) {
        if (clickAction == ClickAction.PRIMARY && itemHeldByCursor.isEmpty()) {
            this.storage().toggleSelectedItem(itemStack, SelectedItem.DEFAULT_SELECTED_ITEM);
            return false;
        } else {
            return ItemInteractionHelper.overrideOtherStackedOnMe(itemStack,
                    () -> this.getMutableContainer(itemStack, player),
                    itemHeldByCursor,
                    slot,
                    clickAction,
                    player,
                    slotHeldByCursor,
                    (ItemStack item) -> {
                        return this.getAcceptableItemCount(itemStack, item, player);
                    },
                    // TODO this must call the method from the item storage implementation
                    Container::getMaxStackSize,
                    () -> this.storage().toggleSelectedItem(itemStack, SelectedItem.DEFAULT_SELECTED_ITEM));
        }
    }

    /**
     * Does this provider support item inventory interactions (extracting and adding items) on the given
     * <code>containterStack</code>.
     *
     * @param itemStack the container stack
     * @param player    the player performing the interaction
     * @return are inventory interactions allowed (is a container present on this item)
     */
    public boolean canPlayerInteractWith(ItemStack itemStack, Player player) {
        return this.storage().canPlayerInteractWith(itemStack, player);
    }

    /**
     * Is <code>stackToAdd</code> allowed to be added to the container supplied by <code>containerStack</code>.
     * <p>
     * This should be the same behavior as vanilla's {@link Item#canFitInsideContainerItems()}.
     *
     * @param stackToAdd the stack to be added to the container
     * @return is <code>stack</code> allowed to be added to the container
     */
    public boolean isItemAllowedInContainer(ItemStack stackToAdd) {
        return this.storage().isItemAllowedInContainer(stackToAdd);
    }

    /**
     * Is there enough space in the container provided by <code>containerStack</code> to add <code>stack</code> (not
     * necessarily the full stack).
     * <p>
     * Before this is called {@link #canPlayerInteractWith(ItemStack, Player)} and
     * {@link #isItemAllowedInContainer(ItemStack)} are checked.
     *
     * @param containerStack the item stack providing the container to add <code>stack</code> to
     * @param stackToAdd     the stack to be added to the container
     * @param player         the player interacting with both items
     * @return is adding any portion of <code>stackToAdd</code> to the container possible
     */
    public boolean canAddItem(ItemStack containerStack, ItemStack stackToAdd, Player player) {
        return this.canAcceptItem(containerStack, stackToAdd, player) && this.storage()
                .canAddItem(containerStack, stackToAdd, player);
    }

    /**
     * Get the container implementation provided by <code>containerStack</code> as a {@link SimpleContainer}.
     *
     * @param containerStack item stack providing the container
     * @param player         player involved in the interaction
     * @return the provided container
     */
    public Container getContainerView(ItemStack containerStack, Player player) {
        return this.storage().getItemContainer(containerStack, player, false);
    }

    /**
     * Get the container implementation provided by <code>containerStack</code> as a {@link SimpleContainer}.
     * <p>
     * Attaches a saving listener to the container.
     *
     * @param containerStack item stack providing the container
     * @param player         player involved in the interaction
     * @return the provided container
     */
    public Container getMutableContainer(ItemStack containerStack, Player player) {
        return this.storage().getItemContainer(containerStack, player, true);
    }

    /**
     * Is there any item of the same type as <code>stackToAdd</code> already in the container provided by
     * <code>containerStack</code>.
     * <p>
     * Before this is called {@link #canPlayerInteractWith(ItemStack, Player)} and
     * {@link #isItemAllowedInContainer(ItemStack)} are checked.
     *
     * @param containerStack the item stack providing the container to add <code>stack</code> to
     * @param stackToAdd     the stack to be searched for in the container
     * @param player         the player interacting with both items
     * @return is any item of the same type as <code>stackToAdd</code> already in the container
     */
    public boolean hasAnyOf(ItemStack containerStack, ItemStack stackToAdd, Player player) {
        return this.canAcceptItem(containerStack, stackToAdd, player) && this.getContainerView(containerStack, player)
                .hasAnyMatching((ItemStack item) -> ItemStack.isSameItem(item, stackToAdd));
    }

    /**
     * How much space is available in the container provided by <code>containerStack</code> to add
     * <code>stackToAdd</code>.
     * <p>
     * Mainly used by bundles, otherwise {@link ItemStorage#canAddItem} should be enough.
     * <p>
     * Before this is called {@link #canPlayerInteractWith(ItemStack, Player)} and
     * {@link #isItemAllowedInContainer(ItemStack)} are checked.
     *
     * @param containerStack the item stack providing the container to add <code>stackToAdd</code> to
     * @param stackToAdd     the stack to be added to the container
     * @param player         the player interacting with both item stacks
     * @return the portion of <code>stackToAdd</code> that can be added to the container
     */
    public int getAcceptableItemCount(ItemStack containerStack, ItemStack stackToAdd, Player player) {
        if (this.canAcceptItem(containerStack, stackToAdd, player)) {
            return this.storage().getAcceptableItemCount(containerStack, stackToAdd, player);
        } else {
            return 0;
        }
    }

    /**
     * Can the container item accept another item, checks for the player being allowed to interact as well as the
     * container item being able to hold the other item.
     *
     * @param containerStack the item stack providing the container to add <code>stackToAdd</code> to
     * @param stackToAdd     the stack to be added to the container
     * @param player         the player interacting with both item stacks
     * @return can the item be added
     */
    public boolean canAcceptItem(ItemStack containerStack, ItemStack stackToAdd, Player player) {
        return !stackToAdd.isEmpty() && this.canPlayerInteractWith(containerStack, player)
                && this.isItemAllowedInContainer(stackToAdd);
    }

    /**
     * The image tooltip provided by the item stack.
     *
     * @param containerStack the item stack providing the container to show a tooltip for
     * @param player         player involved in the interaction
     * @return the image tooltip provided by the item stack.
     */
    public Optional<Optional<TooltipComponent>> getTooltipImage(ItemStack containerStack, Player player) {
        if (this.storage().canProvideTooltipImage(containerStack, player)) {
            return Optional.of(this.storage().getTooltipImage(containerStack, player));
        } else {
            return Optional.empty();
        }
    }

    /**
     * @return is this the empty behavior singleton instance
     */
    public boolean isEmpty() {
        return this.storage == VoidStorage.INSTANCE;
    }
}
