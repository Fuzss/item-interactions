package fuzs.iteminteractions.api.v1.data;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.JsonElement;
import fuzs.iteminteractions.api.v1.ItemContainerProviderSerializers;
import fuzs.iteminteractions.api.v1.provider.ItemContainerProvider;
import fuzs.iteminteractions.impl.world.item.container.ItemContainerProviders;
import fuzs.puzzleslib.api.data.v2.core.DataProviderContext;
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

public abstract class AbstractItemContainerProvider implements DataProvider {
    private final Map<ResourceLocation, ItemContainerProvider> providers = Maps.newHashMap();
    private final PackOutput.PathProvider pathProvider;

    public AbstractItemContainerProvider(DataProviderContext context) {
        this(context.getPackOutput());
    }

    public AbstractItemContainerProvider(PackOutput packOutput) {
        this.pathProvider = packOutput.createPathProvider(PackOutput.Target.DATA_PACK, ItemContainerProviders.ITEM_CONTAINER_PROVIDERS_KEY);
    }

    @Override
    public final CompletableFuture<?> run(CachedOutput output) {
        this.addItemProviders();
        List<CompletableFuture<?>> futures = Lists.newArrayList();
        for (Map.Entry<ResourceLocation, ItemContainerProvider> entry : this.providers.entrySet()) {
            JsonElement jsonElement = ItemContainerProviderSerializers.serialize(entry.getValue());
            Path path = this.pathProvider.json(entry.getKey());
            futures.add(DataProvider.saveStable(output, jsonElement, path));
        }
        return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
    }

    public abstract void addItemProviders();

    public void add(ItemLike item, ItemContainerProvider provider) {
        this.add(BuiltInRegistries.ITEM.getKey(item.asItem()), provider);
    }

    public void add(ResourceLocation resourceLocation, ItemContainerProvider provider) {
        this.providers.put(resourceLocation, provider);
    }

    @Override
    public String getName() {
        return "Item Container Provider";
    }
}
