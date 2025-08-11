package fuzs.iteminteractions.impl.client;

import com.google.common.collect.ImmutableMap;
import fuzs.iteminteractions.api.v1.client.tooltip.ClientBundleContentsTooltip;
import fuzs.iteminteractions.api.v1.client.tooltip.ClientItemContentsTooltip;
import fuzs.iteminteractions.api.v1.tooltip.BundleContentsTooltip;
import fuzs.iteminteractions.api.v1.tooltip.ItemContentsTooltip;
import fuzs.iteminteractions.impl.client.handler.ClientInputActionHandler;
import fuzs.iteminteractions.impl.client.handler.MouseDraggingHandler;
import fuzs.iteminteractions.impl.config.CarriedItemTooltips;
import fuzs.iteminteractions.impl.config.ExtractSingleItem;
import fuzs.iteminteractions.impl.config.SelectedItemTooltips;
import fuzs.iteminteractions.impl.config.VisualItemContents;
import fuzs.iteminteractions.impl.world.item.container.ItemContentsProviders;
import fuzs.puzzleslib.api.client.core.v1.ClientModConstructor;
import fuzs.puzzleslib.api.client.core.v1.context.ClientTooltipComponentsContext;
import fuzs.puzzleslib.api.client.core.v1.context.KeyMappingsContext;
import fuzs.puzzleslib.api.client.event.v1.entity.player.ClientPlayerNetworkEvents;
import fuzs.puzzleslib.api.client.event.v1.gui.ContainerScreenEvents;
import fuzs.puzzleslib.api.client.event.v1.gui.ScreenEvents;
import fuzs.puzzleslib.api.client.event.v1.gui.ScreenKeyboardEvents;
import fuzs.puzzleslib.api.client.event.v1.gui.ScreenMouseEvents;
import fuzs.puzzleslib.api.client.key.v1.KeyActivationContext;
import fuzs.puzzleslib.api.event.v1.core.EventPhase;
import fuzs.puzzleslib.api.event.v1.level.PlaySoundEvents;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.Connection;

public class ItemInteractionsClient implements ClientModConstructor {

    @Override
    public void onConstructMod() {
        registerEventHandlers();
    }

    private static void registerEventHandlers() {
        ScreenMouseEvents.beforeMouseClick(AbstractContainerScreen.class)
                .register(EventPhase.BEFORE, ClientInputActionHandler::onBeforeMousePressed);
        ScreenKeyboardEvents.beforeKeyPress(AbstractContainerScreen.class)
                .register(EventPhase.BEFORE, ClientInputActionHandler::onBeforeKeyPressed);
        ScreenMouseEvents.beforeMouseClick(AbstractContainerScreen.class)
                .register(EventPhase.BEFORE, MouseDraggingHandler::onBeforeMousePressed);
        ScreenMouseEvents.beforeMouseRelease(AbstractContainerScreen.class)
                .register(EventPhase.BEFORE, MouseDraggingHandler::onBeforeMouseRelease);
        ScreenMouseEvents.beforeMouseRelease(AbstractContainerScreen.class)
                .register(EventPhase.BEFORE, ClientInputActionHandler::onBeforeMouseRelease);
        ScreenMouseEvents.beforeMouseScroll(AbstractContainerScreen.class)
                .register(EventPhase.BEFORE, ClientInputActionHandler::onBeforeMouseScroll);
        ScreenMouseEvents.beforeMouseDrag(AbstractContainerScreen.class)
                .register(EventPhase.BEFORE, MouseDraggingHandler::onBeforeMouseDragged);
        ScreenKeyboardEvents.beforeKeyPress(AbstractContainerScreen.class)
                .register(SelectedItemTooltips::onBeforeKeyPressed);
        ScreenKeyboardEvents.beforeKeyPress(AbstractContainerScreen.class)
                .register(CarriedItemTooltips::onBeforeKeyPressed);
        ScreenKeyboardEvents.beforeKeyPress(AbstractContainerScreen.class)
                .register(ExtractSingleItem::onBeforeKeyPressed);
        ScreenKeyboardEvents.beforeKeyPress(AbstractContainerScreen.class)
                .register(VisualItemContents::onBeforeKeyPressed);
        ScreenEvents.afterInit(AbstractContainerScreen.class).register(ClientInputActionHandler::onAfterInit);
        ScreenEvents.afterRender(AbstractContainerScreen.class).register(ClientInputActionHandler::onAfterRender);
        ContainerScreenEvents.FOREGROUND.register(MouseDraggingHandler::onDrawForeground);
        PlaySoundEvents.AT_ENTITY.register(MouseDraggingHandler::onPlaySoundAtEntity);
        PlaySoundEvents.AT_ENTITY.register(ClientInputActionHandler::onPlaySoundAtEntity);
        ClientPlayerNetworkEvents.LOGGED_OUT.register((LocalPlayer player, MultiPlayerGameMode multiPlayerGameMode, Connection connection) -> {
            ItemContentsProviders.setItemContainerProviders(ImmutableMap.of());
        });
    }

    @Override
    public void onRegisterKeyMappings(KeyMappingsContext context) {
        context.registerKeyMapping(VisualItemContents.KEY_MAPPING,
                KeyActivationContext.SCREEN);
        context.registerKeyMapping(SelectedItemTooltips.KEY_MAPPING,
                KeyActivationContext.SCREEN);
        context.registerKeyMapping(CarriedItemTooltips.KEY_MAPPING,
                KeyActivationContext.SCREEN);
    }

    @Override
    public void onRegisterClientTooltipComponents(ClientTooltipComponentsContext context) {
        context.registerClientTooltipComponent(ItemContentsTooltip.class, ClientItemContentsTooltip::new);
        context.registerClientTooltipComponent(BundleContentsTooltip.class, ClientBundleContentsTooltip::new);
    }
}
