package fuzs.iteminteractions.impl.client;

import com.google.common.collect.ImmutableMap;
import fuzs.iteminteractions.api.v1.client.tooltip.ClientItemContentsTooltip;
import fuzs.iteminteractions.api.v1.client.tooltip.ClientBundleContentsTooltip;
import fuzs.iteminteractions.api.v1.tooltip.ItemContentsTooltip;
import fuzs.iteminteractions.api.v1.tooltip.BundleContentsTooltip;
import fuzs.iteminteractions.impl.client.core.HeldActivationType;
import fuzs.iteminteractions.impl.client.core.KeyMappingProvider;
import fuzs.iteminteractions.impl.client.handler.ClientInputActionHandler;
import fuzs.iteminteractions.impl.client.handler.KeyBindingTogglesHandler;
import fuzs.iteminteractions.impl.client.handler.MouseDraggingHandler;
import fuzs.iteminteractions.impl.client.helper.ItemDecorationHelper;
import fuzs.iteminteractions.impl.world.item.container.ItemContentsProviders;
import fuzs.puzzleslib.api.client.core.v1.ClientModConstructor;
import fuzs.puzzleslib.api.client.core.v1.context.ClientTooltipComponentsContext;
import fuzs.puzzleslib.api.client.core.v1.context.KeyMappingsContext;
import fuzs.puzzleslib.api.client.event.v1.entity.player.ClientPlayerNetworkEvents;
import fuzs.puzzleslib.api.client.event.v1.gui.ContainerScreenEvents;
import fuzs.puzzleslib.api.client.event.v1.gui.ScreenEvents;
import fuzs.puzzleslib.api.client.event.v1.gui.ScreenKeyboardEvents;
import fuzs.puzzleslib.api.client.event.v1.gui.ScreenMouseEvents;
import fuzs.puzzleslib.api.event.v1.core.EventPhase;
import fuzs.puzzleslib.api.event.v1.level.PlayLevelSoundEvents;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.Connection;

public class ItemInteractionsClient implements ClientModConstructor {

    @Override
    public void onConstructMod() {
        registerHandlers();
    }

    private static void registerHandlers() {
        ScreenMouseEvents.beforeMouseClick(AbstractContainerScreen.class).register(EventPhase.BEFORE, ClientInputActionHandler::onBeforeMousePressed);
        ScreenMouseEvents.beforeMouseClick(AbstractContainerScreen.class).register(EventPhase.BEFORE, MouseDraggingHandler::onBeforeMousePressed);
        ScreenMouseEvents.beforeMouseRelease(AbstractContainerScreen.class).register(EventPhase.BEFORE, MouseDraggingHandler::onBeforeMouseRelease);
        ScreenMouseEvents.beforeMouseRelease(AbstractContainerScreen.class).register(EventPhase.BEFORE, ClientInputActionHandler::onBeforeMouseRelease);
        ScreenMouseEvents.beforeMouseScroll(AbstractContainerScreen.class).register(EventPhase.BEFORE, ClientInputActionHandler::onBeforeMouseScroll);
        ScreenMouseEvents.beforeMouseDrag(AbstractContainerScreen.class).register(EventPhase.BEFORE, MouseDraggingHandler::onBeforeMouseDragged);
        ScreenKeyboardEvents.beforeKeyPress(AbstractContainerScreen.class).register(EventPhase.BEFORE, ClientInputActionHandler::onBeforeKeyPressed);
        ScreenKeyboardEvents.beforeKeyPress(AbstractContainerScreen.class).register(KeyBindingTogglesHandler::onBeforeKeyPressed);
        ScreenEvents.afterRender(AbstractContainerScreen.class).register(ClientInputActionHandler::onAfterRender);
        ContainerScreenEvents.FOREGROUND.register(MouseDraggingHandler::onDrawForeground);
        PlayLevelSoundEvents.ENTITY.register(MouseDraggingHandler::onPlaySoundAtPosition);
        PlayLevelSoundEvents.ENTITY.register(ClientInputActionHandler::onPlaySoundAtPosition);
        ClientPlayerNetworkEvents.LOGGED_IN.register((LocalPlayer player, MultiPlayerGameMode multiPlayerGameMode, Connection connection) -> {
            ItemContentsProviders.setItemContainerProviders(ImmutableMap.of());
            ItemDecorationHelper.clearCache();
        });
    }

    @Override
    public void onRegisterKeyMappings(KeyMappingsContext context) {
        HeldActivationType.getKeyMappingProviders().map(KeyMappingProvider::getKeyMapping).forEach(context::registerKeyMapping);
    }

    @Override
    public void onRegisterClientTooltipComponents(ClientTooltipComponentsContext context) {
        context.registerClientTooltipComponent(ItemContentsTooltip.class, ClientItemContentsTooltip::new);
        context.registerClientTooltipComponent(BundleContentsTooltip.class, ClientBundleContentsTooltip::new);
    }
}
