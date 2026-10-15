package fuzs.iteminteractions.common.api.v2.client.gui.screens.inventory.tooltip;

import fuzs.iteminteractions.common.api.v2.world.inventory.tooltip.ItemContentsTooltip;
import fuzs.iteminteractions.common.impl.ItemInteractions;
import fuzs.iteminteractions.common.impl.client.helper.ItemStorageTooltipHelper;
import fuzs.iteminteractions.common.impl.config.ClientConfig;
import fuzs.iteminteractions.common.impl.config.SlotHighlight;
import fuzs.iteminteractions.common.impl.world.item.component.SelectedItem;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientBundleTooltip;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.world.item.ItemStack;

import java.util.stream.Stream;

public class ClientItemContentsTooltip implements ClientItemStorageTooltip {
    public static final Identifier CONTAINER_SPRITE = ItemInteractions.id("container/container");
    public static final Identifier SLOT_SELECTION_SPRITE = ItemInteractions.id("container/slot_selection");
    private static final int BORDER_SIZE = 7;
    private static final int SLOT_SIZE = 18;

    private final NonNullList<ItemStack> itemList;
    private final int selectedItem;
    private final int gridWidth;
    private final int gridHeight;
    private final int backgroundColor;
    private final boolean overflow;
    private final boolean combined;

    public ClientItemContentsTooltip(ItemContentsTooltip tooltip) {
        this(tooltip.itemList(),
                tooltip.selectedItem(),
                tooltip.gridWidth(),
                tooltip.gridHeight(),
                tooltip.backgroundColor(),
                false,
                false);
    }

    private ClientItemContentsTooltip(NonNullList<ItemStack> itemList, int selectedItem, int gridWidth, int gridHeight, int backgroundColor, boolean overflow, boolean combined) {
        this.itemList = itemList;
        this.selectedItem = selectedItem;
        this.gridWidth = gridWidth;
        this.gridHeight = gridHeight;
        this.backgroundColor = backgroundColor;
        this.overflow = overflow;
        this.combined = combined;
    }

    @Override
    public int getWidth(Font font) {
        return this.getComponentDimension(this.gridSizeX());
    }

    @Override
    public int getHeight(Font font) {
        return this.getComponentDimension(this.gridSizeY());
    }

    private int getComponentDimension(int size) {
        return size * SLOT_SIZE + 2 * BORDER_SIZE;
    }

    @Override
    public int gridSizeX() {
        return this.gridWidth;
    }

    @Override
    public int gridSizeY() {
        return this.gridHeight;
    }

    @Override
    public boolean showTooltipWithItemInHand() {
        return true;
    }

    @SuppressWarnings("UnnecessaryLocalVariable")
    @Override
    public void extractImage(Font font, int x, int y, int w, int h, GuiGraphicsExtractor guiGraphics) {
        int xStartPos = x + this.getContentXOffset(font, w);
        int yStartPos = y;
        this.extractSprite(font, guiGraphics, xStartPos, yStartPos);
        this.extractSlots(font, guiGraphics, xStartPos, yStartPos, this::extractSlotContents);
        if (this.selectedItem != SelectedItem.DEFAULT_SELECTED_ITEM) {
            this.extractSlots(font, guiGraphics, xStartPos, yStartPos, this::extractHighlightSlotContents);
        }

        if (this.overflow) {
            this.extractSlots(font, guiGraphics, xStartPos, yStartPos, this::extractOverflowSlotContents);
        }

        this.extractSelectedItemTooltip(font, guiGraphics, x, y, w);
    }

    /**
     * @see ClientBundleTooltip#getContentXOffset(int)
     */
    public int getContentXOffset(Font font, int tooltipWidth) {
        return (tooltipWidth - this.getWidth(font)) / 2;
    }

