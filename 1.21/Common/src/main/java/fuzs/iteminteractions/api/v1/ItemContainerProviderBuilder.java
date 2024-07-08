package fuzs.iteminteractions.api.v1;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import fuzs.iteminteractions.api.v1.provider.*;
import fuzs.puzzleslib.api.config.v3.json.GsonEnumHelper;
import fuzs.puzzleslib.api.core.v1.utility.ResourceLocationHelper;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.entity.BlockEntityType;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;

public class ItemContainerProviderBuilder {
    public int inventoryWidth;
    public int inventoryHeight;
    @Nullable
    public DyeColor dyeColor;
    public boolean filterContainerItems;
    public BlockEntityType<?> blockEntityType;
    public int capacity;
    public HolderSet<Item> disallowedItems = HolderSet.empty();
    public boolean anyGameMode;
    @Nullable
    public EquipmentSlot equipmentSlot;

    public ItemContainerProviderBuilder(JsonElement jsonElement, HolderLookup.Provider registries) {
        this.fromJson(jsonElement, registries);
    }

    public static BiFunction<JsonElement, HolderLookup.Provider, ItemContainerProvider> fromJson(Function<ItemContainerProviderBuilder, ItemContainerProvider> factory) {
        return (JsonElement jsonElement, HolderLookup.Provider registries) -> {
            ItemContainerProviderBuilder builder = new ItemContainerProviderBuilder(jsonElement, registries);
            ItemContainerProvider provider = factory.apply(builder);
            if (provider instanceof SimpleItemContainerProvider itemProvider && builder.filterContainerItems) {
                itemProvider.filterContainerItems();
            }
            if (provider instanceof AbstractItemContainerProvider nestedTagProvider) {
                nestedTagProvider.disallowedItems(builder.disallowedItems);
            }
            return provider;
        };
    }

    private void fromJson(JsonElement jsonElement, HolderLookup.Provider registries) {
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        this.inventoryWidth = GsonHelper.getAsInt(jsonObject, "inventory_width", -1);
        this.inventoryHeight = GsonHelper.getAsInt(jsonObject, "inventory_height", -1);
        this.dyeColor = DyeColor.byName(GsonHelper.getAsString(jsonObject, "background_color", ""), null);
        this.filterContainerItems = GsonHelper.getAsBoolean(jsonObject, "filter_container_items", false);
        if (jsonObject.has("block_entity_type")) {
            ResourceLocation blockEntityTypeKey = ResourceLocationHelper.parse(GsonHelper.getAsString(jsonObject,
                    "block_entity_type"
            ));
            this.blockEntityType = BuiltInRegistries.BLOCK_ENTITY_TYPE.get(blockEntityTypeKey);
        }
        this.capacity = GsonHelper.getAsInt(jsonObject, "capacity_multiplier", -1);
        RegistryCodecs.homogeneousList(Registries.ITEM)
                .optionalFieldOf("disallowed_items")
                .decoder()
                .decode(registries.createSerializationContext(JsonOps.INSTANCE), jsonObject)
                .getOrThrow()
                .getFirst()
                .ifPresent((HolderSet<Item> holderSet) -> {
                    this.disallowedItems = holderSet;
                });
        this.anyGameMode = GsonHelper.getAsBoolean(jsonObject, "any_game_mode", false);
        this.equipmentSlot = GsonEnumHelper.getAsEnum(jsonObject, "equipment_slot", EquipmentSlot.class, null);
    }

    public ItemContainerProvider toSimpleItemContainerProvider() {
        this.checkInventorySize("item");
        return new SimpleItemContainerProvider(this.inventoryWidth,
                this.inventoryHeight,
                this.dyeColor
        ).equipmentSlot(this.equipmentSlot);
    }

    public ItemContainerProvider toBlockEntityProvider() {
        this.checkInventorySize("block_entity");
        Objects.requireNonNull(this.blockEntityType, getErrorMessage("block_entity_type", "block_entity"));
        BlockEntityProvider provider = new BlockEntityProvider(this.blockEntityType,
                this.inventoryWidth,
                this.inventoryHeight,
                this.dyeColor
        );
        if (this.anyGameMode) provider.anyGameMode();
        return provider.equipmentSlot(this.equipmentSlot);
    }

    public ItemContainerProvider toBlockEntityViewProvider() {
        this.checkInventorySize("block_entity_view");
        Objects.requireNonNull(this.blockEntityType, getErrorMessage("block_entity_type", "block_entity_view"));
        return new BlockEntityViewProvider(this.blockEntityType,
                this.inventoryWidth,
                this.inventoryHeight,
                this.dyeColor
        ).equipmentSlot(this.equipmentSlot);
    }

    public ItemContainerProvider toBundleProvider() {
        if (this.capacity == -1) {
            throw new IllegalStateException(getErrorMessage("capacity_multiplier", "bundle"));
        }
        return new BundleProvider(this.capacity, this.dyeColor);
    }

    public ItemContainerProvider toEnderChestProvider() {
        return new EnderChestProvider();
    }

    public void checkInventorySize(String type) {
        if (this.inventoryWidth == -1) throw new IllegalStateException(getErrorMessage("inventory_width", type));
        if (this.inventoryHeight == -1) throw new IllegalStateException(getErrorMessage("inventory_height", type));
    }

    public static String getErrorMessage(String jsonKey, String providerType) {
        return "'%s' not set for provider of type '%s'".formatted(jsonKey, providerType);
    }
}
