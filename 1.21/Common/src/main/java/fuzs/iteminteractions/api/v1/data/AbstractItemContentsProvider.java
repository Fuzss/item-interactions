package fuzs.iteminteractions.api.v1.data;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import fuzs.iteminteractions.api.v1.provider.ItemContentsProvider;
import fuzs.iteminteractions.impl.world.item.container.ItemContentsProviders;
import fuzs.puzzleslib.api.data.v2.core.DataProviderContext;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ItemLike;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public abstract class AbstractItemContentsProvider implements DataProvider {
    private final Map<ResourceLocation, ItemContentsProvider> providers = Maps.newHashMap();
    private final PackOutput.PathProvider pathProvider;
    private final CompletableFuture<HolderLookup.Provider> registries;

    public AbstractItemContentsProvider(DataProviderContext context) {
        this(context.getPackOutput(), context.getRegistries());
    }

    public AbstractItemContentsProvider(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> registries) {
        this.pathProvider = packOutput.createPathProvider(PackOutput.Target.DATA_PACK, ItemContentsProviders.ITEM_CONTAINER_PROVIDER_LOCATION.getPath());
        this.registries = registries;
    }

    @Override
    public CompletableFuture<?> run(CachedOutput output) {
        return this.registries.thenCompose((HolderLookup.Provider registries) -> {
            return this.run(output, registries);
        });
    }

    public CompletableFuture<?> run(CachedOutput output, HolderLookup.Provider registries) {
        this.addItemProviders();
        List<CompletableFuture<?>> futures = Lists.newArrayList();
        for (Map.Entry<ResourceLocation, ItemContentsProvider> entry : this.providers.entrySet()) {
            Path path = this.pathProvider.json(entry.getKey());
            futures.add(DataProvider.saveStable(output, registries, ItemContentsProvider.CODEC, entry.getValue(), path));
        }
        return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
    }

    public abstract void addItemProviders();

    public void add(ItemLike item, ItemContentsProvider provider) {
        this.add(BuiltInRegistries.ITEM.getKey(item.asItem()), provider);
    }

    public void add(ResourceLocation resourceLocation, ItemContentsProvider provider) {
        this.providers.put(resourceLocation, provider);
    }

    @Override
    public String getName() {
        return "Item Container Provider";
    }
}
