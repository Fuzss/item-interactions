package fuzs.iteminteractions.api.v1.provider;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fuzs.iteminteractions.impl.ItemInteractions;
import fuzs.iteminteractions.impl.world.item.container.ItemContentsProviders;
import fuzs.puzzleslib.api.init.v3.registry.RegistryFactory;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.Map;
import java.util.Optional;

/**
 * An interface that when implemented represents a provider for any item to enable bundle-like inventory item
 * interactions (extracting and adding items via right-clicking on the item) and bundle-like tooltips.
 * <p>
 * A container does not necessarily need to provide both item interactions and tooltips, what is provided is defined by
 * implementing {@link ItemContentsProvider#allowsPlayerInteractions} and
 * {@link ItemContentsProvider#canProvideTooltipImage}.
 * <p>
 * This overrides any already implemented behavior (the default providers in Easy Shulker Boxes actually do this for
 * vanilla bundles).
 */
public interface ItemContentsProvider {
    /**
     * The {@link Type} registry key.
     */
    ResourceKey<Registry<Type>> REGISTRY_KEY = ResourceKey.createRegistryKey(ItemContentsProviders.ITEM_CONTAINER_PROVIDER_LOCATION);
    /**
     * The {@link Type} registry.
     */
    Registry<Type> REGISTRY = RegistryFactory.INSTANCE.create(REGISTRY_KEY, ItemInteractions.id("empty"), true);
    /**
     * Codec that additionally to the provider itself also includes the provider type.
     */
    MapCodec<ItemContentsProvider> CODEC = REGISTRY.byNameCodec()
            .dispatchMap(ItemContentsProvider::getType, Type::mapCodec);
    /**
     * Codec that includes a list of supported items.
     */
    Codec<Map.Entry<HolderSet<Item>, ItemContentsProvider>> WITH_ITEMS_CODEC = RecordCodecBuilder.create(instance -> {
        return instance.group(RegistryCodecs.homogeneousList(Registries.ITEM)
                .fieldOf("supported_items")
                .forGetter(Map.Entry::getKey), CODEC.forGetter(Map.Entry::getValue)).apply(instance, Map::entry);
    });

    /**
     * Does this provider support item inventory interactions (extracting and adding items) on the given
     * <code>containterStack</code>.
     *
     * @param containerStack the container stack
     * @param player         the player performing the interaction
     * @return are inventory interactions allowed (is a container present on this item)
     */
    default boolean allowsPlayerInteractions(ItemStack containerStack, Player player) {
        return containerStack.getCount() == 1;
    }

    /**
     * Get the maximum stack size for this item in the current container.
     * <p>
     * Allows supporting containers with custom max stack sizes such as our Limitless Container library.
     *
     * @param container current container
     * @param itemStack item stack to retrieve stack size for
     * @return supported max stack size
     */
    default int getMaxStackSize(Container container, ItemStack itemStack) {
        return itemStack.getMaxStackSize();
    }

    /**
     * does the item stack have data for stored items
     * <p>an easy check if the corresponding container is empty without having to create a container instance
     * <p>mainly used by tooltip image and client-side mouse scroll handler
     *
     * @param containerStack the container stack
     * @return is the item stack tag with stored item data present
     */
    boolean hasContents(ItemStack containerStack);

    /**
     * called on the client-side to sync changes made during inventory item interactions back to the server
     * <p>this only works in the creative inventory, as any other menu does not allow the client to sync changes
     * <p>only really need for ender chests right now
     *
     * @param containerStack item stack providing the container
     * @param player         the player performing the item interaction
     */
    default void broadcastContainerChanges(ItemStack containerStack, Player player) {
        // NO-OP
    }

    /**
     * Is <code>stackToAdd</code> allowed to be added to the container supplied by <code>containerStack</code>.
     * <p>
     * This should be the same behavior as vanilla's {@link Item#canFitInsideContainerItems()}.
     *
     * @param stackToAdd the stack to be added to the container
     * @return is <code>stack</code> allowed to be added to the container
     */
    default boolean isItemAllowedInContainer(ItemStack stackToAdd) {
        return true;
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
    default boolean canAddItem(ItemStack containerStack, ItemStack stackToAdd, Player player) {
        return this.getItemContainer(containerStack, player, false).canAddItem(stackToAdd);
    }

    /**
     * Get the container implementation provided by <code>containerStack</code> as a {@link SimpleContainer}, must not
     * return <code>null</code>.
     *
     * @param containerStack item stack providing the container
     * @param player         player involved in the interaction
     * @param allowSaving    attach a saving listener to the container (this is set to <code>false</code> when creating
     *                       a container e.g. for rendering a tooltip)
     * @return the provided container
     */
    SimpleContainer getItemContainer(ItemStack containerStack, Player player, boolean allowSaving);

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
    default int getAcceptableItemCount(ItemStack containerStack, ItemStack stackToAdd, Player player) {
        return stackToAdd.getCount();
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
    boolean canProvideTooltipImage(ItemStack containerStack, Player player);

    /**
     * The image tooltip provided by the item stack.
     *
     * @param containerStack the item stack providing the container to show a tooltip for
     * @param player         player involved in the interaction
     * @return the image tooltip provided by the item stack.
     */
    Optional<TooltipComponent> getTooltipImage(ItemStack containerStack, Player player);

    /**
     * @return the item container provider type
     */
    Type getType();

    /**
     * A type for identifying and serializing item container provider implementations.
     *
     * @param mapCodec the item container provider codec
     */
    record Type(MapCodec<? extends ItemContentsProvider> mapCodec) {

        @SuppressWarnings("unchecked")
        public Codec<ItemContentsProvider> codec() {
            return (Codec<ItemContentsProvider>) this.mapCodec.codec();
        }
    }
}
