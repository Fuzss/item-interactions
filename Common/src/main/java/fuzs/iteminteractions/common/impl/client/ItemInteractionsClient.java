package fuzs.iteminteractions.common.impl.client;

import com.google.common.collect.ImmutableMap;
import fuzs.iteminteractions.common.api.v1.client.gui.screens.inventory.tooltip.ClientBundleContentsTooltip;
import fuzs.iteminteractions.common.api.v1.client.gui.screens.inventory.tooltip.ClientItemContentsTooltip;
import fuzs.iteminteractions.common.api.v1.world.inventory.tooltip.BundleContentsTooltip;
import fuzs.iteminteractions.common.api.v1.world.inventory.tooltip.ItemContentsTooltip;
import fuzs.iteminteractions.common.impl.client.gui.ItemContentsMouseActions;
import fuzs.iteminteractions.common.impl.client.gui.screens.inventory.tooltip.CollapsibleClientTooltipComponent;
import fuzs.iteminteractions.common.impl.client.handler.InteractionSoundsHandler;
import fuzs.iteminteractions.common.impl.client.handler.MouseDraggingHandler;
import fuzs.iteminteractions.common.impl.config.ExtractSingleItem;
import fuzs.iteminteractions.common.impl.config.VisualItemContents;
import fuzs.iteminteractions.common.impl.world.item.container.ItemContentsProviders;
import fuzs.puzzleslib.common.api.client.core.v1.ClientModConstructor;
import fuzs.puzzleslib.common.api.client.core.v1.context.ClientTooltipComponentsContext;
import fuzs.puzzleslib.common.api.client.core.v1.context.KeyMappingsContext;
import fuzs.puzzleslib.common.api.client.event.v1.entity.player.ClientPlayerNetworkEvents;
import fuzs.puzzleslib.common.api.client.event.v1.gui.RenderContainerScreenContentsCallback;
import fuzs.puzzleslib.common.api.client.event.v1.gui.ScreenEvents;
import fuzs.puzzleslib.common.api.client.event.v1.gui.ScreenKeyboardEvents;
import fuzs.puzzleslib.common.api.client.event.v1.gui.ScreenMouseEvents;
import fuzs.puzzleslib.common.api.client.key.v1.KeyActivationContext;
import fuzs.puzzleslib.common.api.event.v1.core.EventPhase;
import fuzs.puzzleslib.common.api.event.v1.level.PlaySoundEvents;
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
                .register(EventPhase.BEFORE, MouseDraggingHandler::onBeforeMousePressed);
        ScreenMouseEvents.beforeMouseRelease(AbstractContainerScreen.class)
                .register(EventPhase.BEFORE, MouseDraggingHandler::onBeforeMouseRelease);
        ScreenMouseEvents.beforeMouseDrag(AbstractContainerScreen.class)
                .register(EventPhase.BEFORE, MouseDraggingHandler::onBeforeMouseDragged);
        ScreenKeyboardEvents.beforeKeyPress(AbstractContainerScreen.class)
                .register(ExtractSingleItem::onBeforeKeyPressed);
        ScreenKeyboardEvents.beforeKeyPress(AbstractContainerScreen.class)
                .register(VisualItemContents::onBeforeKeyPressed);
        ScreenEvents.afterInit(AbstractContainerScreen.class).register(ItemContentsMouseActions::onAfterInit);
        ScreenEvents.afterBackground(AbstractContainerScreen.class).register(MouseDraggingHandler::onAfterBackground);
        RenderContainerScreenContentsCallback.EVENT.register(MouseDraggingHandler::onRenderContainerScreenContents);
        PlaySoundEvents.AT_ENTITY.register(MouseDraggingHandler::onPlaySoundAtEntity);
        PlaySoundEvents.AT_ENTITY.register(InteractionSoundsHandler::onPlaySoundAtEntity);
        ClientPlayerNetworkEvents.LEAVE.register((LocalPlayer player, MultiPlayerGameMode multiPlayerGameMode, Connection connection) -> {
            ItemContentsProviders.setItemContainerProviders(ImmutableMap.of());
        });
    }

    @Override
    public void onRegisterKeyMappings(KeyMappingsContext context) {
        context.registerKeyMapping(VisualItemContents.KEY_MAPPING, KeyActivationContext.SCREEN);
    }

    @Override
    public void onRegisterClientTooltipComponents(ClientTooltipComponentsContext context) {
        context.registerClientTooltipComponent(ItemContentsTooltip.class,
                CollapsibleClientTooltipComponent.wrapFactory(ClientItemContentsTooltip::new));
        context.registerClientTooltipComponent(BundleContentsTooltip.class,
                CollapsibleClientTooltipComponent.wrapFactory(ClientBundleContentsTooltip::new));
    }
}
