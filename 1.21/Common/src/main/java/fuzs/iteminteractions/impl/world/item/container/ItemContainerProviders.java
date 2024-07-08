package fuzs.iteminteractions.impl.world.item.container;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import fuzs.iteminteractions.api.v1.ItemContainerProviderSerializers;
import fuzs.iteminteractions.api.v1.provider.ItemContainerBehavior;
import fuzs.iteminteractions.api.v1.provider.ItemContainerProvider;
import fuzs.iteminteractions.impl.ItemInteractions;
import fuzs.iteminteractions.impl.network.S2CSyncItemContainerProvider;
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

import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.Objects;

public class ItemContainerProviders extends SimpleJsonResourceReloadListener {
    public static final ResourceLocation ITEM_CONTAINER_PROVIDERS_ID = ItemInteractions.id("item_container_providers");
    public static final ItemContainerProviders INSTANCE = new ItemContainerProviders();

    private WeakReference<HolderLookup.Provider> registries = new WeakReference<>(null);
    private Map<ResourceLocation, JsonElement> rawProviders = ImmutableMap.of();
    private Map<Item, ItemContainerBehavior> providers = ImmutableMap.of();

    private ItemContainerProviders() {
        super(JsonConfigFileUtil.GSON, ITEM_CONTAINER_PROVIDERS_ID.getPath());
    }

    public ItemContainerBehavior get(ItemStack itemStack) {
        return itemStack.isEmpty() ? ItemContainerBehavior.empty() : this.get(itemStack.getItem());
    }

    public ItemContainerBehavior get(Item item) {
        return this.providers.getOrDefault(item, ItemContainerBehavior.empty());
    }

    public void injectRegistries(HolderLookup.Provider registries) {
        this.registries = new WeakReference<>(registries);
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> object, ResourceManager resourceManager, ProfilerFiller profiler) {
        this.rawProviders = ImmutableMap.copyOf(object);
        HolderLookup.Provider registries = this.registries.get();
        Objects.requireNonNull(registries, "registry access is null");
        this.buildProviders(object, registries);
    }

    public void buildProviders(Map<ResourceLocation, JsonElement> object, HolderLookup.Provider registries) {
        ImmutableMap.Builder<Item, ItemContainerBehavior> builder = ImmutableMap.builder();
        for (Map.Entry<ResourceLocation, JsonElement> entry : object.entrySet()) {
            ResourceLocation item = entry.getKey();
            try {
                JsonObject jsonObject = entry.getValue().getAsJsonObject();
                // modded items may not be present, but we register default providers for some
                if (!BuiltInRegistries.ITEM.containsKey(item)) continue;
                ItemContainerProvider provider = ItemContainerProviderSerializers.deserialize(jsonObject, registries);
                if (provider != null) {
                    builder.put(BuiltInRegistries.ITEM.get(item), new ItemContainerBehavior(provider));
                }
            } catch (Exception e) {
                ItemInteractions.LOGGER.error("Couldn't parse item container provider {}", item, e);
            }
        }
        this.providers = builder.build();
    }

    public void onSyncDataPackContents(ServerPlayer player, boolean joined) {
        ItemInteractions.NETWORK.sendTo(player, new S2CSyncItemContainerProvider(this.rawProviders).toClientboundMessage());
    }
}
