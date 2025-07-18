package fuzs.iteminteractions.impl;

import fuzs.iteminteractions.api.v1.provider.ItemContentsProvider;
import fuzs.iteminteractions.impl.config.ClientConfig;
import fuzs.iteminteractions.impl.config.ServerConfig;
import fuzs.iteminteractions.impl.data.DynamicItemContentsProvider;
import fuzs.iteminteractions.impl.handler.EnderChestSyncHandler;
import fuzs.iteminteractions.impl.init.ModRegistry;
import fuzs.iteminteractions.impl.network.ClientboundEnderChestContentMessage;
import fuzs.iteminteractions.impl.network.ClientboundEnderChestSlotMessage;
import fuzs.iteminteractions.impl.network.ClientboundSyncItemContentsProviders;
import fuzs.iteminteractions.impl.network.client.ServerboundContainerClientInputMessage;
import fuzs.iteminteractions.impl.network.client.ServerboundEnderChestContentMessage;
import fuzs.iteminteractions.impl.world.item.container.ItemContentsProviders;
import fuzs.puzzleslib.api.config.v3.ConfigHolder;
import fuzs.puzzleslib.api.core.v1.ModConstructor;
import fuzs.puzzleslib.api.core.v1.ModLoaderEnvironment;
import fuzs.puzzleslib.api.core.v1.context.GameRegistriesContext;
import fuzs.puzzleslib.api.core.v1.context.PackRepositorySourcesContext;
import fuzs.puzzleslib.api.core.v1.context.PayloadTypesContext;
import fuzs.puzzleslib.api.core.v1.utility.ResourceLocationHelper;
import fuzs.puzzleslib.api.event.v1.entity.player.AfterChangeDimensionCallback;
import fuzs.puzzleslib.api.event.v1.entity.player.ContainerEvents;
import fuzs.puzzleslib.api.event.v1.entity.player.PlayerCopyEvents;
import fuzs.puzzleslib.api.event.v1.entity.player.PlayerNetworkEvents;
import fuzs.puzzleslib.api.event.v1.server.AddDataPackReloadListenersCallback;
import fuzs.puzzleslib.api.event.v1.server.SyncDataPackContentsCallback;
import fuzs.puzzleslib.api.event.v1.server.TagsUpdatedCallback;
import fuzs.puzzleslib.api.resources.v1.DynamicPackResources;
import fuzs.puzzleslib.api.resources.v1.PackResourcesHelper;
import net.minecraft.resources.ResourceLocation;
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
        PlayerNetworkEvents.LOGGED_IN.register(EnderChestSyncHandler::onLoggedIn);
        AfterChangeDimensionCallback.EVENT.register(EnderChestSyncHandler::onAfterChangeDimension);
        PlayerCopyEvents.RESPAWN.register(EnderChestSyncHandler::onRespawn);
        AddDataPackReloadListenersCallback.EVENT.register(ItemContentsProviders::onAddDataPackReloadListeners);
        TagsUpdatedCallback.EVENT.register(ItemContentsProviders::onTagsUpdated);
    }

    @Override
    public void onRegisterPayloadTypes(PayloadTypesContext context) {
        context.playToServer(ServerboundContainerClientInputMessage.class,
                ServerboundContainerClientInputMessage.STREAM_CODEC);
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
        context.registerRegistry(ItemContentsProvider.REGISTRY);
    }

    @Override
    public void onAddDataPackFinders(PackRepositorySourcesContext context) {
        if (ModLoaderEnvironment.INSTANCE.isDevelopmentEnvironment(MOD_ID)) {
            context.addRepositorySource(PackResourcesHelper.buildServerPack(id("test_item_interactions"),
                    DynamicPackResources.create(DynamicItemContentsProvider::new),
                    false));
        }
    }

    public static ResourceLocation id(String path) {
        return ResourceLocationHelper.fromNamespaceAndPath(MOD_ID, path);
    }
}
