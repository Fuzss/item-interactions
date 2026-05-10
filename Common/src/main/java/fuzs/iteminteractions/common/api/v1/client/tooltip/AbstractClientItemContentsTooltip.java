package fuzs.iteminteractions.common.api.v1.client.tooltip;

import fuzs.iteminteractions.common.api.v1.DyeBackedColor;
import fuzs.iteminteractions.common.impl.ItemInteractions;
import fuzs.iteminteractions.common.impl.client.handler.ClientInputActionHandler;
import fuzs.iteminteractions.common.impl.config.ClientConfig;
import fuzs.iteminteractions.common.impl.world.inventory.ContainerSlotHelper;
import fuzs.puzzleslib.common.api.client.gui.v2.tooltip.TooltipRenderHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.ColorLerper;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.DefaultTooltipPositioner;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.mutable.MutableInt;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public abstract class AbstractClientItemContentsTooltip extends ExpandableClientContentsTooltip {
    private static final Identifier CONTAINER_SPRITE = ItemInteractions.id("container/container");
    private static final Identifier SLOT_BLOCKED_SPRITE = ItemInteractions.id("container/slot_blocked");
    private static final Identifier SLOT_SELECTION_SPRITE = ItemInteractions.id("container/slot_selection");
    protected static final int BORDER_SIZE = 7;
    protected static final int SLOT_SIZE = 18;
    private static final MutableInt ACTIVE_CONTAINER_ITEM_TOOLTIPS = new MutableInt();

    private final Minecraft minecraft = Minecraft.getInstance();
    protected final NonNullList<ItemStack> items;
    private final int backgroundColor;

    public AbstractClientItemContentsTooltip(NonNullList<ItemStack> items, @Nullable DyeBackedColor dyeColor) {
        this.items = items;
        this.backgroundColor = getBackgroundColor(dyeColor);
    }

    public static int getBackgroundColor(@Nullable DyeBackedColor color) {
        if (color == null) {
            return -1;
        } else {
            DyeColor dyeColor = DyeColor.byName(color.serialize(), null);
            int colorValue;
            if (dyeColor != null) {
                colorValue = ColorLerper.Type.SHEEP.getColor(dyeColor);
            } else {
                colorValue = color.getValue();
            }

            return ARGB.opaque(colorValue);
        }
    }

    protected abstract int getGridSizeX();

    protected abstract int getGridSizeY();

    protected boolean isSlotBlocked(int itemIndex) {
        return false;
    }

    @Override
    public int getExpandedHeight(Font font) {
        return this.getGridSize(this.getGridSizeY());
    }

    @Override
    public int getExpandedWidth(Font font) {
        return this.getGridSize(this.getGridSizeX());
    }

    protected final int getGridSize(int gridSize) {
        return gridSize * SLOT_SIZE + 2 * BORDER_SIZE;
    }

    @Override
    public void extractExpandedImage(Font font, int x, int y, GuiGraphicsExtractor guiGraphics) {
        ACTIVE_CONTAINER_ITEM_TOOLTIPS.increment();
        guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED,
                CONTAINER_SPRITE,
                x,
                y,
                this.getGridSize(this.getGridSizeX()),
                this.getGridSize(this.getGridSizeY()),
                this.getBackgroundColor());
        int highlightSlot = this.getLastFilledSlot();
        this.drawSlots(guiGraphics, font, x, y, highlightSlot, this::extractBlockedSlots);
        this.drawSlots(guiGraphics, font, x, y, highlightSlot, this::extractSlotContents);
        this.drawSlots(guiGraphics, font, x, y, highlightSlot, this::extractHighlightSlotContents);
        this.drawSelectedSlotTooltip(guiGraphics, font, x, y, highlightSlot);
        ACTIVE_CONTAINER_ITEM_TOOLTIPS.decrement();
    }

    private int getBackgroundColor() {
        return ItemInteractions.CONFIG.get(ClientConfig.class).colorfulTooltips ? this.backgroundColor : -1;
    }

    private void drawSlots(GuiGraphicsExtractor guiGraphics, Font font, int x, int y, int highlightSlot, SlotRenderer slotRenderer) {
        int slotIndex = 0;
        for (int gridY = 0; gridY < this.getGridSizeY(); ++gridY) {
            for (int gridX = 0; gridX < this.getGridSizeX(); ++gridX) {
                int posX = x + gridX * SLOT_SIZE + BORDER_SIZE;
                int posY = y + gridY * SLOT_SIZE + BORDER_SIZE;
                slotRenderer.extractSlot(guiGraphics, font, posX, posY, slotIndex, slotIndex == highlightSlot);
                slotIndex++;
            }
        }
    }

    private void extractBlockedSlots(GuiGraphicsExtractor guiGraphics, Font font, int posX, int posY, int slotIndex, boolean isHighlightSlot) {
        if (this.isSlotBlocked(slotIndex)) {
            guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED,
                    SLOT_BLOCKED_SPRITE,
                    posX,
                    posY,
                    18,
                    18,
                    this.getBackgroundColor());
        }
    }

    private void extractSlotContents(GuiGraphicsExtractor guiGraphics, Font font, int posX, int posY, int slotIndex, boolean isHighlightSlot) {
        if (!isHighlightSlot) {
            this.extractSlot(guiGraphics, font, posX, posY, slotIndex);
        }
    }

    private void extractSlot(GuiGraphicsExtractor guiGraphics, Font font, int posX, int posY, int slotIndex) {
        if (slotIndex < this.items.size()) {
            ItemStack itemstack = this.items.get(slotIndex);
            guiGraphics.item(itemstack, posX + 1, posY + 1, slotIndex);
            guiGraphics.itemDecorations(font, itemstack, posX + 1, posY + 1);
        }
    }

    private void extractHighlightSlotContents(GuiGraphicsExtractor guiGraphics, Font font, int posX, int posY, int slotIndex, boolean isHighlightSlot) {
        if (isHighlightSlot) {
            this.extractSlotHighlight(guiGraphics,
                    posX,
                    posY,
                    SLOT_SELECTION_SPRITE,
                    AbstractContainerScreen.SLOT_HIGHLIGHT_BACK_SPRITE);
            this.extractSlot(guiGraphics, font, posX, posY, slotIndex);
            this.extractSlotHighlight(guiGraphics,
                    posX,
                    posY,
                    null,
                    AbstractContainerScreen.SLOT_HIGHLIGHT_FRONT_SPRITE);
        }
    }

    private void drawSelectedSlotTooltip(GuiGraphicsExtractor guiGraphics, Font font, int mouseX, int mouseY, int highlightSlot) {
        if (!ItemInteractions.CONFIG.get(ClientConfig.class).selectedItemTooltips.isActive()) {
            return;
        }

        if (ACTIVE_CONTAINER_ITEM_TOOLTIPS.intValue() > 1) {
            return;
        }

        if (this.minecraft.screen != null && !this.willTooltipBeMoved(font) && highlightSlot >= 0
                && highlightSlot < this.items.size()) {
            ItemStack itemStack = this.items.get(highlightSlot);
            List<ClientTooltipComponent> tooltipComponents = TooltipRenderHelper.getTooltip(itemStack);
            int maxWidth = tooltipComponents.stream()
                    .mapToInt(tooltipComponent -> tooltipComponent.getWidth(font))
                    .max()
                    .orElse(0);
            if (ACTIVE_CONTAINER_ITEM_TOOLTIPS.intValue() > 0) {
                guiGraphics.tooltip(font,
                        tooltipComponents,
                        mouseX - maxWidth - 2 * SLOT_SIZE,
                        mouseY,
                        DefaultTooltipPositioner.INSTANCE,
                        null);
            } else {
                List<Component> itemTooltip = Screen.getTooltipFromItem(this.minecraft, itemStack);
                Optional<TooltipComponent> itemTooltipImage = itemStack.getTooltipImage();
                guiGraphics.setTooltipForNextFrame(font,
                        itemTooltip,
                        itemTooltipImage,
                        mouseX - maxWidth - 2 * SLOT_SIZE,
                        mouseY);
            }
        }
    }

    private boolean willTooltipBeMoved(Font font) {
        if (!(this.minecraft.screen instanceof AbstractContainerScreen<?> containerScreen)) return false;
        ItemStack stack = ClientInputActionHandler.getContainerItemStack(containerScreen, true);
        if (stack.isEmpty()) return false;
        List<ClientTooltipComponent> tooltipComponents = TooltipRenderHelper.getTooltip(stack);
        int maxWidth = tooltipComponents.stream()
                .mapToInt(tooltipComponent -> tooltipComponent.getWidth(font))
                .max()
                .orElse(0);
        // actual mouseX, tooltip components are passed the adjusted position where the tooltip should be rendered
        int mouseX = (int) (this.minecraft.mouseHandler.xpos() * (double) this.minecraft.getWindow().getGuiScaledWidth()
                / (double) this.minecraft.getWindow().getScreenWidth());
        return mouseX + 12 + maxWidth > containerScreen.width;
    }

    private int getLastFilledSlot() {
        int currentContainerSlot = ContainerSlotHelper.getCurrentContainerSlot(this.minecraft.player);
        if (currentContainerSlot != -1 && currentContainerSlot < this.items.size()) {
            if (!this.items.get(currentContainerSlot).isEmpty()) {
                return currentContainerSlot;
            }
        }

        for (int i = this.items.size() - 1; i >= 0; i--) {
            if (!this.items.get(i).isEmpty()) {
                return i;
            }
        }

        return -1;
    }

    private void extractSlotHighlight(GuiGraphicsExtractor guiGraphics, int posX, int posY, @Nullable Identifier hotbarSelectionSprite, @Nullable Identifier slotHighlightSprite) {
        if (ACTIVE_CONTAINER_ITEM_TOOLTIPS.intValue() > 1) {
            return;
        }

        ClientConfig.SlotOverlay slotOverlay = ItemInteractions.CONFIG.get(ClientConfig.class).slotOverlay;
        switch (slotOverlay) {
            case HOTBAR -> {
                if (hotbarSelectionSprite != null) {
                    guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED,
                            hotbarSelectionSprite,
                            posX - 3,
                            posY - 3,
                            24,
                            24);
                }
            }
            case HOVER -> {
                if (slotHighlightSprite != null) {
                    guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED,
                            slotHighlightSprite,
                            posX - 3,
                            posY - 3,
                            24,
                            24);
                }
            }
        }
    }

    @FunctionalInterface
    private interface SlotRenderer {
        void extractSlot(GuiGraphicsExtractor guiGraphics, Font font, int posX, int posY, int slotIndex, boolean isHighlightSlot);
    }
}
