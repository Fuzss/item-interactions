package fuzs.iteminteractions.impl.world.item.container;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import fuzs.iteminteractions.api.v1.provider.ItemContentsBehavior;
import fuzs.iteminteractions.api.v1.provider.ItemContentsProvider;
import fuzs.iteminteractions.impl.ItemInteractions;
import fuzs.iteminteractions.impl.network.S2CSyncItemContentsProviders;
import fuzs.puzzleslib.api.config.v3.json.GsonCodecHelper;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

public final class ItemContentsProviders extends SimpleJsonResourceReloadListener {
    public static final ResourceLocation ITEM_CONTAINER_PROVIDER_LOCATION = ItemInteractions.id("item_contents_provider");

    @Nullable
    private static Map<HolderSet<Item>, ItemContentsProvider> unresolvedProviders;
    private static Map<Item, ItemContentsProvider> resolvedProviders = ImmutableMap.of();
    private final HolderLookup.Provider registries;

    public ItemContentsProviders(HolderLookup.Provider registries) {
        super(GsonCodecHelper.GSON, ITEM_CONTAINER_PROVIDER_LOCATION.getPath());
        this.registries = registries;
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> map, ResourceManager resourceManager, ProfilerFiller profiler) {
        Map<HolderSet<Item>, ItemContentsProvider> providers = new IdentityHashMap<>();
        for (JsonElement jsonElement : map.values()) {
            ItemContentsProvider.WITH_ITEMS_CODEC.parse(this.registries.createSerializationContext(JsonOps.INSTANCE),
                            jsonElement)
                    .resultOrPartial(string -> ItemInteractions.LOGGER.error(
                            "Failed to parse item container provider: {}",
                            string))
                    .ifPresent((Map.Entry<HolderSet<Item>, ItemContentsProvider> entry) -> {
                        providers.put(entry.getKey(), entry.getValue());
                    });
        }
        unresolvedProviders = providers;
        resolvedProviders = ImmutableMap.of();
    }

    public static ItemContentsBehavior get(ItemStack itemStack) {
        if (itemStack.isEmpty()) {
            return ItemContentsBehavior.empty();
        } else {
            return ItemContentsBehavior.ofNullable(resolvedProviders.get(itemStack.getItem()));
        }
    }

    public static void setItemContainerProviders(Map<Item, ItemContentsProvider> providers) {
        ItemContentsProviders.resolvedProviders = ImmutableMap.copyOf(providers);
    }

    public static void onAddDataPackReloadListeners(BiConsumer<ResourceLocation, BiFunction<HolderLookup.Provider, RegistryAccess, PreparableReloadListener>> consumer) {
        consumer.accept(ITEM_CONTAINER_PROVIDER_LOCATION,
                (HolderLookup.Provider registries, RegistryAccess registryAccess) -> {
                    return new ItemContentsProviders(registries);
                });
    }

    public static void onTagsUpdated(RegistryAccess registryAccess, boolean client) {
        Map<HolderSet<Item>, ItemContentsProvider> map = unresolvedProviders;
        if (map != null && !client) {
            Map<Item, ItemContentsProvider> providers = new IdentityHashMap<>();
            for (Map.Entry<HolderSet<Item>, ItemContentsProvider> entry : map.entrySet()) {
                entry.getKey().forEach((Holder<Item> holder) -> {
                    // multiple entries can define a provider for the same item, in that case just let the first one win
                    providers.putIfAbsent(holder.value(), entry.getValue());
                });
            }
            unresolvedProviders = null;
            setItemContainerProviders(providers);
        }
    }

    public static void onSyncDataPackContents(ServerPlayer serverPlayer, boolean joined) {
        if (!serverPlayer.connection.connection.isMemoryConnection()) {
            ItemInteractions.NETWORK.sendTo(serverPlayer,
                    new S2CSyncItemContentsProviders(resolvedProviders).toClientboundMessage());
        }
    }
}
