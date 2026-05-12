package fuzs.iteminteractions.common.api.v1.world.item.storage;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fuzs.iteminteractions.common.impl.ItemInteractions;
import fuzs.iteminteractions.common.impl.world.item.container.ItemStorageManager;
import fuzs.puzzleslib.common.api.init.v3.registry.RegistryFactory;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.Map;
import java.util.Optional;

/**
 * An interface that when implemented, represents a provider for any item to enable bundle-like inventory item
 * interactions (extracting and adding items via right-clicking on the item) and bundle-like tooltips.
 * <p>
 * A container does not necessarily need to provide both item interactions and tooltips, what is provided is defined by
 * implementing {@link ItemStorage#canPlayerInteractWith} and {@link ItemStorage#canProvideTooltipImage}.
 * <p>
 * This overrides any already implemented behavior (the default providers in Easy Shulker Boxes actually do this for
 * vanilla bundles).
 */
public interface ItemStorage {
    /**
     * The {@link ItemStorageType} registry key.
     */
    ResourceKey<Registry<ItemStorageType<?>>> REGISTRY_KEY = ResourceKey.createRegistryKey(ItemStorageManager.REGISTRY_KEY.identifier());
    /**
     * The {@link ItemStorageType} registry.
     */
    Registry<ItemStorageType<?>> REGISTRY = RegistryFactory.INSTANCE.createSynced(REGISTRY_KEY,
            ItemInteractions.id("empty"));
    /**
     * Codec that additionally to the provider itself also includes the provider type.
     */
    MapCodec<ItemStorage> CODEC = REGISTRY.byNameCodec().dispatchMap(ItemStorage::getType, ItemStorageType::codec);
    /**
     * Codec that includes a list of supported items.
     */
    Codec<Map.Entry<HolderSet<Item>, ItemStorage>> WITH_ITEMS_CODEC = RecordCodecBuilder.create(instance -> {
        return instance.group(Ingredient.NON_AIR_HOLDER_SET_CODEC.lenientOptionalFieldOf("supported_items",
                        HolderSet.empty()).forGetter(Map.Entry::getKey), CODEC.forGetter(Map.Entry::getValue))
                .apply(instance, Map::entry);
    });
    /**
     * Stream codec that additionally to the provider itself also includes the provider type.
     */
    StreamCodec<RegistryFriendlyByteBuf, ItemStorage> STREAM_CODEC = ByteBufCodecs.registry(ItemStorage.REGISTRY_KEY)
            .dispatch(ItemStorage::getType, ItemStorageType::streamCodec);

    /**
     * Returns whether extracting and adding items is supported.
     *
     * @param itemStack the item stack providing the storage
     * @param player    the player performing the interaction
     * @return are inventory interactions allowed?
     */
    default boolean canPlayerInteractWith(ItemStack itemStack, Player player) {
        return itemStack.getCount() == 1;
    }

    /**
     * Get the maximum stack size for this item in the current container.
     * <p>
     * Allows supporting containers with custom max stack sizes such as our Limitless Container library.
     *
     * @param container current container
     * @param slotIndex the menu slot index
     * @param itemStack item stack to retrieve stack size for
     * @return supported max stack size
     */
    default int getMaxStackSize(Container container, int slotIndex, ItemStack itemStack) {
        return container.getMaxStackSize(itemStack);
    }

    /**
     * does the item stack have data for stored items
     * <p>an easy check if the corresponding container is empty without having to create a container instance
     * <p>mainly used by tooltip image and client-side mouse scroll handler
     *
     * @param itemStack the container stack
     * @return is the item stack tag with stored item data present
     */
    default boolean hasContents(ItemStack itemStack) {
        return true;
    }

    /**
     * Used to synchronize item storage changes.
     *
     * @param itemStack the item stack providing the storage
     * @param player    the player performing the interaction
     * @see net.minecraft.world.item.BundleItem#broadcastChangesOnContainerMenu(Player)
     */
    default void broadcastChangesOnContainerMenu(ItemStack itemStack, Player player) {
        player.containerMenu.slotsChanged(player.getInventory());
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
     * Before this is called {@link #canPlayerInteractWith(ItemStack, Player)} and
     * {@link #isItemAllowedInContainer(ItemStack)} are checked.
     *
     * @param itemStack  the item stack providing the container to add <code>stack</code> to
     * @param stackToAdd the stack to be added to the container
     * @param player     the player interacting with both items
     * @return is adding any portion of <code>stackToAdd</code> to the container possible
     */
    default boolean canAddItem(ItemStack itemStack, ItemStack stackToAdd, Player player) {
        return this.getItemContainer(itemStack, player, false).canAddItem(stackToAdd);
    }

    /**
     * Get the container implementation provided by <code>containerStack</code> as a {@link SimpleContainer}, must not
     * return <code>null</code>.
     *
     * @param itemStack item stack providing the container
     * @param player    player involved in the interaction
     * @param isMutable attach a saving listener to the container (this is set to <code>false</code> when creating a
     *                  container e.g. for rendering a tooltip)
     * @return the provided container
     */
    SimpleContainer getItemContainer(ItemStack itemStack, Player player, boolean isMutable);

    /**
     * How much space is available in the container provided by <code>containerStack</code> to add
     * <code>stackToAdd</code>.
     * <p>
     * Mainly used by bundles, otherwise {@link ItemStorage#canAddItem} should be enough.
     * <p>
     * Before this is called {@link #canPlayerInteractWith(ItemStack, Player)} and
     * {@link #isItemAllowedInContainer(ItemStack)} are checked.
     *
     * @param itemStack  the item stack providing the container to add <code>stackToAdd</code> to
     * @param stackToAdd the stack to be added to the container
     * @param player     the player interacting with both item stacks
     * @return the portion of <code>stackToAdd</code> that can be added to the container
     */
    default int getAcceptableItemCount(ItemStack itemStack, ItemStack stackToAdd, Player player) {
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
     * Called when the selected item index for a container item changes from the player scrolling through the tooltip.
     *
     * @param itemStack            the item stack providing the container
     * @param previousSelectedItem the previous selected slot inside the container item
     * @param updatedSelectedItem  the next selected slot inside the container item
     */
    default void onToggleSelectedItem(ItemStack itemStack, int previousSelectedItem, int updatedSelectedItem) {
        // NO-OP
    }

    /**
     * @return the item container provider type
     */
    ItemStorageType<?> getType();
}
