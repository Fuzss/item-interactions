package fuzs.iteminteractions.impl.world.item.container;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import fuzs.iteminteractions.api.v1.provider.ItemContentsBehavior;
import fuzs.iteminteractions.api.v1.provider.ItemContentsProvider;
import fuzs.iteminteractions.impl.ItemInteractions;
import fuzs.iteminteractions.impl.network.S2CSyncItemContentsProviders;
import fuzs.puzzleslib.api.config.v3.json.JsonConfigFileUtil;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.Map;

public final class ItemContentsProviders extends SimpleJsonResourceReloadListener {
    public static final ResourceLocation ITEM_CONTAINER_PROVIDER_LOCATION = ItemInteractions.id("item_contents_provider");
    private static Map<Item, ItemContentsProvider> providers = ImmutableMap.of();

    private final HolderLookup.Provider registries;

    public ItemContentsProviders(HolderLookup.Provider registries) {
        super(JsonConfigFileUtil.GSON, ITEM_CONTAINER_PROVIDER_LOCATION.getPath());
        this.registries = registries;
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

    public static void onSyncDataPackContents(ServerPlayer player, boolean joined) {
        ItemInteractions.NETWORK.sendTo(player, new S2CSyncItemContentsProviders(providers).toClientboundMessage());
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> map, ResourceManager resourceManager, ProfilerFiller profiler) {
        ImmutableMap.Builder<Item, ItemContentsProvider> builder = ImmutableMap.builder();
        for (Map.Entry<ResourceLocation, JsonElement> entry : map.entrySet()) {
            // modded items may not be present, but we register default providers for some
            if (BuiltInRegistries.ITEM.containsKey(entry.getKey())) {
                JsonObject jsonObject = entry.getValue().getAsJsonObject();
                ItemContentsProvider.CODEC.parse(this.registries.createSerializationContext(JsonOps.INSTANCE),
                                jsonObject
                        )
                        .resultOrPartial(string -> ItemInteractions.LOGGER.error(
                                "Failed to parse item container provider: {}",
                                string
                        ))
                        .ifPresent(provider -> builder.put(BuiltInRegistries.ITEM.get(entry.getKey()),
                                provider
                        ));
            }
        }
        setItemContainerProviders(builder.build());
    }
}
