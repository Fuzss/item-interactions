package fuzs.iteminteractions.common.impl.client.gui;

import com.mojang.blaze3d.platform.InputConstants;
import fuzs.iteminteractions.common.api.v1.world.item.storage.ItemStorageHolder;
import fuzs.iteminteractions.common.impl.ItemInteractions;
import fuzs.iteminteractions.common.impl.config.ClientConfig;
import fuzs.iteminteractions.common.impl.init.ModRegistry;
import fuzs.iteminteractions.common.impl.network.client.ServerboundContainerClientInputMessage;
import fuzs.iteminteractions.common.impl.network.client.ServerboundSelectedItemMessage;
import fuzs.puzzleslib.common.api.network.v4.MessageSender;
import net.minecraft.client.gui.BundleMouseActions;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.joml.Vector2i;
import org.joml.Vector2ic;

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
        if (!ItemInteractions.CONFIG.get(ClientConfig.class).itemContentsTooltip.isUsed()) {
            return false;
        }

        // TODO single item-only mode picks up the container item when carried is empty which should not happen
        if (slotIndex.isPresent() && ItemInteractions.CONFIG.get(ClientConfig.class).extractSingleItemOnly()) {
            int wheel = this.onMouseScroll(scrollX, scrollY);
            if (wheel != 0) {
                Slot slot = this.screen.getMenu().getSlot(slotIndex.getAsInt());
                int buttonNum = this.getMouseButtonFromWheel(wheel);
                this.setSingleItemOnly(true);
                this.screen.slotClicked(slot, slot.index, buttonNum, ContainerInput.PICKUP);
                this.setSingleItemOnly(false);
            }

            return true;
        }

        if (itemStack == this.screen.getMenu().getCarried()
                && !ItemInteractions.CONFIG.get(ClientConfig.class).itemHeldByCursorTooltip.isUsed()) {
            return false;
        }

        ItemStorageHolder holder = ItemStorageHolder.ofItem(itemStack);
        if (holder.storage().hasContents(itemStack)) {
            int wheel = this.onMouseScroll(scrollX, scrollY);
            if (wheel != 0) {
                Vector2ic scrollXY;
                if (ItemInteractions.CONFIG.get(ClientConfig.class).verticalTooltipScrolling.isUsed()) {
                    scrollXY = new Vector2i(0, wheel);
                } else {
                    scrollXY = new Vector2i(-wheel, 0);
                }

                this.scrollSelectedItem(holder, slotIndex, itemStack, scrollXY);
            }

            return true;
        }

        return false;
    }

    /**
     * @see net.minecraft.client.MouseHandler#onScroll(long, double, double)
     */
    private int onMouseScroll(double scrollX, double scrollY) {
        Vector2i wheelXY = this.scrollWheelHandler.onMouseScroll(scrollX, scrollY);
        return wheelXY.y == 0 ? -wheelXY.x : wheelXY.y;
    }

    private int getMouseButtonFromWheel(int wheel) {
        if (ItemInteractions.CONFIG.get(ClientConfig.class).reverseSingleItemScrolling ? wheel < 0 : wheel > 0) {
            return InputConstants.MOUSE_BUTTON_RIGHT;
        } else {
            return InputConstants.MOUSE_BUTTON_LEFT;
        }
    }

    /**
     * @see MultiPlayerGameMode#ensureHasSentCarriedItem()
     */
    private void setSingleItemOnly(boolean singleItemOnly) {
        ModRegistry.MOVE_SINGLE_ITEM_ATTACHMENT_TYPE.set(this.minecraft.player, singleItemOnly);
        MessageSender.broadcast(new ServerboundContainerClientInputMessage(singleItemOnly));
    }

    private void scrollSelectedItem(ItemStorageHolder holder, OptionalInt slotIndex, ItemStack itemStack, Vector2ic scrollXY) {
        Container container = holder.getContainerView(itemStack, this.minecraft.player);
        int updatedSelectedItem = holder.storage().scrollSelectedItem(itemStack, container, scrollXY);
        int previousSelectedItem = holder.storage().getSelectedItem(itemStack);
        if (previousSelectedItem != updatedSelectedItem) {
            this.toggleSelectedItem(itemStack, slotIndex, updatedSelectedItem);
        }
    }

    @Override
    public boolean onKeyPressed(KeyEvent event, OptionalInt slotIndex, ItemStack itemStack) {
        if (!ItemInteractions.CONFIG.get(ClientConfig.class).itemContentsTooltip.isUsed()) {
            return false;
        }

        if (itemStack == this.screen.getMenu().getCarried()
                && !ItemInteractions.CONFIG.get(ClientConfig.class).itemHeldByCursorTooltip.isUsed()) {
            return false;
        }

        ItemStorageHolder holder = ItemStorageHolder.ofItem(itemStack);
        if (holder.storage().hasContents(itemStack)) {
            int scrollX = 0;
            int scrollY = 0;
            if (event.isLeft()) {
                scrollX--;
            }

            if (event.isRight()) {
                scrollX++;
            }

            if (event.isUp()) {
                scrollY--;
            }

            if (event.isDown()) {
                scrollY++;
            }

            if (scrollX != 0 || scrollY != 0) {
                this.scrollSelectedItem(holder, slotIndex, itemStack, new Vector2i(scrollX, scrollY));
                return true;
            }
        }

        // Better to not handle this then, as other keys like escape will be blocked.
        return false;
    }

    @Override
    public void toggleSelectedBundleItem(ItemStack bundleItem, int slotIndex, int updatedSelectedItem) {
        this.toggleSelectedItem(bundleItem, OptionalInt.of(slotIndex), updatedSelectedItem);
    }

    private void toggleSelectedItem(ItemStack bundleItem, OptionalInt slotIndex, int updatedSelectedItem) {
        ItemStorageHolder.ofItem(bundleItem).storage().toggleSelectedItem(bundleItem, updatedSelectedItem);
        MessageSender.broadcast(new ServerboundSelectedItemMessage(slotIndex, updatedSelectedItem));
    }
}
