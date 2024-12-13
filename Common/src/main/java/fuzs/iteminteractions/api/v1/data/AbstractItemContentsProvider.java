package fuzs.iteminteractions.api.v1.data;

import com.google.common.collect.Maps;
import fuzs.iteminteractions.api.v1.provider.ItemContentsProvider;
import fuzs.iteminteractions.impl.world.item.container.ItemContentsProviders;
import fuzs.puzzleslib.api.core.v1.utility.ResourceLocationHelper;
import fuzs.puzzleslib.api.data.v2.core.DataProviderContext;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public abstract class AbstractItemContentsProvider implements DataProvider {
    private final Map<ResourceLocation, Map.Entry<HolderSet<Item>, ItemContentsProvider>> providers = Maps.newHashMap();
    private final String modId;
    private final PackOutput.PathProvider pathProvider;
    private final CompletableFuture<HolderLookup.Provider> registries;

    public AbstractItemContentsProvider(DataProviderContext context) {
        this(context.getModId(), context.getPackOutput(), context.getRegistries());
    }

    public AbstractItemContentsProvider(String modId, PackOutput packOutput, CompletableFuture<HolderLookup.Provider> registries) {
        this.modId = modId;
        this.pathProvider = packOutput.createPathProvider(PackOutput.Target.DATA_PACK,
                ItemContentsProviders.ITEM_CONTAINER_PROVIDER_LOCATION.getPath());
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
        for (Map.Entry<ResourceLocation, Map.Entry<HolderSet<Item>, ItemContentsProvider>> entry : this.providers.entrySet()) {
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

    public void add(HolderLookup.RegistryLookup<Item> items, ItemContentsProvider provider, TagKey<Item> tagKey) {
        this.add(items, tagKey.location().getPath(), provider, tagKey);
    }

    public void add(HolderLookup.RegistryLookup<Item> items, String id, ItemContentsProvider provider, TagKey<Item> tagKey) {
        ResourceLocation resourceLocation = ResourceLocationHelper.fromNamespaceAndPath(this.modId, id);
        this.add(items, resourceLocation, provider, tagKey);
    }

    public void add(HolderLookup.RegistryLookup<Item> items, ResourceLocation resourceLocation, ItemContentsProvider provider, TagKey<Item> tagKey) {
        this.providers.put(resourceLocation, Map.entry(items.getOrThrow(tagKey), provider));
    }

    public void add(HolderLookup.RegistryLookup<Item> items, ItemContentsProvider provider, Item item) {
        this.add(items, BuiltInRegistries.ITEM.getKey(item).getPath(), provider, item);
    }

    public void add(HolderLookup.RegistryLookup<Item> items, String id, ItemContentsProvider provider, Item... item) {
        ResourceLocation resourceLocation = ResourceLocationHelper.fromNamespaceAndPath(this.modId, id);
        this.add(items, resourceLocation, provider, item);
    }

    public void add(HolderLookup.RegistryLookup<Item> items, ResourceLocation resourceLocation, ItemContentsProvider provider, Item... item) {
        this.providers.put(resourceLocation, Map.entry(HolderSet.direct(Item::builtInRegistryHolder, item), provider));
    }

    @Override
    public String getName() {
        return "Item Contents Provider";
    }
}
