package fuzs.iteminteractions.impl.world.item.container;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import fuzs.iteminteractions.api.v1.provider.ItemContentsBehavior;
import fuzs.iteminteractions.api.v1.provider.ItemContentsProvider;
import fuzs.iteminteractions.impl.ItemInteractions;
import fuzs.iteminteractions.impl.network.S2CSyncItemContentsProviders;
import fuzs.puzzleslib.api.config.v3.json.JsonConfigFileUtil;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.function.BiConsumer;

public final class ItemContentsProviders extends SimpleJsonResourceReloadListener {
    public static final ResourceLocation ITEM_CONTAINER_PROVIDER_LOCATION = ItemInteractions.id("item_contents_provider");
    private static Map<Item, ItemContentsProvider> providers = ImmutableMap.of();

    private final HolderLookup.Provider registries;

    public ItemContentsProviders(HolderLookup.Provider registries) {
        super(JsonConfigFileUtil.GSON, ITEM_CONTAINER_PROVIDER_LOCATION.getPath());
        this.registries = registries;
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> map, ResourceManager resourceManager, ProfilerFiller profiler) {
        Map<Item, ItemContentsProvider> providers = new IdentityHashMap<>();
        for (JsonElement jsonElement : map.values()) {
            ItemContentsProvider.WITH_ITEMS_CODEC.parse(this.registries.createSerializationContext(JsonOps.INSTANCE),
                            jsonElement
                    )
                    .resultOrPartial(string -> ItemInteractions.LOGGER.error(
                            "Failed to parse item container provider: {}",
                            string
                    ))
                    .ifPresent((Map.Entry<HolderSet<Item>, ItemContentsProvider> entry) -> {
                        entry.getKey().forEach((Holder<Item> holder) -> {
                            // multiple entries can define a provider for the same item, in that case just let the first one win
                            providers.putIfAbsent(holder.value(), entry.getValue());
                        });
                    });
        }
        setItemContainerProviders(providers);
    }

    public static ItemContentsBehavior get(ItemStack itemStack) {
        if (itemStack.isEmpty()) {
            return ItemContentsBehavior.empty();
        } else {
            return ItemContentsBehavior.ofNullable(providers.get(itemStack.getItem()));
        }
    }

    public static void setItemContainerProviders(Map<Item, ItemContentsProvider> providers) {
        ItemContentsProviders.providers = ImmutableMap.copyOf(providers);
    }

    public static void onAddDataPackReloadListeners(HolderLookup.Provider registries, BiConsumer<ResourceLocation, PreparableReloadListener> consumer) {
        consumer.accept(ITEM_CONTAINER_PROVIDER_LOCATION, new ItemContentsProviders(registries));
    }

    public static void onSyncDataPackContents(ServerPlayer player, boolean joined) {
        ItemInteractions.NETWORK.sendTo(player, new S2CSyncItemContentsProviders(providers).toClientboundMessage());
    }
}
