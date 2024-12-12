package fuzs.iteminteractions.impl.init;

import fuzs.iteminteractions.api.v1.provider.ItemContentsProvider;
import fuzs.iteminteractions.api.v1.provider.impl.BundleProvider;
import fuzs.iteminteractions.api.v1.provider.impl.ContainerProvider;
import fuzs.iteminteractions.api.v1.provider.impl.EmptyProvider;
import fuzs.iteminteractions.api.v1.provider.impl.EnderChestProvider;
import fuzs.iteminteractions.impl.ItemInteractions;
import fuzs.puzzleslib.api.attachment.v4.DataAttachmentRegistry;
import fuzs.puzzleslib.api.attachment.v4.DataAttachmentType;
import fuzs.puzzleslib.api.init.v3.registry.RegistryManager;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;

public class ModRegistry {
    static final RegistryManager REGISTRIES = RegistryManager.from(ItemInteractions.MOD_ID);
    public static final Holder.Reference<ItemContentsProvider.Type> EMPTY_ITEM_CONTENTS_PROVIDER_TYPE = REGISTRIES.register(
            ItemContentsProvider.REGISTRY_KEY,
            "empty",
            () -> new ItemContentsProvider.Type(EmptyProvider.CODEC)
    );
    public static final Holder.Reference<ItemContentsProvider.Type> CONTAINER_ITEM_CONTENTS_PROVIDER_TYPE = REGISTRIES.register(
            ItemContentsProvider.REGISTRY_KEY,
            "container",
            () -> new ItemContentsProvider.Type(ContainerProvider.CODEC)
    );
    public static final Holder.Reference<ItemContentsProvider.Type> ENDER_CHEST_ITEM_CONTENTS_PROVIDER_TYPE = REGISTRIES.register(
            ItemContentsProvider.REGISTRY_KEY,
            "ender_chest",
            () -> new ItemContentsProvider.Type(EnderChestProvider.CODEC)
    );
    public static final Holder.Reference<ItemContentsProvider.Type> BUNDLE_ITEM_CONTENTS_PROVIDER_TYPE = REGISTRIES.register(
            ItemContentsProvider.REGISTRY_KEY,
            "bundle",
            () -> new ItemContentsProvider.Type(BundleProvider.CODEC)
    );

    public static final DataAttachmentType<Entity, Integer> CURRENT_CONTAINER_SLOT_ATTACHMENT_TYPE = DataAttachmentRegistry.<Integer>entityBuilder().defaultValue(
            EntityType.PLAYER, -1).build(ItemInteractions.id("current_container_slot"));
    public static final DataAttachmentType<Entity, Boolean> SINGLE_ITEM_MODIFIER_ATTACHMENT_TYPE = DataAttachmentRegistry.<Boolean>entityBuilder().defaultValue(
            EntityType.PLAYER, false).build(ItemInteractions.id("single_item_modifier"));

    public static void touch() {
        // NO-OP
    }
}
