package fuzs.iteminteractions.common.api.v1.client.gui.screens.inventory.tooltip;

import fuzs.iteminteractions.common.api.v1.world.item.DyeBackedColor;
import fuzs.iteminteractions.common.impl.ItemInteractions;
import fuzs.iteminteractions.common.impl.config.ClientConfig;
import net.minecraft.client.color.ColorLerper;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.DefaultTooltipPositioner;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

import java.util.List;

public abstract class ClientItemStorageTooltip implements ClientTooltipComponent {
    private static final Identifier CONTAINER_SPRITE = ItemInteractions.id("container/container");
    private static final Identifier SLOT_BLOCKED_SPRITE = ItemInteractions.id("container/slot_blocked");
    private static final Identifier SLOT_SELECTION_SPRITE = ItemInteractions.id("container/slot_selection");
    protected static final int BORDER_SIZE = 7;
    protected static final int SLOT_SIZE = 18;

    protected final NonNullList<ItemStack> items;
    private final int backgroundColor;
    private final int selectedItem;

    public ClientItemStorageTooltip(NonNullList<ItemStack> items, @Nullable DyeBackedColor dyeColor, int selectedItem) {
        this.items = items;
        this.backgroundColor = getBackgroundColor(dyeColor);
        this.selectedItem = selectedItem;
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
    public int getHeight(Font font) {
        return this.getGridSize(this.getGridSizeY());
    }

    @Override
    public int getWidth(Font font) {
        return this.getGridSize(this.getGridSizeX());
    }

    protected final int getGridSize(int gridSize) {
        return gridSize * SLOT_SIZE + 2 * BORDER_SIZE;
    }

    @Override
    public boolean showTooltipWithItemInHand() {
        return true;
    }

    @Override
    public void extractImage(Font font, int x, int y, int width, int height, GuiGraphicsExtractor guiGraphics) {
        guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED,
                CONTAINER_SPRITE,
                x,
                y,
                this.getGridSize(this.getGridSizeX()),
                this.getGridSize(this.getGridSizeY()),
                this.getBackgroundColor());
        this.extractSlots(font, guiGraphics, x, y, this::extractBlockedSlots);
        this.extractSlots(font, guiGraphics, x, y, this::extractSlotContents);
        this.extractSlots(font, guiGraphics, x, y, this::extractHighlightSlotContents);
        this.extractSelectedItemTooltip(font, guiGraphics, x, y, width);
    }

    private int getBackgroundColor() {
        return ItemInteractions.CONFIG.get(ClientConfig.class).colorfulTooltips ? this.backgroundColor : -1;
    }

    private void extractSlots(Font font, GuiGraphicsExtractor guiGraphics, int x, int y, SlotRenderer slotRenderer) {
        int isSelectedSlot = this.getSelectedSlot();
        int slotIndex = 0;
        for (int gridY = 0; gridY < this.getGridSizeY(); ++gridY) {
            for (int gridX = 0; gridX < this.getGridSizeX(); ++gridX) {
                int posX = x + gridX * SLOT_SIZE + BORDER_SIZE;
                int posY = y + gridY * SLOT_SIZE + BORDER_SIZE;
                slotRenderer.extractSlot(font, guiGraphics, posX, posY, slotIndex, slotIndex == isSelectedSlot);
                slotIndex++;
            }
        }
    }

    private int getSelectedSlot() {
        if (this.selectedItem != -1 && this.selectedItem < this.items.size()) {
            if (!this.items.get(this.selectedItem).isEmpty()) {
                return this.selectedItem;
            }
        }

        for (int i = this.items.size() - 1; i >= 0; i--) {
            if (!this.items.get(i).isEmpty()) {
                return i;
            }
        }

        return -1;
    }

    private void extractBlockedSlots(Font font, GuiGraphicsExtractor guiGraphics, int posX, int posY, int slotIndex, boolean isHighlightSlot) {
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

    private void extractSlotContents(Font font, GuiGraphicsExtractor guiGraphics, int posX, int posY, int slotIndex, boolean isHighlightSlot) {
        if (!isHighlightSlot) {
            this.extractSlot(font, guiGraphics, posX, posY, slotIndex);
        }
    }

    private void extractSlot(Font font, GuiGraphicsExtractor guiGraphics, int posX, int posY, int slotIndex) {
        if (slotIndex < this.items.size()) {
            ItemStack itemstack = this.items.get(slotIndex);
            guiGraphics.item(itemstack, posX + 1, posY + 1, slotIndex);
            guiGraphics.itemDecorations(font, itemstack, posX + 1, posY + 1);
        }
    }

    private void extractHighlightSlotContents(Font font, GuiGraphicsExtractor guiGraphics, int posX, int posY, int slotIndex, boolean isHighlightSlot) {
        if (isHighlightSlot) {
            this.extractSlotHighlight(guiGraphics,
                    posX,
                    posY,
                    SLOT_SELECTION_SPRITE,
                    AbstractContainerScreen.SLOT_HIGHLIGHT_BACK_SPRITE);
            this.extractSlot(font, guiGraphics, posX, posY, slotIndex);
            this.extractSlotHighlight(guiGraphics,
                    posX,
                    posY,
                    null,
                    AbstractContainerScreen.SLOT_HIGHLIGHT_FRONT_SPRITE);
        }
    }

    private void extractSlotHighlight(GuiGraphicsExtractor guiGraphics, int posX, int posY, @Nullable Identifier hotbarSelectionSprite, @Nullable Identifier slotHighlightSprite) {
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

    /**
     * @see net.minecraft.client.gui.screens.inventory.tooltip.ClientBundleTooltip#extractSelectedItemTooltip(Font,
     *         GuiGraphicsExtractor, int, int, int)
     */
    private void extractSelectedItemTooltip(Font font, GuiGraphicsExtractor graphics, int x, int y, int width) {
        ItemStack itemStack = this.getSelectedItem();
        if (!itemStack.isEmpty()) {
            Component selectedItemName = itemStack.getStyledHoverName();
            int textWidth = font.width(selectedItemName.getVisualOrderText());
            int centerTooltip = x + width / 2 - 12;
            ClientTooltipComponent selectedItemNameTooltip = ClientTooltipComponent.create(selectedItemName.getVisualOrderText());
            graphics.tooltip(font,
                    List.of(selectedItemNameTooltip),
                    centerTooltip - textWidth / 2,
                    y - 15,
                    DefaultTooltipPositioner.INSTANCE,
                    itemStack.get(DataComponents.TOOLTIP_STYLE));
        }
    }

    private ItemStack getSelectedItem() {
        int isSelectedSlot = this.getSelectedSlot();
        if (isSelectedSlot >= 0 && isSelectedSlot < this.items.size()) {
            return this.items.get(isSelectedSlot);
        } else {
            return ItemStack.EMPTY;
        }
    }

    @FunctionalInterface
    private interface SlotRenderer {
        void extractSlot(Font font, GuiGraphicsExtractor guiGraphics, int posX, int posY, int slotIndex, boolean isSelectedSlot);
    }
}
