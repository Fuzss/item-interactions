package fuzs.iteminteractions.impl;

import fuzs.iteminteractions.api.v1.ItemContainerProviderBuilder;
import fuzs.iteminteractions.api.v1.ItemContainerProviderSerializers;
import fuzs.iteminteractions.api.v1.provider.BlockEntityProvider;
import fuzs.iteminteractions.api.v1.provider.EnderChestProvider;
import fuzs.iteminteractions.impl.config.ClientConfig;
import fuzs.iteminteractions.impl.config.ServerConfig;
import fuzs.iteminteractions.impl.data.DynamicItemContainerProvider;
import fuzs.iteminteractions.impl.handler.EnderChestMenuHandler;
import fuzs.iteminteractions.impl.init.ModRegistry;
import fuzs.iteminteractions.impl.network.S2CEnderChestSetContentMessage;
import fuzs.iteminteractions.impl.network.S2CEnderChestSetSlotMessage;
import fuzs.iteminteractions.impl.network.S2CSyncItemContainerProvider;
import fuzs.iteminteractions.impl.network.client.C2SContainerClientInputMessage;
import fuzs.iteminteractions.impl.network.client.C2SEnderChestMenuMessage;
import fuzs.iteminteractions.impl.network.client.C2SEnderChestSetSlotMessage;
import fuzs.iteminteractions.impl.world.item.container.ItemContainerProviders;
import fuzs.puzzleslib.api.config.v3.ConfigHolder;
import fuzs.puzzleslib.api.core.v1.ModConstructor;
import fuzs.puzzleslib.api.core.v1.ModLoaderEnvironment;
import fuzs.puzzleslib.api.core.v1.context.AddReloadListenersContext;
import fuzs.puzzleslib.api.core.v1.context.PackRepositorySourcesContext;
import fuzs.puzzleslib.api.event.v1.entity.player.PlayerTickEvents;
import fuzs.puzzleslib.api.event.v1.server.SyncDataPackContentsCallback;
import fuzs.puzzleslib.api.network.v2.NetworkHandlerV2;
import fuzs.puzzleslib.api.resources.v1.DynamicPackResources;
import fuzs.puzzleslib.api.resources.v1.PackResourcesHelper;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ItemInteractions implements ModConstructor {
    public static final String MOD_ID = "iteminteractions";
    public static final String MOD_NAME = "Item Interactions";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_NAME);

    public static final NetworkHandlerV2 NETWORK = NetworkHandlerV2.build(MOD_ID, false);
    public static final ConfigHolder CONFIG = ConfigHolder.builder(MOD_ID).client(ClientConfig.class).server(
                    ServerConfig.class);

    @Override
    public void onConstructMod() {
        ModRegistry.touch();
        registerMessages();
        registerHandlers();
    }

    private static void registerMessages() {
        NETWORK.registerServerbound(C2SContainerClientInputMessage.class, C2SContainerClientInputMessage::new);
        NETWORK.registerClientbound(S2CEnderChestSetContentMessage.class, S2CEnderChestSetContentMessage::new);
        NETWORK.registerClientbound(S2CEnderChestSetSlotMessage.class, S2CEnderChestSetSlotMessage::new);
        NETWORK.registerServerbound(C2SEnderChestSetSlotMessage.class, C2SEnderChestSetSlotMessage::new);
        NETWORK.registerServerbound(C2SEnderChestMenuMessage.class, C2SEnderChestMenuMessage::new);
        NETWORK.registerClientbound(S2CSyncItemContainerProvider.class, S2CSyncItemContainerProvider::new);
    }

    private static void registerHandlers() {
        PlayerTickEvents.START.register(EnderChestMenuHandler::onLivingTick);
        SyncDataPackContentsCallback.EVENT.register(ItemContainerProviders.INSTANCE::sendProvidersToPlayer);
    }

    @Override
    public void onCommonSetup() {
        setupDevelopmentEnvironment();
    }

    private static void setupDevelopmentEnvironment() {
        if (ModLoaderEnvironment.INSTANCE.isDevelopmentEnvironment()) {
            ItemContainerProviderSerializers.register(BlockEntityProvider.class,
                    id("block_entity"),
                    ItemContainerProviderBuilder.fromJson(ItemContainerProviderBuilder::toBlockEntityProvider)
            );
            ItemContainerProviderSerializers.register(EnderChestProvider.class,
                    id("ender_chest"),
                    ItemContainerProviderBuilder.fromJson(ItemContainerProviderBuilder::toEnderChestProvider)
            );
        }
    }

    @Override
    public void onRegisterDataPackReloadListeners(AddReloadListenersContext context) {
        context.registerReloadListener(ItemContainerProviders.ITEM_CONTAINER_PROVIDERS_KEY, ItemContainerProviders.INSTANCE);
    }

    @Override
    public void onAddDataPackFinders(PackRepositorySourcesContext context) {
        if (ModLoaderEnvironment.INSTANCE.isDevelopmentEnvironment()) {
            context.addRepositorySource(PackResourcesHelper.buildServerPack(id("test_item_interactions"),
                    DynamicPackResources.create(DynamicItemContainerProvider::new), false
            ));
        }
    }

    public static ResourceLocation id(String path) {
        return new ResourceLocation(MOD_ID, path);
    }
}
