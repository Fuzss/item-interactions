package fuzs.iteminteractions.common.impl;

import fuzs.iteminteractions.common.api.v1.world.item.storage.ItemStorage;
import fuzs.iteminteractions.common.impl.config.ClientConfig;
import fuzs.iteminteractions.common.impl.config.ServerConfig;
import fuzs.iteminteractions.common.impl.data.DynamicItemContentsProvider;
import fuzs.iteminteractions.common.impl.handler.EnderChestSyncHandler;
import fuzs.iteminteractions.common.impl.init.ModRegistry;
import fuzs.iteminteractions.common.impl.network.ClientboundEnderChestContentMessage;
import fuzs.iteminteractions.common.impl.network.ClientboundEnderChestSlotMessage;
import fuzs.iteminteractions.common.impl.network.ClientboundSyncItemContentsProviders;
import fuzs.iteminteractions.common.impl.network.client.ServerboundContainerClientInputMessage;
import fuzs.iteminteractions.common.impl.network.client.ServerboundEnderChestContentMessage;
import fuzs.iteminteractions.common.impl.network.client.ServerboundSelectedItemMessage;
import fuzs.iteminteractions.common.impl.world.item.container.ItemContentsProviders;
import fuzs.puzzleslib.common.api.config.v3.ConfigHolder;
import fuzs.puzzleslib.common.api.core.v1.ModConstructor;
import fuzs.puzzleslib.common.api.core.v1.ModLoaderEnvironment;
import fuzs.puzzleslib.common.api.core.v1.context.*;
import fuzs.puzzleslib.common.api.event.v1.entity.player.AfterChangeDimensionCallback;
import fuzs.puzzleslib.common.api.event.v1.entity.player.ContainerEvents;
import fuzs.puzzleslib.common.api.event.v1.entity.player.PlayerCopyEvents;
import fuzs.puzzleslib.common.api.event.v1.entity.player.PlayerNetworkEvents;
import fuzs.puzzleslib.common.api.event.v1.server.SyncDataPackContentsCallback;
import fuzs.puzzleslib.common.api.event.v1.server.TagsUpdatedCallback;
import fuzs.puzzleslib.common.api.resources.v1.DynamicPackResources;
import fuzs.puzzleslib.common.api.resources.v1.PackResourcesHelper;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.server.ReloadableServerResources;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.TooltipDisplay;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ItemInteractions implements ModConstructor {
    public static final String MOD_ID = "iteminteractions";
    public static final String MOD_NAME = "Item Interactions";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_NAME);

    public static final ConfigHolder CONFIG = ConfigHolder.builder(MOD_ID)
            .client(ClientConfig.class)
            .server(ServerConfig.class);

    @Override
    public void onConstructMod() {
        ModRegistry.bootstrap();
        registerEventHandlers();
    }

    private static void registerEventHandlers() {
        ContainerEvents.OPEN.register(EnderChestSyncHandler::onContainerOpen);
        SyncDataPackContentsCallback.EVENT.register(ItemContentsProviders::onSyncDataPackContents);
        PlayerNetworkEvents.JOIN.register(EnderChestSyncHandler::onPlayerJoin);
        AfterChangeDimensionCallback.EVENT.register(EnderChestSyncHandler::onAfterChangeDimension);
        PlayerCopyEvents.RESPAWN.register(EnderChestSyncHandler::onRespawn);
        TagsUpdatedCallback.EVENT.register(ItemContentsProviders::onTagsUpdated);
    }

    @Override
    public void onRegisterPayloadTypes(PayloadTypesContext context) {
        context.playToServer(ServerboundContainerClientInputMessage.class,
                ServerboundContainerClientInputMessage.STREAM_CODEC);
        context.playToServer(ServerboundSelectedItemMessage.class, ServerboundSelectedItemMessage.STREAM_CODEC);
        context.playToClient(ClientboundEnderChestContentMessage.class,
                ClientboundEnderChestContentMessage.STREAM_CODEC);
        context.playToClient(ClientboundEnderChestSlotMessage.class, ClientboundEnderChestSlotMessage.STREAM_CODEC);
        context.playToServer(ServerboundEnderChestContentMessage.class,
                ServerboundEnderChestContentMessage.STREAM_CODEC);
        context.playToClient(ClientboundSyncItemContentsProviders.class,
                ClientboundSyncItemContentsProviders.STREAM_CODEC);
    }

    @Override
    public void onRegisterGameRegistries(GameRegistriesContext context) {
        context.registerRegistry(ItemStorage.REGISTRY);
    }

    @Override
    public void onAddDataPackFinders(PackRepositorySourcesContext context) {
        if (!ModLoaderEnvironment.INSTANCE.isDevelopmentEnvironment(MOD_ID)) {
            return;
        }

        context.registerRepositorySource(PackResourcesHelper.buildServerPack(id("test_item_interactions"),
                DynamicPackResources.create(DynamicItemContentsProvider::new),
                false));
    }

    @Override
    public void onAddDataPackReloadListeners(DataPackReloadListenersContext context) {
        context.registerReloadListener(ItemContentsProviders.REGISTRY_KEY.identifier(),
                (DataPackReloadListenersContext.PreparableReloadListenerFactory) (ReloadableServerResources serverResources, HolderLookup.Provider lookupWithUpdatedTags) -> {
                    return new ItemContentsProviders(lookupWithUpdatedTags);
                });
    }

    @Override
    public void onRegisterItemComponentPatches(ItemComponentsContext context) {
        if (!ModLoaderEnvironment.INSTANCE.isDevelopmentEnvironment(MOD_ID)) {
            return;
        }

        context.registerItemComponentsPatch((DataComponentGetter components, DataComponentMap.Builder builder, HolderLookup.Provider registries, Item item) -> {
            if (components.get(DataComponents.CONTAINER) != null) {
                builder.set(DataComponents.TOOLTIP_DISPLAY,
                        TooltipDisplay.DEFAULT.withHidden(DataComponents.CONTAINER, true));
            }
        });
    }

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }
}
