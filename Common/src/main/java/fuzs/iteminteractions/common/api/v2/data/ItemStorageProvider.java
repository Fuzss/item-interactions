package fuzs.iteminteractions.common.api.v2.data;

import fuzs.iteminteractions.common.api.v2.world.item.storage.ItemStorage;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.registries.SingleRegistryBootstrap;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

/**
 * A base implementation of {@link SingleRegistryBootstrap} for generating the item storage definitions of the mod.
 * <p>
 * Subclasses implement {@link #addItemStorageDefinitions(BootstrapContext)} and register definitions via the various
 * {@code add} methods, mirroring the vanilla storages.
 */
public interface ItemStorageProvider extends SingleRegistryBootstrap<ItemStorage.Definition> {
    default void add(BootstrapContext<ItemStorage.Definition> output, ItemStorage storage, TagKey<Item> tag) {
        this.add(output, tag.location(), storage, tag);
    }

    default void add(BootstrapContext<ItemStorage.Definition> output, Identifier id, ItemStorage storage, TagKey<Item> tag) {
        this.add(output, id, storage, output.lookup(Registries.ITEM).getOrThrow(tag));
    }

    default void add(BootstrapContext<ItemStorage.Definition> output, ItemStorage storage, Item item) {
        this.add(output, BuiltInRegistries.ITEM.getKey(item), storage, item);
    }

    default void add(BootstrapContext<ItemStorage.Definition> output, Identifier id, ItemStorage storage, Item item) {
        this.add(output, id, storage, HolderSet.direct(BuiltInRegistries.ITEM::wrapAsHolder, item));
    }

    default void add(BootstrapContext<ItemStorage.Definition> output, Identifier id, ItemStorage storage, HolderSet<Item> holderSet) {
        output.register(ResourceKey.create(ItemStorage.Definition.REGISTRY_KEY, id),
                new ItemStorage.Definition(holderSet, storage));
    }
}
