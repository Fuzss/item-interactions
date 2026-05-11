package fuzs.iteminteractions.common.impl.init;

import com.mojang.serialization.MapCodec;
import fuzs.iteminteractions.common.api.v1.world.item.storage.ItemStorage;
import fuzs.iteminteractions.common.api.v1.world.item.storage.BundleContentsStorage;
import fuzs.iteminteractions.common.api.v1.world.item.storage.ContainerStorage;
import fuzs.iteminteractions.common.api.v1.world.item.storage.VoidStorage;
import fuzs.iteminteractions.common.api.v1.world.item.storage.EnderChestStorage;
import fuzs.iteminteractions.common.impl.ItemInteractions;
import fuzs.puzzleslib.common.api.attachment.v4.DataAttachmentRegistry;
import fuzs.puzzleslib.common.api.attachment.v4.DataAttachmentType;
import fuzs.puzzleslib.common.api.init.v3.registry.RegistryManager;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;

public class ModRegistry {
    static final RegistryManager REGISTRIES = RegistryManager.from(ItemInteractions.MOD_ID);
    public static final Holder.Reference<DataComponentType<Integer>> SELECTED_ITEM_DATA_COMPONENT_TYPE = REGISTRIES.registerDataComponentType(
            "selected_item",
            (DataComponentType.Builder<Integer> builder) -> builder.persistent(MapCodec.unitCodec(-1))
                    .networkSynchronized(ByteBufCodecs.VAR_INT));
    public static final Holder.Reference<ItemStorage.Type<?>> EMPTY_ITEM_CONTENTS_PROVIDER_TYPE = REGISTRIES.register(
            ItemStorage.REGISTRY_KEY,
            "empty",
            () -> new ItemStorage.Type<>(VoidStorage.CODEC));
    public static final Holder.Reference<ItemStorage.Type<?>> CONTAINER_ITEM_CONTENTS_PROVIDER_TYPE = REGISTRIES.register(
            ItemStorage.REGISTRY_KEY,
            "container",
            () -> new ItemStorage.Type<>(ContainerStorage.CODEC));
    public static final Holder.Reference<ItemStorage.Type<?>> ENDER_CHEST_ITEM_CONTENTS_PROVIDER_TYPE = REGISTRIES.register(
            ItemStorage.REGISTRY_KEY,
            "ender_chest",
            () -> new ItemStorage.Type<>(EnderChestStorage.CODEC));
    public static final Holder.Reference<ItemStorage.Type<?>> BUNDLE_ITEM_CONTENTS_PROVIDER_TYPE = REGISTRIES.register(
            ItemStorage.REGISTRY_KEY,
            "bundle",
            () -> new ItemStorage.Type<>(BundleContentsStorage.CODEC));

    public static final DataAttachmentType<Entity, Boolean> MOVE_SINGLE_ITEM_ATTACHMENT_TYPE = DataAttachmentRegistry.<Boolean>entityBuilder()
            .defaultValue(EntityType.PLAYER, Boolean.FALSE)
            .build(ItemInteractions.id("move_single_item"));

    public static void bootstrap() {
        // NO-OP
    }
}
