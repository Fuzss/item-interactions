package fuzs.iteminteractions.common.api.v1.client.gui.screens.inventory.tooltip;

import fuzs.iteminteractions.common.api.v1.world.inventory.tooltip.ItemContentsTooltip;
import fuzs.iteminteractions.common.api.v1.world.item.DyeBackedColor;
import fuzs.iteminteractions.common.impl.ItemInteractions;
import fuzs.iteminteractions.common.impl.config.ClientConfig;
import fuzs.iteminteractions.common.impl.config.SlotHighlight;
import net.minecraft.client.color.ColorLerper;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientBundleTooltip;
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

public class ClientItemContentsTooltip implements ClientTooltipComponent {
    public static final Identifier CONTAINER_SPRITE = ItemInteractions.id("container/container");
    public static final Identifier SLOT_SELECTION_SPRITE = ItemInteractions.id("container/slot_selection");
    private static final int BORDER_SIZE = 7;
    private static final int SLOT_SIZE = 18;

    private final NonNullList<ItemStack> itemList;
    private final int selectedItem;
    private final int gridWidth;
    private final int gridHeight;
    private final int backgroundColor;

    public ClientItemContentsTooltip(ItemContentsTooltip tooltip) {
        this.itemList = tooltip.itemList();
        this.selectedItem = tooltip.selectedItem();
        this.gridWidth = tooltip.gridWidth();
        this.gridHeight = tooltip.gridHeight();
        this.backgroundColor = computeBackgroundColor(tooltip.dyeColor());
    }

    public static int computeBackgroundColor(@Nullable DyeBackedColor color) {
        if (!ItemInteractions.CONFIG.get(ClientConfig.class).colorfulTooltips || color == null) {
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

    @Override
    public int getHeight(Font font) {
        return this.getGridDimension(this.gridSizeY());
    }

    @Override
    public int getWidth(Font font) {
        return this.getGridDimension(this.gridSizeX());
    }

    private int getGridDimension(int size) {
        return size * SLOT_SIZE + 2 * BORDER_SIZE;
    }

    public int gridSizeX() {
        return this.gridWidth;
    }

    public int gridSizeY() {
        return this.gridHeight;
    }

    @Override
    public boolean showTooltipWithItemInHand() {
        return !ItemInteractions.CONFIG.get(ClientConfig.class).carriedItemTooltips.isActive();
    }

    @SuppressWarnings("UnnecessaryLocalVariable")
    @Override
    public void extractImage(Font font, int x, int y, int w, int h, GuiGraphicsExtractor guiGraphics) {
        int spriteWidth = this.getGridDimension(this.gridSizeX());
        int spriteHeight = this.getGridDimension(this.gridSizeY());
        int xStartPos = x + this.getContentXOffset(font, w);
        int yStartPos = y;
        guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED,
                CONTAINER_SPRITE,
                xStartPos,
                yStartPos,
                spriteWidth,
                spriteHeight,
                this.backgroundColor);
        this.extractSlots(font, guiGraphics, xStartPos, yStartPos, this::extractSlotContents);
        this.extractSlots(font, guiGraphics, xStartPos, yStartPos, this::extractHighlightSlotContents);
        this.extractSelectedItemTooltip(font, guiGraphics, x, y, w);
    }

    /**
     * @see ClientBundleTooltip#getContentXOffset(int)
     */
    public int getContentXOffset(Font font, int tooltipWidth) {
        return (tooltipWidth - this.getWidth(font)) / 2;
    }

    private void extractSlots(Font font, GuiGraphicsExtractor guiGraphics, int x, int y, SlotRenderer slotRenderer) {
        int slotIndex = 0;
        for (int gridY = 0; gridY < this.gridSizeY(); ++gridY) {
            for (int gridX = 0; gridX < this.gridSizeX(); ++gridX) {
                int posX = x + gridX * SLOT_SIZE + BORDER_SIZE;
                int posY = y + gridY * SLOT_SIZE + BORDER_SIZE;
                slotRenderer.extractSlot(font, guiGraphics, posX, posY, slotIndex, slotIndex == this.selectedItem);
                slotIndex++;
            }
        }
    }

    private void extractSlotContents(Font font, GuiGraphicsExtractor guiGraphics, int posX, int posY, int slotIndex, boolean isHighlightSlot) {
        if (!isHighlightSlot) {
            this.extractSlot(font, guiGraphics, posX, posY, slotIndex);
        }
    }

    private void extractSlot(Font font, GuiGraphicsExtractor guiGraphics, int posX, int posY, int slotIndex) {
        if (slotIndex < this.itemList.size()) {
            ItemStack itemstack = this.itemList.get(slotIndex);
            guiGraphics.item(itemstack, posX + 1, posY + 1, slotIndex);
            guiGraphics.itemDecorations(font, itemstack, posX + 1, posY + 1);
        }
    }

    private void extractHighlightSlotContents(Font font, GuiGraphicsExtractor guiGraphics, int posX, int posY, int slotIndex, boolean isHighlightSlot) {
        if (isHighlightSlot) {
            SlotHighlight slotHighlight = ItemInteractions.CONFIG.get(ClientConfig.class).slotHighlight;
            slotHighlight.blitBackSprite(guiGraphics, posX, posY);
            this.extractSlot(font, guiGraphics, posX, posY, slotIndex);
            slotHighlight.blitFrontSprite(guiGraphics, posX, posY);
        }
    }

    /**
     * @see ClientBundleTooltip#extractSelectedItemTooltip(Font, GuiGraphicsExtractor, int, int, int)
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
        if (this.selectedItem >= 0 && this.selectedItem < this.itemList.size()) {
            return this.itemList.get(this.selectedItem);
        } else {
            return ItemStack.EMPTY;
        }
    }

    @FunctionalInterface
    private interface SlotRenderer {
        void extractSlot(Font font, GuiGraphicsExtractor guiGraphics, int posX, int posY, int slotIndex, boolean isSelectedSlot);
    }
}
