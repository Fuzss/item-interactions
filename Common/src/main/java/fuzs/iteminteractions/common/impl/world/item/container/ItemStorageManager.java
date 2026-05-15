package fuzs.iteminteractions.common.impl.world.item.container;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import fuzs.iteminteractions.common.api.v1.world.item.storage.ItemStorage;
import fuzs.iteminteractions.common.api.v1.world.item.storage.ItemStorageHolder;
import fuzs.iteminteractions.common.impl.ItemInteractions;
import fuzs.iteminteractions.common.impl.network.ClientboundSyncItemStorage;
import fuzs.puzzleslib.common.api.network.v4.MessageSender;
import fuzs.puzzleslib.common.api.network.v4.PlayerSet;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

public final class ItemStorageManager extends UnconditionalSimpleJsonResourceReloadListener<Map.Entry<HolderSet<Item>, ItemStorage>> {
    public static final ResourceKey<Registry<Map.Entry<HolderSet<Item>, ItemStorage>>> REGISTRY_KEY = ResourceKey.createRegistryKey(
            ItemInteractions.id("item_storage"));

    @Nullable
    private static List<Map.Entry<HolderSet<Item>, ItemStorage>> unresolvedProviders;
    private static Map<Item, ItemStorage> resolvedProviders = ImmutableMap.of();

    public ItemStorageManager(HolderLookup.Provider registries) {
        super(registries, ItemStorage.WITH_ITEMS_CODEC, REGISTRY_KEY);
    }

    @Override
    public void apply(Map<Identifier, Map.Entry<HolderSet<Item>, ItemStorage>> map, ResourceManager resourceManager, ProfilerFiller profiler) {
        unresolvedProviders = ImmutableList.copyOf(map.values());
        resolvedProviders = ImmutableMap.of();
    }

    public static ItemStorageHolder get(ItemStack itemStack) {
        return ItemStorageHolder.ofNullable(resolvedProviders.get(itemStack.getItem()));
    }

    public static void setItemStorage(Map<Item, ItemStorage> providers) {
        ItemStorageManager.resolvedProviders = ImmutableMap.copyOf(providers);
    }

    public static void onTagsUpdated(HolderLookup.Provider registries, boolean client) {
        List<Map.Entry<HolderSet<Item>, ItemStorage>> holderSets = unresolvedProviders;
        if (holderSets != null && !client) {
            Map<Item, ItemStorage> providers = new IdentityHashMap<>();
            for (Map.Entry<HolderSet<Item>, ItemStorage> entry : holderSets) {
                entry.getKey().forEach((Holder<Item> holder) -> {
                    // multiple entries can define a provider for the same item, in that case just let the first one win
                    providers.putIfAbsent(holder.value(), entry.getValue());
                });
            }

            unresolvedProviders = null;
            setItemStorage(providers);
        }
    }

    public static void onSyncDataPackContents(ServerPlayer serverPlayer, boolean joined) {
        if (!serverPlayer.connection.connection.isMemoryConnection()) {
            MessageSender.broadcast(PlayerSet.ofPlayer(serverPlayer),
                    new ClientboundSyncItemStorage(resolvedProviders));
        }
    }
}
