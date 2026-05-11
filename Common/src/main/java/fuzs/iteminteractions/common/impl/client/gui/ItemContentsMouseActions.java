package fuzs.iteminteractions.common.impl.client.gui;

import com.mojang.blaze3d.platform.InputConstants;
import fuzs.iteminteractions.common.api.v1.world.item.storage.ItemStorageHolder;
import fuzs.iteminteractions.common.impl.ItemInteractions;
import fuzs.iteminteractions.common.impl.config.ClientConfig;
import fuzs.iteminteractions.common.impl.config.ServerConfig;
import fuzs.iteminteractions.common.impl.network.client.ServerboundContainerClientInputMessage;
import fuzs.iteminteractions.common.impl.network.client.ServerboundSelectedItemMessage;
import fuzs.iteminteractions.common.impl.world.inventory.ContainerSlotHelper;
import fuzs.iteminteractions.common.impl.world.item.container.ItemContentsProviders;
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
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;

/**
 * @see BundleMouseActions
 */
public class ItemContentsMouseActions extends BundleMouseActions {
    private final AbstractContainerScreen<?> screen;

    public ItemContentsMouseActions(AbstractContainerScreen<?> screen) {
        super(screen.minecraft);
        this.screen = screen;
    }

    public static void onAfterInit(AbstractContainerScreen<?> screen, int screenWidth, int screenHeight, List<AbstractWidget> widgets, UnaryOperator<AbstractWidget> addWidget, Consumer<AbstractWidget> removeWidget) {
        screen.itemSlotMouseActions.addFirst(new ItemContentsMouseActions(screen));
    }

    public static boolean extractSingleItemOnly() {
        return ItemInteractions.CONFIG.get(ServerConfig.class).allowPrecisionMode && ItemInteractions.CONFIG.get(
                ClientConfig.class).extractSingleItem.isActive();
    }

    @Override
    public boolean matches(Slot slot) {
        return !ItemContentsProviders.get(slot.getItem()).isEmpty();
    }

    @Override
    public boolean onMouseScrolled(double scrollX, double scrollY, int slotIndex, ItemStack itemStack) {
        if (!ItemInteractions.CONFIG.get(ClientConfig.class).visualItemContents.isActive()) {
            return false;
        }

        if (extractSingleItemOnly()) {
            Vector2i wheelXY = this.scrollWheelHandler.onMouseScroll(scrollX, scrollY);
            int wheel = wheelXY.y == 0 ? -wheelXY.x : wheelXY.y;
            if (wheel != 0) {
                Slot slot = this.screen.hoveredSlot;
                Objects.requireNonNull(slot, "hovered slot is null");
                int buttonNum = this.getMouseButtonFromWheel(wheel);
                this.setSingleItemOnly(true);
                this.screen.slotClicked(slot, slot.index, buttonNum, ContainerInput.PICKUP);
                this.setSingleItemOnly(false);
            }

            return true;
        }

        ItemStorageHolder holder = ItemContentsProviders.get(itemStack);
        if (holder.storage().hasContents(itemStack)) {
            Vector2i wheelXY = this.scrollWheelHandler.onMouseScroll(scrollX, scrollY);
            int wheel = wheelXY.y == 0 ? -wheelXY.x : wheelXY.y;
            if (wheel != 0) {
                int selectedItem = ContainerSlotHelper.getSelectedItem(itemStack);
                Container container = holder.getItemContainerView(itemStack, this.minecraft.player);
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
     * @see MultiPlayerGameMode#ensureHasSentCarriedItem()
     */
    private void setSingleItemOnly(boolean singleItemOnly) {
        // This sets the value on the client, so it's important to call before click actions (notably in the creative menu).
        ContainerSlotHelper.extractSingleItem(this.minecraft.player, singleItemOnly);
        MessageSender.broadcast(new ServerboundContainerClientInputMessage(singleItemOnly));
    }

    @Override
    public void toggleSelectedBundleItem(ItemStack itemStack, int slotIndex, int selectedItem) {
        ContainerSlotHelper.setSelectedItem(itemStack, selectedItem);
        ItemStorageHolder holder = ItemContentsProviders.get(itemStack);
        holder.storage().onToggleSelectedItem(itemStack, selectedItem, selectedItem);
        MessageSender.broadcast(new ServerboundSelectedItemMessage(slotIndex, selectedItem));
    }
}
