package fuzs.iteminteractions.common.impl.client.gui;

import com.mojang.blaze3d.platform.InputConstants;
import fuzs.iteminteractions.common.api.v1.world.item.storage.ItemStorageHolder;
import fuzs.iteminteractions.common.impl.ItemInteractions;
import fuzs.iteminteractions.common.impl.config.ClientConfig;
import fuzs.iteminteractions.common.impl.config.ServerConfig;
import fuzs.iteminteractions.common.impl.network.client.ServerboundContainerClientInputMessage;
import fuzs.iteminteractions.common.impl.network.client.ServerboundSelectedItemMessage;
import fuzs.iteminteractions.common.impl.world.inventory.ContainerSlotHelper;
import fuzs.puzzleslib.common.api.network.v4.MessageSender;
import fuzs.puzzleslib.common.api.util.v1.CommonHelper;
import net.minecraft.client.gui.BundleMouseActions;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.joml.Vector2i;

import java.util.List;
import java.util.OptionalInt;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;

/**
 * @see BundleMouseActions
 */
public class ItemStorageMouseActions extends BundleMouseActions implements CustomItemSlotMouseAction {
    private final AbstractContainerScreen<?> screen;

    public ItemStorageMouseActions(AbstractContainerScreen<?> screen) {
        super(screen.minecraft);
        this.screen = screen;
    }

    public static void onAfterInit(AbstractContainerScreen<?> screen, int screenWidth, int screenHeight, List<AbstractWidget> widgets, UnaryOperator<AbstractWidget> addWidget, Consumer<AbstractWidget> removeWidget) {
        screen.itemSlotMouseActions.addFirst(new ItemStorageMouseActions(screen));
    }

    public static boolean extractSingleItemOnly() {
        return ItemInteractions.CONFIG.get(ServerConfig.class).allowPrecisionMode && ItemInteractions.CONFIG.get(
                ClientConfig.class).extractSingleItem.isActive();
    }

    @Override
    public boolean matches(Slot slot) {
        return CustomItemSlotMouseAction.super.matches(slot);
    }

    @Override
    public boolean matches(ItemStack itemStack) {
        return !ItemStorageHolder.ofItem(itemStack).isEmpty();
    }

    @Override
    public boolean onMouseScrolled(double scrollX, double scrollY, int slotIndex, ItemStack itemStack) {
        return CustomItemSlotMouseAction.super.onMouseScrolled(scrollX, scrollY, slotIndex, itemStack);
    }

    @Override
    public boolean onMouseScrolled(double scrollX, double scrollY, OptionalInt slotIndex, ItemStack itemStack) {
        if (!ItemInteractions.CONFIG.get(ClientConfig.class).visualItemContents.isActive()) {
            return false;
        }

        if (this.screen.hoveredSlot != null && extractSingleItemOnly()) {
            Vector2i wheelXY = this.scrollWheelHandler.onMouseScroll(scrollX, scrollY);
            int wheel = wheelXY.y == 0 ? -wheelXY.x : wheelXY.y;
            if (wheel != 0) {
                Slot slot = this.screen.hoveredSlot;
                int buttonNum = this.getMouseButtonFromWheel(wheel);
                this.setSingleItemOnly(true);
                this.screen.slotClicked(slot, slot.index, buttonNum, ContainerInput.PICKUP);
                this.setSingleItemOnly(false);
            }

            return true;
        }

        if (itemStack == this.screen.getMenu().getCarried()
                && !ItemInteractions.CONFIG.get(ClientConfig.class).carriedItemTooltips.isActive()) {
            return false;
        }

        ItemStorageHolder holder = ItemStorageHolder.ofItem(itemStack);
        if (holder.storage().hasContents(itemStack)) {
            Vector2i wheelXY = this.scrollWheelHandler.onMouseScroll(scrollX, scrollY);
            int wheel = wheelXY.y == 0 ? -wheelXY.x : wheelXY.y;
            if (wheel != 0) {
                int selectedItem = ContainerSlotHelper.getSelectedItem(itemStack);
                Container container = holder.getContainerView(itemStack, this.minecraft.player);
                int updatedSelectedItem = ContainerSlotHelper.findClosestSlotWithContent(container,
                        selectedItem,
                        wheel < 0,
                        CommonHelper.hasShiftDown());
                if (selectedItem != updatedSelectedItem) {
                    this.toggleSelectedBundleItem(itemStack, slotIndex, updatedSelectedItem);
                }
            }

            return true;
        } else {
            return false;
        }
    }

    private int getMouseButtonFromWheel(int wheel) {
        if (ItemInteractions.CONFIG.get(ClientConfig.class).invertPrecisionModeScrolling ? wheel < 0 : wheel > 0) {
            return InputConstants.MOUSE_BUTTON_RIGHT;
        } else {
            return InputConstants.MOUSE_BUTTON_LEFT;
        }
    }

    /**
     * Important to call before click actions (notably in the creative menu).
     *
     * @see MultiPlayerGameMode#ensureHasSentCarriedItem()
     */
    private void setSingleItemOnly(boolean singleItemOnly) {
        ContainerSlotHelper.extractSingleItem(this.minecraft.player, singleItemOnly);
        MessageSender.broadcast(new ServerboundContainerClientInputMessage(singleItemOnly));
    }

    @Override
    public void toggleSelectedBundleItem(ItemStack bundleItem, int slotIndex, int updatedSelectedItem) {
        this.toggleSelectedBundleItem(bundleItem, OptionalInt.of(slotIndex), updatedSelectedItem);
    }

    private void toggleSelectedBundleItem(ItemStack bundleItem, OptionalInt slotIndex, int updatedSelectedItem) {
        int previousSelectedItem = ContainerSlotHelper.getSelectedItem(bundleItem);
        ContainerSlotHelper.setSelectedItem(bundleItem, updatedSelectedItem);
        ItemStorageHolder holder = ItemStorageHolder.ofItem(bundleItem);
        holder.storage().onToggleSelectedItem(bundleItem, previousSelectedItem, updatedSelectedItem);
        MessageSender.broadcast(new ServerboundSelectedItemMessage(slotIndex, updatedSelectedItem));
    }
}
