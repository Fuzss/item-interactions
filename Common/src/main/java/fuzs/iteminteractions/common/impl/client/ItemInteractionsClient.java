package fuzs.iteminteractions.common.impl.client;

import com.google.common.collect.ImmutableMap;
import fuzs.iteminteractions.common.api.v1.client.gui.screens.inventory.tooltip.ClientBundleContentsTooltip;
import fuzs.iteminteractions.common.api.v1.client.gui.screens.inventory.tooltip.ClientItemContentsTooltip;
import fuzs.iteminteractions.common.api.v1.world.inventory.tooltip.BundleContentsTooltip;
import fuzs.iteminteractions.common.impl.client.gui.ItemStorageMouseActions;
import fuzs.iteminteractions.common.impl.client.gui.screens.inventory.tooltip.CollapsibleClientTooltipComponent;
import fuzs.iteminteractions.common.impl.client.handler.ClientEventHandler;
import fuzs.iteminteractions.common.impl.client.handler.ItemSlotMouseActionHandler;
import fuzs.iteminteractions.common.impl.config.ItemContentsTooltip;
import fuzs.iteminteractions.common.impl.config.ItemHeldByCursorTooltip;
import fuzs.iteminteractions.common.impl.world.item.container.ItemStorageManager;
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
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.Connection;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;

public class ItemInteractionsClient implements ClientModConstructor {

    @Override
    public void onConstructMod() {
        registerEventHandlers();
    }

    private static void registerEventHandlers() {
        ScreenMouseEvents.beforeMouseClick(AbstractContainerScreen.class)
                .register(EventPhase.BEFORE, ItemSlotMouseActionHandler::onBeforeMouseClicked);
        ScreenMouseEvents.beforeMouseRelease(AbstractContainerScreen.class)
                .register(EventPhase.BEFORE, ItemSlotMouseActionHandler::onBeforeMouseRelease);
        ScreenMouseEvents.beforeMouseDrag(AbstractContainerScreen.class)
                .register(EventPhase.BEFORE, ItemSlotMouseActionHandler::onBeforeMouseDragged);
        ScreenKeyboardEvents.beforeKeyPress(AbstractContainerScreen.class)
                .register(ItemHeldByCursorTooltip::onBeforeKeyPressed);
        ScreenKeyboardEvents.beforeKeyPress(AbstractContainerScreen.class)
                .register(ItemContentsTooltip::onBeforeKeyPressed);
        ScreenMouseEvents.beforeMouseScroll(AbstractContainerScreen.class)
                .register(ItemSlotMouseActionHandler::onBeforeMouseScroll);
        ScreenKeyboardEvents.beforeKeyPress(AbstractContainerScreen.class)
                .register(ItemSlotMouseActionHandler::onBeforeKeyPress);
        ScreenEvents.afterInit(AbstractContainerScreen.class).register(ItemInteractionsClient::onAfterInit);
        ScreenEvents.afterBackground(AbstractContainerScreen.class)
                .register(ClientEventHandler::onAfterBackground);
        ScreenEvents.afterBackground(AbstractContainerScreen.class).register(ItemSlotMouseActionHandler::onAfterBackground);
        RenderContainerScreenContentsCallback.EVENT.register(ItemSlotMouseActionHandler::onRenderContainerScreenContents);
        PlaySoundEvents.AT_ENTITY.register(ClientEventHandler::onPlaySoundAtEntity);
        ClientPlayerNetworkEvents.LEAVE.register(ItemInteractionsClient::onPlayerLeave);
    }

    private static void onAfterInit(AbstractContainerScreen<?> screen, int screenWidth, int screenHeight, List<AbstractWidget> widgets, UnaryOperator<AbstractWidget> addWidget, Consumer<AbstractWidget> removeWidget) {
        screen.itemSlotMouseActions.addFirst(new ItemStorageMouseActions(screen));
    }

    private static void onPlayerLeave(LocalPlayer player, MultiPlayerGameMode multiPlayerGameMode, Connection connection) {
        ItemStorageManager.setItemStorageDefinitions(ImmutableMap.of());
    }

    @Override
    public void onRegisterKeyMappings(KeyMappingsContext context) {
        context.registerKeyMapping(ItemContentsTooltip.KEY_MAPPING, KeyActivationContext.SCREEN);
        context.registerKeyMapping(ItemHeldByCursorTooltip.KEY_MAPPING, KeyActivationContext.SCREEN);
    }

    @Override
    public void onRegisterClientTooltipComponents(ClientTooltipComponentsContext context) {
        context.registerClientTooltipComponent(fuzs.iteminteractions.common.api.v1.world.inventory.tooltip.ItemContentsTooltip.class,
                CollapsibleClientTooltipComponent.wrapFactory(ClientItemContentsTooltip::new));
        context.registerClientTooltipComponent(BundleContentsTooltip.class,
                CollapsibleClientTooltipComponent.wrapFactory(ClientBundleContentsTooltip::new));
    }
}
