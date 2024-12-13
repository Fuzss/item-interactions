package fuzs.iteminteractions.impl.world.item.container;

import com.google.common.collect.ImmutableMap;
import fuzs.iteminteractions.api.v1.provider.ItemContentsBehavior;
import fuzs.iteminteractions.api.v1.provider.ItemContentsProvider;
import fuzs.iteminteractions.impl.ItemInteractions;
import fuzs.iteminteractions.impl.network.S2CSyncItemContentsProviders;
import fuzs.puzzleslib.api.network.v3.PlayerSet;
import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.function.BiConsumer;

public final class ItemContentsProviders extends UnconditionalSimpleJsonResourceReloadListener<Map.Entry<HolderSet<Item>, ItemContentsProvider>> {
    public static final ResourceLocation ITEM_CONTAINER_PROVIDER_LOCATION = ItemInteractions.id("item_contents_provider");

    @Nullable
    private static Map<HolderSet<Item>, ItemContentsProvider> unresolvedProviders;
    private static Map<Item, ItemContentsProvider> resolvedProviders = ImmutableMap.of();

    public ItemContentsProviders(HolderLookup.Provider registries) {
        super(registries, ItemContentsProvider.WITH_ITEMS_CODEC, ITEM_CONTAINER_PROVIDER_LOCATION.getPath());
    }

    @Override
    public void apply(Map<ResourceLocation, Map.Entry<HolderSet<Item>, ItemContentsProvider>> map, ResourceManager resourceManager, ProfilerFiller profiler) {
        unresolvedProviders = map.values().stream().collect(Util.toMap());
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

    public static void onAddDataPackReloadListeners(RegistryAccess fullRegistries, HolderLookup.Provider lookupWithUpdatedTags, BiConsumer<ResourceLocation, PreparableReloadListener> consumer) {
        consumer.accept(ITEM_CONTAINER_PROVIDER_LOCATION, new ItemContentsProviders(lookupWithUpdatedTags));
    }

    public static void onTagsUpdated(HolderLookup.Provider registries, boolean client) {
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
        ItemInteractions.NETWORK.sendMessage(PlayerSet.ofPlayer(serverPlayer),
                new S2CSyncItemContentsProviders(resolvedProviders).toClientboundMessage());
    }
}
