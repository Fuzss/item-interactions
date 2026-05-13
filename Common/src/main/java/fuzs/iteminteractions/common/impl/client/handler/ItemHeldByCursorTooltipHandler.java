package fuzs.iteminteractions.common.impl.client.handler;

import fuzs.iteminteractions.common.api.v1.world.item.storage.ItemStorageHolder;
import fuzs.iteminteractions.common.impl.ItemInteractions;
import fuzs.iteminteractions.common.impl.config.ClientConfig;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;

public class ItemHeldByCursorTooltipHandler {

    /**
     * Shows the item tooltip for the item held by the cursor; to be used with the single item moving feature to be able
     * to continuously see what's going on.
     *
     * @see AbstractContainerScreen#extractTooltip(GuiGraphicsExtractor, int, int)
     */
    public static void onAfterBackground(AbstractContainerScreen<?> screen, GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        if (!ItemInteractions.CONFIG.get(ClientConfig.class).itemHeldByCursorTooltip.isUsed()) {
            return;
        }

        ItemStack itemHeldByCursor = screen.getMenu().getCarried();
        if (!itemHeldByCursor.isEmpty() && !showTooltipWithItemInHand(screen)) {
            ItemStorageHolder holder = ItemStorageHolder.ofItem(itemHeldByCursor);
            if (!holder.isEmpty() && holder.storage().hasContents(itemHeldByCursor)) {
                guiGraphics.setTooltipForNextFrame(screen.getFont(),
                        screen.getTooltipFromContainerItem(itemHeldByCursor),
                        itemHeldByCursor.getTooltipImage(),
                        mouseX,
                        mouseY,
                        itemHeldByCursor.get(DataComponents.TOOLTIP_STYLE));
            }
        }
    }

    /**
     * @see AbstractContainerScreen#extractTooltip(GuiGraphicsExtractor, int, int)
     */
    private static boolean showTooltipWithItemInHand(AbstractContainerScreen<?> screen) {
        if (screen.hoveredSlot != null && screen.hoveredSlot.hasItem()) {
            ItemStack item = screen.hoveredSlot.getItem();
            return screen.showTooltipWithItemInHand(item);
        } else {
            return false;
        }
    }
}
