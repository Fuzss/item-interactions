package fuzs.iteminteractions.api.v1.provider;

import fuzs.iteminteractions.api.v1.provider.impl.EmptyProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;

/**
 * A holder class for individual {@link ItemContentsProvider} instances, mainly to include additional checks when
 * calling various methods.
 *
 * @param provider the wrapped {@link ItemContentsProvider}
 */
public record ItemContentsBehavior(ItemContentsProvider provider) {

    /**
     * @return new and possibly empty behavior instance
     */
    public static ItemContentsBehavior ofNullable(@Nullable ItemContentsProvider provider) {
        return provider != null ? new ItemContentsBehavior(provider) : empty();
    }

    /**
     * @return new empty behavior instance
     */
    public static ItemContentsBehavior empty() {
        return new ItemContentsBehavior(EmptyProvider.INSTANCE);
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
        return this.provider.allowsPlayerInteractions(containerStack, player);
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
        return this.provider.isItemAllowedInContainer(stackToAdd);
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
        return this.canAcceptItem(containerStack, stackToAdd, player) &&
                this.provider.canAddItem(containerStack, stackToAdd, player);
    }

    /**
     * Get the container implementation provided by <code>containerStack</code> as a {@link SimpleContainer}.
     *
     * @param containerStack item stack providing the container
     * @param player         player involved in the interaction
     * @return the provided container
     */
    public SimpleContainer getItemContainerView(ItemStack containerStack, Player player) {
        SimpleContainer container = this.provider.getItemContainer(containerStack, player, false);
        Objects.requireNonNull(container, "container is null");
        return container;
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
    public SimpleContainer getItemContainer(ItemStack containerStack, Player player) {
        SimpleContainer container = this.provider.getItemContainer(containerStack, player, true);
        Objects.requireNonNull(container, "container is null");
        return container;
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
        return this.canAcceptItem(containerStack, stackToAdd, player) &&
                this.getItemContainerView(containerStack, player)
                        .hasAnyMatching(stack -> ItemStack.isSameItem(stack, stackToAdd));
    }

    /**
     * How much space is available in the container provided by <code>containerStack</code> to add
     * <code>stackToAdd</code>.
     * <p>
     * Mainly used by bundles, otherwise {@link ItemContentsProvider#canAddItem} should be enough.
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
            return this.provider.getAcceptableItemCount(containerStack, stackToAdd, player);
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
        return !stackToAdd.isEmpty() && this.allowsPlayerInteractions(containerStack, player) &&
                this.isItemAllowedInContainer(stackToAdd);
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
        return this.provider.canProvideTooltipImage(containerStack, player);
    }

    /**
     * The image tooltip provided by the item stack.
     *
     * @param containerStack the item stack providing the container to show a tooltip for
     * @param player         player involved in the interaction
     * @return the image tooltip provided by the item stack.
     */
    public Optional<TooltipComponent> getTooltipImage(ItemStack containerStack, Player player) {
        Optional<TooltipComponent> tooltipImage = this.provider.getTooltipImage(containerStack, player);
        Objects.requireNonNull(tooltipImage, "tooltip image is null");
        return tooltipImage;
    }

    /**
     * @return the item container provider type
     */
    public ItemContentsProvider.Type<?> getType() {
        return this.provider.getType();
    }

    /**
     * @return is this the empty behavior singleton instance
     */
    public boolean isEmpty() {
        return this.provider == EmptyProvider.INSTANCE;
    }
}
