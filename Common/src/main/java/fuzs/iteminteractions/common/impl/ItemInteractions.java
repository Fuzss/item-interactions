package fuzs.iteminteractions.common.impl;

import fuzs.iteminteractions.common.api.v2.world.item.storage.ItemStorage;
import fuzs.iteminteractions.common.impl.config.ClientConfig;
import fuzs.iteminteractions.common.impl.config.ServerConfig;
import fuzs.iteminteractions.common.impl.handler.EnderChestSyncHandler;
import fuzs.iteminteractions.common.impl.init.ModRegistry;
import fuzs.iteminteractions.common.impl.network.ClientboundEnderChestContentMessage;
import fuzs.iteminteractions.common.impl.network.ClientboundEnderChestSlotMessage;
import fuzs.iteminteractions.common.impl.network.ClientboundSyncItemStorage;
import fuzs.iteminteractions.common.impl.network.client.ServerboundContainerClientInputMessage;
import fuzs.iteminteractions.common.impl.network.client.ServerboundEnderChestContentMessage;
import fuzs.iteminteractions.common.impl.network.client.ServerboundSelectedItemMessage;
import fuzs.iteminteractions.common.impl.world.item.container.ItemStorageManager;
import fuzs.puzzleslib.common.api.config.v3.ConfigHolder;
import fuzs.puzzleslib.common.api.core.v1.ModConstructor;
import fuzs.puzzleslib.common.api.core.v1.ModLoaderEnvironment;
import fuzs.puzzleslib.common.api.core.v1.context.DataPackRegistriesContext;
import fuzs.puzzleslib.common.api.core.v1.context.GameRegistriesContext;
import fuzs.puzzleslib.common.api.core.v1.context.PackRepositorySourcesContext;
import fuzs.puzzleslib.common.api.core.v1.context.PayloadTypesContext;
import fuzs.puzzleslib.common.api.event.v1.entity.player.AfterChangeDimensionCallback;
import fuzs.puzzleslib.common.api.event.v1.entity.player.ContainerEvents;
import fuzs.puzzleslib.common.api.event.v1.entity.player.PlayerCopyEvents;
import fuzs.puzzleslib.common.api.event.v1.entity.player.PlayerNetworkEvents;
import fuzs.puzzleslib.common.api.event.v1.server.ServerResourcesLoadCallback;
import fuzs.puzzleslib.common.api.event.v1.server.SyncDataPackContentsCallback;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ItemInteractions implements ModConstructor {
    public static final String MOD_ID = "iteminteractions";
    public static final String MOD_NAME = "Item Interactions";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_NAME);

    public static final ConfigHolder CONFIG = ConfigHolder.builder(MOD_ID)
            .client(ClientConfig.class)
            .server(ServerConfig.class);
    public static final Identifier DEVELOPMENT_ID = id("development");

    @Override
    public void onConstructMod() {
        ModRegistry.bootstrap();
        registerEventHandlers();
    }

    private static void registerEventHandlers() {
        SyncDataPackContentsCallback.EVENT.register(ItemStorageManager::onSyncDataPackContents);
        ServerResourcesLoadCallback.EVENT.register(ItemStorageManager::onServerResourcesLoad);
        ContainerEvents.OPEN.register(EnderChestSyncHandler::onContainerOpen);
        PlayerNetworkEvents.JOIN.register(EnderChestSyncHandler::onPlayerJoin);
        AfterChangeDimensionCallback.EVENT.register(EnderChestSyncHandler::onAfterChangeDimension);
        PlayerCopyEvents.RESPAWN.register(EnderChestSyncHandler::onRespawn);
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
        context.playToClient(ClientboundSyncItemStorage.class, ClientboundSyncItemStorage.STREAM_CODEC);
    }

    @Override
    public void onRegisterGameRegistries(GameRegistriesContext context) {
        context.registerRegistry(ItemStorage.REGISTRY);
    }

    @Override
    public void onRegisterDataPackRegistries(DataPackRegistriesContext context) {
        // TODO This should be registered as a reloadable registry; when possible in NeoForge.
        context.registerRegistry(ItemStorage.Definition.REGISTRY_KEY, ItemStorage.Definition.DIRECT_CODEC);
    }

    @Override
    public void onAddDataPackFinders(PackRepositorySourcesContext context) {
        if (!ModLoaderEnvironment.INSTANCE.isDevelopmentEnvironment(MOD_ID)) {
            return;
        }

        context.registerBuiltInPack(DEVELOPMENT_ID, Component.literal("Development"), true);
    }

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }
}
