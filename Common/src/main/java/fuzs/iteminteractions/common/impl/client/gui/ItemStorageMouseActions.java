package fuzs.iteminteractions.common.impl.client.gui;

import com.google.common.collect.Sets;
import com.mojang.blaze3d.platform.InputConstants;
import fuzs.iteminteractions.common.api.v1.world.item.storage.ItemStorageHolder;
import fuzs.iteminteractions.common.impl.ItemInteractions;
import fuzs.iteminteractions.common.impl.config.ClientConfig;
import fuzs.iteminteractions.common.impl.config.ServerConfig;
import fuzs.iteminteractions.common.impl.network.client.ServerboundSelectedItemMessage;
import fuzs.puzzleslib.common.api.network.v4.MessageSender;
import net.minecraft.client.gui.BundleMouseActions;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.joml.Vector2i;
import org.joml.Vector2ic;
import org.jspecify.annotations.Nullable;

import java.util.OptionalInt;
import java.util.Set;

/**
 * @see BundleMouseActions
 */
public class ItemStorageMouseActions extends BundleMouseActions implements CustomItemSlotMouseAction {
    private final AbstractContainerScreen<?> screen;
    private final Set<Slot> clickedDraggingSlots = Sets.newIdentityHashSet();
    private final Set<Slot> allDraggingSlots = Sets.newIdentityHashSet();
    @Nullable
    private ClickAction clickAction;

    public ItemStorageMouseActions(AbstractContainerScreen<?> screen) {
        super(screen.minecraft);
        this.screen = screen;
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
    public void onExtractBackground(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTicks) {
        if (!this.clickedDraggingSlots.isEmpty()) {
            guiGraphics.pose().pushMatrix();
            guiGraphics.pose().translate(this.screen.leftPos, this.screen.topPos);
            this.extractSlotHighlights(guiGraphics, mouseX, mouseY, AbstractContainerScreen.SLOT_HIGHLIGHT_BACK_SPRITE);
            guiGraphics.pose().popMatrix();
        }
    }

    @Override
    public void onExtractContents(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTicks) {
        if (!this.clickedDraggingSlots.isEmpty()) {
            this.extractSlotHighlights(guiGraphics,
                    mouseX,
                    mouseY,
                    AbstractContainerScreen.SLOT_HIGHLIGHT_FRONT_SPRITE);
        }
    }

    private void extractSlotHighlights(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, Identifier slotHighlightSprite) {
        for (Slot slot : this.screen.getMenu().slots) {
            if (slot.isHighlightable() && this.clickedDraggingSlots.contains(slot)) {
                // slots will sometimes be added to dragged slots when simply clicking on a slot, so don't render our overlay then
                if (this.clickedDraggingSlots.size() > 1 || !this.screen.isHovering(slot, mouseX, mouseY)) {
                    guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED,
                            slotHighlightSprite,
                            slot.x - 4,
                            slot.y - 4,
                            24,
                            24);
                }
            }
        }
    }

    @Override
    public boolean onMouseClicked(MouseButtonEvent event, ItemStack itemStack) {
        if (!ItemInteractions.CONFIG.get(ServerConfig.class).enableMouseDragging) {
            return false;
        }

        this.clearDraggingSlots();
        if (ItemStorageHolder.ofItem(itemStack).isPresentFor(itemStack, this.screen.minecraft.player)) {
            Slot slot = this.screen.getHoveredSlot(event.x(), event.y());
            if (slot != null) {
                this.clickAction = event.button() == InputConstants.MOUSE_BUTTON_LEFT ? ClickAction.PRIMARY :
                        ClickAction.SECONDARY;
                return true;
            }
        }

        return false;
    }

    private void clearDraggingSlots() {
        this.clickAction = null;
        this.clickedDraggingSlots.clear();
        this.allDraggingSlots.clear();
    }

    @Override
    public boolean onMouseReleased(MouseButtonEvent event, ItemStack itemStack) {
        if (!ItemInteractions.CONFIG.get(ServerConfig.class).enableMouseDragging) {
            return false;
        }

        ClickAction lastClickAction = this.getLastClickAction();
        boolean handleMouseRelease = this.isMouseReleaseHandled();
        this.clearDraggingSlots();
        if (lastClickAction != null) {
            // Play this manually at the end as we suppress all interaction sounds played while dragging.
            switch (lastClickAction) {
                case PRIMARY -> {
                    ItemStorageHolder.ofItem(itemStack).storage().playInsertSound(this.screen.minecraft.player);
                }
                case SECONDARY -> {
                    ItemStorageHolder.ofItem(itemStack).storage().playRemoveOneSound(this.screen.minecraft.player);
                }
            }
        }

        return handleMouseRelease;
    }

