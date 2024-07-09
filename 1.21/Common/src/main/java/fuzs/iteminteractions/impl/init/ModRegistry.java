package fuzs.iteminteractions.impl.init;

import fuzs.iteminteractions.api.v1.provider.ItemContentsProvider;
import fuzs.iteminteractions.api.v1.provider.impl.BundleProvider;
import fuzs.iteminteractions.api.v1.provider.impl.ContainerProvider;
import fuzs.iteminteractions.api.v1.provider.impl.EmptyProvider;
import fuzs.iteminteractions.api.v1.provider.impl.EnderChestProvider;
import fuzs.iteminteractions.impl.ItemInteractions;
import fuzs.iteminteractions.impl.capability.ContainerClientInputCapability;
import fuzs.puzzleslib.api.capability.v3.CapabilityController;
import fuzs.puzzleslib.api.capability.v3.data.CopyStrategy;
import fuzs.puzzleslib.api.capability.v3.data.EntityCapabilityKey;
import fuzs.puzzleslib.api.init.v3.registry.RegistryManager;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.player.Player;

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

    static final CapabilityController CAPABILITIES = CapabilityController.from(ItemInteractions.MOD_ID);
    public static final EntityCapabilityKey<Player, ContainerClientInputCapability> CONTAINER_SLOT_CAPABILITY = CAPABILITIES.registerEntityCapability(
            "container_client_input",
            ContainerClientInputCapability.class,
            ContainerClientInputCapability::new,
            Player.class
    ).setCopyStrategy(CopyStrategy.ALWAYS);

    public static void touch() {
        // NO-OP
    }
}