    private void extractSprite(Font font, GuiGraphicsExtractor guiGraphics, int startX, int startY) {
        guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED,
                CONTAINER_SPRITE,
                startX,
                startY,
                this.getWidth(font),
                this.getHeight(font),
                this.getBackgroundColor());
    }

    private int getBackgroundColor() {
        return ItemInteractions.CONFIG.get(ClientConfig.class).itemStorageColors ? ARGB.opaque(this.backgroundColor) :
                -1;
    }

    private void extractSlots(Font font, GuiGraphicsExtractor guiGraphics, int x, int y, SlotRenderer slotRenderer) {
        int slotNum = 0;
        for (int gridY = 0; gridY < this.gridSizeY(); ++gridY) {
            for (int gridX = 0; gridX < this.gridSizeX(); ++gridX) {
                int posX = x + this.getSlotStart(gridX);
                int posY = y + this.getSlotStart(gridY);
                slotRenderer.extractSlot(font, guiGraphics, posX, posY, slotNum);
                slotNum++;
            }
        }
    }

    private int getSlotStart(int gridDimension) {
        return gridDimension * SLOT_SIZE + BORDER_SIZE;
    }

    private void extractSlot(Font font, GuiGraphicsExtractor guiGraphics, int posX, int posY, int slotNum) {
        if (slotNum < this.itemList.size()) {
            ItemStack item = this.itemList.get(slotNum);
            guiGraphics.item(item, posX + 1, posY + 1, slotNum);
            if (this.combined) {
                ItemStorageTooltipHelper.itemDecorations(guiGraphics, font, item, posX + 1, posY + 1);
            } else {
                guiGraphics.itemDecorations(font, item, posX + 1, posY + 1);
            }
        }
    }

    private void extractSlotContents(Font font, GuiGraphicsExtractor guiGraphics, int posX, int posY, int slotNum) {
        if (!this.isHighlightSlot(slotNum) && !this.isOverflowSlot(slotNum)) {
            this.extractSlot(font, guiGraphics, posX, posY, slotNum);
        }
    }

    private void extractHighlightSlotContents(Font font, GuiGraphicsExtractor guiGraphics, int posX, int posY, int slotNum) {
        if (this.isHighlightSlot(slotNum) && !this.isOverflowSlot(slotNum)) {
            SlotHighlight slotHighlight = ItemInteractions.CONFIG.get(ClientConfig.class).itemStorageHighlightSprite;
            slotHighlight.blitBackSprite(guiGraphics, posX, posY);
            this.extractSlot(font, guiGraphics, posX, posY, slotNum);
            slotHighlight.blitFrontSprite(guiGraphics, posX, posY);
        }
    }

    private void extractOverflowSlotContents(Font font, GuiGraphicsExtractor guiGraphics, int posX, int posY, int slotNum) {
        if (this.isOverflowSlot(slotNum)) {
            guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED,
                    ClientItemStorageTooltip.ELLIPSIS_SPRITE,
                    posX + 1,
                    posY + 1,
                    16,
                    16);
        }
    }

    private boolean isHighlightSlot(int slotNum) {
        return slotNum == this.selectedItem;
    }

    private boolean isOverflowSlot(int slotNum) {
        return this.overflow && slotNum == this.itemList.size() - 1;
    }

    @Override
    public Stream<ItemStack> getAllItems() {
        return this.itemList.stream();
    }

    @Override
    public ItemStack getSelectedItem() {
        if (this.selectedItem >= 0 && this.selectedItem < this.itemList.size()) {
            return this.itemList.get(this.selectedItem);
        } else {
            return ItemStack.EMPTY;
        }
    }

    @Override
    public ClientTooltipComponent createItemPreview(NonNullList<ItemStack> previewItems, int gridWidth, int gridHeight, boolean overflow, boolean combined) {
        return new ClientItemContentsTooltip(previewItems,
                SelectedItem.DEFAULT_SELECTED_ITEM,
                gridWidth,
                gridHeight,
                this.backgroundColor,
                overflow,
                combined);
    }

    @FunctionalInterface
    private interface SlotRenderer {
        void extractSlot(Font font, GuiGraphicsExtractor guiGraphics, int posX, int posY, int slotNum);
    }
}
