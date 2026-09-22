package fuzs.iteminteractions.common.impl.client;

import fuzs.iteminteractions.common.api.v2.client.gui.screens.inventory.tooltip.ClientBundleContentsTooltip;
import fuzs.iteminteractions.common.api.v2.client.gui.screens.inventory.tooltip.ClientItemContentsTooltip;
import fuzs.iteminteractions.common.api.v2.world.inventory.tooltip.BundleContentsTooltip;
import fuzs.iteminteractions.common.impl.client.gui.screens.inventory.tooltip.CollapsibleClientTooltipComponent;
import fuzs.iteminteractions.common.impl.client.handler.ClientEventHandler;
import fuzs.iteminteractions.common.impl.client.handler.ItemSlotMouseActionHandler;
import fuzs.iteminteractions.common.impl.config.ItemHeldByCursorTooltip;
import fuzs.iteminteractions.common.impl.config.ItemStorageTooltip;
import fuzs.puzzleslib.common.api.client.core.v1.ClientModConstructor;
import fuzs.puzzleslib.common.api.client.core.v1.context.ClientTooltipComponentsContext;
import fuzs.puzzleslib.common.api.client.core.v1.context.KeyMappingsContext;
import fuzs.puzzleslib.common.api.client.event.v1.entity.player.ClientPlayerNetworkEvents;
import fuzs.puzzleslib.common.api.client.event.v1.gui.*;
import fuzs.puzzleslib.common.api.client.key.v1.KeyActivationContext;
import fuzs.puzzleslib.common.api.event.v1.core.EventPhase;
import fuzs.puzzleslib.common.api.event.v1.level.PlaySoundEvents;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;

public class ItemInteractionsClient implements ClientModConstructor {

    @Override
    public void onConstructMod() {
        registerEventHandlers();
    }

    private static void registerEventHandlers() {
        ScreenKeyboardEvents.beforeKeyPress(AbstractContainerScreen.class)
                .register(ItemHeldByCursorTooltip::onBeforeKeyPressed);
        ScreenKeyboardEvents.beforeKeyPress(AbstractContainerScreen.class)
                .register(ItemStorageTooltip::onBeforeKeyPressed);
        ScreenEvents.afterBackground(AbstractContainerScreen.class)
                .register(ItemSlotMouseActionHandler::onAfterBackground);
        ScreenEvents.afterForeground(AbstractContainerScreen.class)
                .register(ItemSlotMouseActionHandler::onAfterForeground);
        ScreenMouseEvents.beforeMouseClick(AbstractContainerScreen.class)
                .register(EventPhase.BEFORE, ItemSlotMouseActionHandler::onBeforeMouseClicked);
        ScreenMouseEvents.beforeMouseRelease(AbstractContainerScreen.class)
                .register(EventPhase.BEFORE, ItemSlotMouseActionHandler::onBeforeMouseRelease);
        ScreenMouseEvents.beforeMouseDrag(AbstractContainerScreen.class)
                .register(EventPhase.BEFORE, ItemSlotMouseActionHandler::onBeforeMouseDragged);
        ScreenMouseEvents.beforeMouseScroll(AbstractContainerScreen.class)
                .register(ItemSlotMouseActionHandler::onBeforeMouseScroll);
        ScreenKeyboardEvents.beforeKeyPress(AbstractContainerScreen.class)
                .register(ItemSlotMouseActionHandler::onBeforeKeyPress);
        PlaySoundEvents.AT_ENTITY.register(ClientEventHandler::onPlaySoundAtEntity);
        ItemTooltipCallback.EVENT.register(ClientEventHandler::onItemTooltip);
        ScreenEvents.afterInit(AbstractContainerScreen.class).register(ClientEventHandler::onAfterInit);
        ClientPlayerNetworkEvents.LEAVE.register(ClientEventHandler::onPlayerLeave);
    }

    @Override
    public void onRegisterKeyMappings(KeyMappingsContext context) {
        context.registerKeyMapping(ItemStorageTooltip.KEY_MAPPING, KeyActivationContext.SCREEN);
        context.registerKeyMapping(ItemHeldByCursorTooltip.KEY_MAPPING, KeyActivationContext.SCREEN);
    }

    @Override
    public void onRegisterClientTooltipComponents(ClientTooltipComponentsContext context) {
        context.registerClientTooltipComponent(fuzs.iteminteractions.common.api.v2.world.inventory.tooltip.ItemContentsTooltip.class,
                CollapsibleClientTooltipComponent.wrapFactory(ClientItemContentsTooltip::new));
        context.registerClientTooltipComponent(BundleContentsTooltip.class,
                CollapsibleClientTooltipComponent.wrapFactory(ClientBundleContentsTooltip::new));
    }
}