    private @Nullable ClickAction getLastClickAction() {
        return !this.clickedDraggingSlots.isEmpty() ? this.clickAction : null;
    }

    private boolean isMouseReleaseHandled() {
        return this.allDraggingSlots.size() > 1 || !this.clickedDraggingSlots.isEmpty();
    }

    @Override
    public boolean onMouseDragged(MouseButtonEvent event, double dragX, double dragY, ItemStack itemStack) {
        if (!ItemInteractions.CONFIG.get(ServerConfig.class).enableMouseDragging) {
            return false;
        }

        if (this.clickAction != null) {
            ItemStorageHolder holder = ItemStorageHolder.ofItem(itemStack);
            if (!holder.isPresentFor(itemStack, this.screen.minecraft.player)) {
                this.clearDraggingSlots();
                return false;
            }

            Slot slot = this.screen.getHoveredSlot(event.x(), event.y());
            if (slot != null && this.screen.getMenu().canDragTo(slot) && !this.allDraggingSlots.contains(slot)) {
                if (this.shouldSlotBeClicked(this.clickAction, slot, holder, itemStack, this.screen.minecraft.player)) {
                    this.clickedDraggingSlots.add(slot);
                    this.screen.slotClicked(slot, slot.index, event.button(), ContainerInput.PICKUP);
                }

                this.allDraggingSlots.add(slot);
                return true;
            }
        }

        return false;
    }

    private boolean shouldSlotBeClicked(ClickAction clickAction, Slot slot, ItemStorageHolder holder, ItemStack itemStack, Player player) {
        return switch (clickAction) {
            case PRIMARY -> {
                yield slot.hasItem() && holder.canAddItem(itemStack, slot.getItem(), player);
            }
            case SECONDARY -> {
                yield !slot.hasItem() && !holder.getItemContainer(itemStack, player).isEmpty()
                        || ItemInteractions.CONFIG.get(ClientConfig.class).extractSingleItemOnly() && holder.hasAnyOf(
                        itemStack,
                        slot.getItem(),
                        player,
                        true);
            }
        };
    }

    @Override
    public boolean isDragging() {
        return this.clickAction != null;
    }

    @Override
    public boolean onMouseScrolled(double scrollX, double scrollY, int slotIndex, ItemStack itemStack) {
        return CustomItemSlotMouseAction.super.onMouseScrolled(scrollX, scrollY, slotIndex, itemStack);
    }

    @Override
    public boolean onMouseScrolled(double scrollX, double scrollY, OptionalInt slotIndex, ItemStack itemStack) {
        if (!ItemInteractions.CONFIG.get(ClientConfig.class).itemStorageTooltip.isUsed()) {
            return false;
        }

        if (this.screen.hoveredSlot != null && ItemInteractions.CONFIG.get(ClientConfig.class)
                .extractSingleItemOnly()) {
            int wheel = this.onMouseScroll(scrollX, scrollY);
            if (wheel != 0) {
                Slot slot = this.screen.hoveredSlot;
                int buttonNum = this.getMouseButtonFromWheel(wheel);
                this.screen.slotClicked(slot, slot.index, buttonNum, ContainerInput.PICKUP);
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

    private void scrollSelectedItem(ItemStorageHolder holder, OptionalInt slotIndex, ItemStack itemStack, Vector2ic scrollXY) {
        Container container = holder.getItemContainer(itemStack, this.minecraft.player);
        int updatedSelectedItem = holder.storage().scrollSelectedItem(itemStack, container, scrollXY);
        int previousSelectedItem = holder.storage().getSelectedItem(itemStack);
        if (previousSelectedItem != updatedSelectedItem) {
            this.toggleSelectedItem(itemStack, slotIndex, updatedSelectedItem);
        }
    }

    @Override
    public boolean onKeyPressed(KeyEvent event, OptionalInt slotIndex, ItemStack itemStack) {
        if (!ItemInteractions.CONFIG.get(ClientConfig.class).itemStorageTooltip.isUsed()) {
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
