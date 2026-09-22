package fuzs.iteminteractions.common.impl.world.item.container;

import com.google.common.collect.ImmutableMap;
import fuzs.iteminteractions.common.api.v2.world.item.storage.ItemStorage;
import fuzs.iteminteractions.common.api.v2.world.item.storage.ItemStorageHolder;
import fuzs.iteminteractions.common.impl.network.ClientboundSyncItemStorage;
import fuzs.puzzleslib.common.api.network.v4.MessageSender;
import fuzs.puzzleslib.common.api.network.v4.PlayerSet;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.server.ReloadableServerResources;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.IdentityHashMap;
import java.util.Map;

public final class ItemStorageManager {
    private static Map<Item, ItemStorage> resolvedDefinitions = ImmutableMap.of();

    public static ItemStorageHolder getHolder(ItemStack itemStack) {
        return ItemStorageHolder.ofNullable(resolvedDefinitions.get(itemStack.getItem()));
    }

    public static void setItemStorageDefinitions(Map<Item, ItemStorage> definitions) {
        ItemStorageManager.resolvedDefinitions = ImmutableMap.copyOf(definitions);
    }

    /**
     * @see net.minecraft.world.item.crafting.RecipeMap#create(HolderLookup)
     */
    public static void onServerResourcesLoad(ReloadableServerResources serverResources, RegistryAccess registries) {
        Map<Item, ItemStorage> providers = new IdentityHashMap<>();
        registries.lookupOrThrow(ItemStorage.Definition.REGISTRY_KEY)
                .listElements()
                .forEach((Holder.Reference<ItemStorage.Definition> holder) -> {
                    ItemStorage.Definition definition = holder.value();
                    definition.items().forEach((Holder<Item> itemHolder) -> {
                        // multiple entries can define a provider for the same item, in that case just let the first one win
                        providers.putIfAbsent(itemHolder.value(), definition.storage());
                    });
                });
        setItemStorageDefinitions(providers);
    }

    public static void onSyncDataPackContents(ServerPlayer serverPlayer, boolean joined) {
        if (!serverPlayer.connection.connection.isMemoryConnection()) {
            MessageSender.broadcast(PlayerSet.ofPlayer(serverPlayer),
                    new ClientboundSyncItemStorage(resolvedDefinitions));
        }
    }
}
