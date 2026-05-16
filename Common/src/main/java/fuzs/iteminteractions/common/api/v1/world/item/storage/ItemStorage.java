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
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import org.joml.Vector2ic;

import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;

/**
 * An interface that when implemented, represents a provider for any item to enable bundle-like inventory item
 * interactions (extracting and adding items via right-clicking on the item) and bundle-like tooltips.
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
    boolean canPlayerInteractWith(ItemStack itemStack, Player player);

    /**
     * does the item stack have data for stored items
     * <p>an easy check if the corresponding container is empty without having to create a container instance
     * <p>mainly used by tooltip image and client-side mouse scroll handler
     *
     * @param itemStack the container stack
     * @return is the item stack tag with stored item data present
     */
    boolean hasContents(ItemStack itemStack);

    boolean overrideStackedOnOther(ItemStorageHolder holder, ItemStack itemStack, Slot slot, ClickAction clickAction, Player player);

    boolean overrideOtherStackedOnMe(ItemStorageHolder holder, ItemStack itemStack, ItemStack itemHeldByCursor, Slot slot, ClickAction clickAction, Player player, SlotAccess slotHeldByCursor);

    /**
     * Is <code>stackToAdd</code> allowed to be added to the container supplied by <code>containerStack</code>.
     * <p>
     * This should be the same behavior as vanilla's {@link Item#canFitInsideContainerItems()}.
     *
     * @param stackToAdd the stack to be added to the container
     * @return is <code>stack</code> allowed to be added to the container
     */
    boolean isItemAllowedInContainer(ItemStack stackToAdd);

    /**
     * Get the container implementation provided by <code>containerStack</code> as a {@link SimpleContainer}, must not
     * return <code>null</code>.
     *
     * @param itemStack item stack providing the container
     * @param player    player involved in the interaction
     * @return the provided container
     */
    Container getItemContainer(ItemStack itemStack, Player player);

    /**
     * Is there enough space in the container provided by <code>containerStack</code> to add <code>stack</code> (not
     * necessarily the full stack).
     *
     * @param itemStack  the item stack providing the container to add <code>stack</code> to
     * @param stackToAdd the stack to be added to the container
     * @param player     the player interacting with both items
     * @return is adding any portion of <code>stackToAdd</code> to the container possible
     */
    boolean canAddItem(ItemStack itemStack, ItemStack stackToAdd, Player player);

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
    int getAcceptableItemCount(ItemStack itemStack, ItemStack stackToAdd, Player player);

    /**
     * The image tooltip provided by the item stack.
     *
     * @param itemStack the item stack providing the item storage
     * @param player    the player
     * @return the image tooltip provided by the item stack
     *
     * @see Item#getTooltipImage(ItemStack)
     */
    Optional<Optional<TooltipComponent>> getTooltipImage(ItemStack itemStack, Player player);

    /**
     * @see Item#isBarVisible(ItemStack)
     */
    Optional<Boolean> isBarVisible(ItemStack itemStack, Player player);

    /**
     * @see Item#getBarWidth(ItemStack)
     */
    OptionalInt getBarWidth(ItemStack itemStack, Player player);

    /**
     * @see Item#getBarColor(ItemStack)
     */
    OptionalInt getBarColor(ItemStack itemStack, Player player);

    int getSelectedItem(ItemStack itemStack);

    int scrollSelectedItem(ItemStack itemStack, Container container, Vector2ic scrollXY);

    /**
     * Called when the selected item index for a container item changes from the player scrolling through the tooltip.
     *
     * @param itemStack    the item stack providing the container
     * @param selectedItem the updated selected slot inside the container item
     * @param slotClicked  the action was triggered by clicking on the item slot
     * @see net.minecraft.world.item.BundleItem#toggleSelectedItem(ItemStack, int)
     */
    void toggleSelectedItem(ItemStack itemStack, int selectedItem, boolean slotClicked);

    void playRemoveOneSound(Player player);

    void playInsertSound(Player player);

    /**
     * @return the item container provider type
     */
    ItemStorageType<?> getType();
}
