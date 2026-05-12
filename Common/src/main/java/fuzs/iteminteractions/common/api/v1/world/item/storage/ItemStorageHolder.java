package fuzs.iteminteractions.common.api.v1.world.item.storage;

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

    public boolean overrideStackedOnOther(ItemStack itemStack, Slot slot, ClickAction clickAction, Player player) {
        if (this.allowsPlayerInteractions(itemStack, player)) {
            boolean broadcastChanges = ItemInteractionHelper.overrideStackedOnOther(itemStack,
                    () -> this.getMutableContainer(itemStack, player),
                    slot,
                    clickAction,
                    player,
                    (ItemStack item) -> {
                        return this.getAcceptableItemCount(itemStack, item, player);
                    },
                    (Container container, ItemStack item) -> this.storage().getMaxStackSize(container, -1, item));
            if (broadcastChanges) {
                this.storage().broadcastChangesOnContainerMenu(itemStack, player);
            }

            return broadcastChanges;
        } else {
            return false;
        }
    }

    public boolean overrideOtherStackedOnMe(ItemStack itemStack, ItemStack itemHeldByCursor, Slot slot, ClickAction clickAction, Player player, SlotAccess slotHeldByCursor) {
        if (this.allowsPlayerInteractions(itemStack, player)) {
            boolean broadcastChanges = ItemInteractionHelper.overrideOtherStackedOnMe(itemStack,
                    () -> this.getMutableContainer(itemStack, player),
                    itemHeldByCursor,
                    slot,
                    clickAction,
                    player,
                    slotHeldByCursor,
                    (ItemStack item) -> {
                        return this.getAcceptableItemCount(itemStack, item, player);
                    },
                    (Container container, ItemStack item) -> this.storage().getMaxStackSize(container, -1, item),
                    () -> this.storage().onToggleSelectedItem(itemStack, 0, -1));
            if (broadcastChanges) {
                this.storage().broadcastChangesOnContainerMenu(itemStack, player);
            }

            return broadcastChanges;
        } else {
            return false;
        }
    }

    /**
     * @see net.minecraft.world.item.BundleItem#broadcastChangesOnContainerMenu(Player)
     */
    private void broadcastChangesOnContainerMenu(Player player) {
        player.containerMenu.slotsChanged(player.getInventory());
    }

    /**
     * Does this provider support item inventory interactions (extracting and adding items) on the given
     * <code>containterStack</code>.
     *
     * @param containerStack the container stack
     * @param player         the player performing the interaction
     * @return are inventory interactions allowed (is a container present on this item)
     */
    public boolean allowsPlayerInteractions(ItemStack containerStack, Player player) {
        return this.storage.canPlayerInteractWith(containerStack, player);
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
        return this.storage.isItemAllowedInContainer(stackToAdd);
    }

    /**
     * Is there enough space in the container provided by <code>containerStack</code> to add <code>stack</code> (not
     * necessarily the full stack).
     * <p>
     * Before this is called {@link #allowsPlayerInteractions(ItemStack, Player)} and
     * {@link #isItemAllowedInContainer(ItemStack)} are checked.
     *
     * @param containerStack the item stack providing the container to add <code>stack</code> to
     * @param stackToAdd     the stack to be added to the container
     * @param player         the player interacting with both items
     * @return is adding any portion of <code>stackToAdd</code> to the container possible
     */
    public boolean canAddItem(ItemStack containerStack, ItemStack stackToAdd, Player player) {
        return this.canAcceptItem(containerStack, stackToAdd, player) && this.storage.canAddItem(containerStack,
                stackToAdd,
                player);
    }

    /**
     * Get the container implementation provided by <code>containerStack</code> as a {@link SimpleContainer}.
     *
     * @param containerStack item stack providing the container
     * @param player         player involved in the interaction
     * @return the provided container
     */
    public SimpleContainer getContainerView(ItemStack containerStack, Player player) {
        return this.storage.getItemContainer(containerStack, player, false);
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
    public SimpleContainer getMutableContainer(ItemStack containerStack, Player player) {
        return this.storage.getItemContainer(containerStack, player, true);
    }

    /**
     * Is there any item of the same type as <code>stackToAdd</code> already in the container provided by
     * <code>containerStack</code>.
     * <p>
     * Before this is called {@link #allowsPlayerInteractions(ItemStack, Player)} and
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
     * Before this is called {@link #allowsPlayerInteractions(ItemStack, Player)} and
     * {@link #isItemAllowedInContainer(ItemStack)} are checked.
     *
     * @param containerStack the item stack providing the container to add <code>stackToAdd</code> to
     * @param stackToAdd     the stack to be added to the container
     * @param player         the player interacting with both item stacks
     * @return the portion of <code>stackToAdd</code> that can be added to the container
     */
    public int getAcceptableItemCount(ItemStack containerStack, ItemStack stackToAdd, Player player) {
        if (this.canAcceptItem(containerStack, stackToAdd, player)) {
            return this.storage.getAcceptableItemCount(containerStack, stackToAdd, player);
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
        return !stackToAdd.isEmpty() && this.allowsPlayerInteractions(containerStack, player)
                && this.isItemAllowedInContainer(stackToAdd);
    }

    /**
     * Does this provider support an image tooltip.
     * <p>
     * This is required despite {@link #getTooltipImage} providing an {@link Optional} when overriding the tooltip image
     * for items which normally provide their own (like bundles).
     *
     * @param containerStack the item stack providing the container to show a tooltip for
     * @param player         player involved in the interaction
     * @return does <code>containerStack</code> provide a tooltip image
     */
    public boolean canProvideTooltipImage(ItemStack containerStack, Player player) {
        return this.storage.canProvideTooltipImage(containerStack, player);
    }

    /**
     * The image tooltip provided by the item stack.
     *
     * @param containerStack the item stack providing the container to show a tooltip for
     * @param player         player involved in the interaction
     * @return the image tooltip provided by the item stack.
     */
    public Optional<TooltipComponent> getTooltipImage(ItemStack containerStack, Player player) {
        return this.storage.getTooltipImage(containerStack, player);
    }

    /**
     * @return the item container provider type
     */
    public ItemStorageType<?> getType() {
        return this.storage.getType();
    }

    /**
     * @return is this the empty behavior singleton instance
     */
    public boolean isEmpty() {
        return this.storage == VoidStorage.INSTANCE;
    }
}
