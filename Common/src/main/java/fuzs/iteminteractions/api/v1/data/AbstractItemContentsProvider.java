package fuzs.iteminteractions.api.v1.data;

import com.google.common.collect.Maps;
import fuzs.iteminteractions.api.v1.provider.ItemContentsProvider;
import fuzs.iteminteractions.impl.world.item.container.ItemContentsProviders;
import net.minecraft.resources.Identifier;
import fuzs.puzzleslib.api.data.v2.core.DataProviderContext;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * TODO remove deprecated members
 */
public abstract class AbstractItemContentsProvider implements DataProvider {
    private final Map<Identifier, Map.Entry<HolderSet<Item>, ItemContentsProvider>> providers = Maps.newHashMap();
    private final String modId;
    private final PackOutput.PathProvider pathProvider;
    private final CompletableFuture<HolderLookup.Provider> registries;

    public AbstractItemContentsProvider(DataProviderContext context) {
        this(context.getModId(), context.getPackOutput(), context.getRegistries());
    }

    public AbstractItemContentsProvider(String modId, PackOutput packOutput, CompletableFuture<HolderLookup.Provider> registries) {
        this.modId = modId;
        this.pathProvider = packOutput.createRegistryElementsPathProvider(ItemContentsProviders.REGISTRY_KEY);
        this.registries = registries;
    }

    @Override
    public CompletableFuture<?> run(CachedOutput output) {
        return this.registries.thenCompose((HolderLookup.Provider registries) -> {
            return this.run(output, registries);
        });
    }

    public CompletableFuture<?> run(CachedOutput output, HolderLookup.Provider registries) {
        this.addItemProviders(registries);
        List<CompletableFuture<?>> completableFutures = new ArrayList<>();
        for (Map.Entry<Identifier, Map.Entry<HolderSet<Item>, ItemContentsProvider>> entry : this.providers.entrySet()) {
            Path path = this.pathProvider.json(entry.getKey());
            completableFutures.add(DataProvider.saveStable(output,
                    registries,
                    ItemContentsProvider.WITH_ITEMS_CODEC,
                    entry.getValue(),
                    path));
        }
        return CompletableFuture.allOf(completableFutures.toArray(CompletableFuture[]::new));
    }

    public abstract void addItemProviders(HolderLookup.Provider registries);

    public void add(HolderLookup.RegistryLookup<Item> itemLookup, ItemContentsProvider provider, TagKey<Item> tagKey) {
        this.add(itemLookup, tagKey.location(), provider, tagKey);
    }

    @Deprecated(forRemoval = true)
    public void add(HolderLookup.RegistryLookup<Item> itemLookup, String id, ItemContentsProvider provider, TagKey<Item> tagKey) {
        this.add(itemLookup, Identifier.fromNamespaceAndPath(this.modId, id), provider, tagKey);
    }

    public final void add(HolderLookup.RegistryLookup<Item> itemLookup, Identifier identifier, ItemContentsProvider provider, TagKey<Item> tagKey) {
        this.add(identifier, provider, itemLookup.getOrThrow(tagKey));
    }

    @Deprecated(forRemoval = true)
    public void add(HolderLookup.RegistryLookup<Item> itemLookup, ItemContentsProvider provider, Item item) {
        this.add(provider, item);
    }

    public void add(ItemContentsProvider provider, Item item) {
        this.add(BuiltInRegistries.ITEM.getKey(item), provider, item);
    }

    @Deprecated(forRemoval = true)
    public void add(HolderLookup.RegistryLookup<Item> itemLookup, String id, ItemContentsProvider provider, Item... items) {
        this.add(id, provider, items);
    }

    @Deprecated(forRemoval = true)
    public void add(String id, ItemContentsProvider provider, Item... items) {
        this.add(Identifier.fromNamespaceAndPath(this.modId, id), provider, items);
    }

    @Deprecated(forRemoval = true)
    public void add(HolderLookup.RegistryLookup<Item> itemLookup, Identifier identifier, ItemContentsProvider provider, Item... items) {
        this.add(identifier, provider, items);
    }

    public final void add(Identifier identifier, ItemContentsProvider provider, Item... items) {
        this.add(identifier, provider, HolderSet.direct(BuiltInRegistries.ITEM::wrapAsHolder, items));
    }

    @SafeVarargs
    public final void add(Identifier identifier, ItemContentsProvider provider, Holder<Item>... items) {
        this.add(identifier, provider, HolderSet.direct(items));
    }

    public void add(Identifier identifier, ItemContentsProvider provider, HolderSet<Item> holderSet) {
        this.providers.put(identifier, Map.entry(holderSet, provider));
    }

    @Override
    public String getName() {
        return "Item Contents Provider";
    }
}
