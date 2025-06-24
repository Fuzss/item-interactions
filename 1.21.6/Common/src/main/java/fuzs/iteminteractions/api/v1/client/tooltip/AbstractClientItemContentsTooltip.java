package fuzs.iteminteractions.api.v1.client.tooltip;

import fuzs.iteminteractions.impl.ItemInteractions;
import fuzs.iteminteractions.impl.client.handler.ClientInputActionHandler;
import fuzs.iteminteractions.impl.config.ClientConfig;
import fuzs.iteminteractions.impl.world.inventory.ContainerSlotHelper;
import fuzs.puzzleslib.api.client.gui.v2.GuiGraphicsHelper;
import fuzs.puzzleslib.api.client.gui.v2.tooltip.TooltipRenderHelper;
import fuzs.puzzleslib.api.core.v1.utility.ResourceLocationHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.mutable.MutableInt;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public abstract class AbstractClientItemContentsTooltip extends ExpandableClientContentsTooltip {
    private static final ResourceLocation HOTBAR_SELECTION_SPRITE = ResourceLocationHelper.withDefaultNamespace(
            "hud/hotbar_selection");
    public static final ResourceLocation TEXTURE_LOCATION = ItemInteractions.id(
            "textures/gui/container/inventory_tooltip.png");
    private static final ResourceLocation SLOT_HIGHLIGHT_BACK_SPRITE = ResourceLocation.withDefaultNamespace(
            "container/slot_highlight_back");
    private static final ResourceLocation SLOT_HIGHLIGHT_FRONT_SPRITE = ResourceLocation.withDefaultNamespace(
            "container/slot_highlight_front");
    protected static final int BORDER_SIZE = 7;
    protected static final int GRID_SIZE = 18;
    private static final MutableInt ACTIVE_CONTAINER_ITEM_TOOLTIPS = new MutableInt();

    private final Minecraft minecraft = Minecraft.getInstance();
    protected final NonNullList<ItemStack> items;
    private final int backgroundColor;

    public AbstractClientItemContentsTooltip(NonNullList<ItemStack> items, int backgroundColor) {
        this.items = items;
        this.backgroundColor = backgroundColor;
    }

    protected abstract int getGridSizeX();

    protected abstract int getGridSizeY();

    protected boolean isSlotBlocked(int itemIndex) {
        return false;
    }

    @Override
    public int getExpandedHeight(Font font) {
        return getGridSize(this.getGridSizeY());
    }

    @Override
    public int getExpandedWidth(Font font) {
        return getGridSize(this.getGridSizeX());
    }

    protected static int getGridSize(int gridSize) {
        return gridSize * GRID_SIZE + 2 * BORDER_SIZE;
    }

    @Override
    public void renderExpandedImage(Font font, int x, int y, GuiGraphics guiGraphics) {
        ACTIVE_CONTAINER_ITEM_TOOLTIPS.increment();
        if (this.defaultSize()) {
            ContainerTexture.FULL.blit(guiGraphics, x, y, this.getBackgroundColor());
        } else {
            this.drawBorder(x, y, this.getGridSizeX(), this.getGridSizeY(), guiGraphics);
        }
        int highlightSlot = this.getLastFilledSlot();
        if (!this.defaultSize()) {
            this.drawSlots(guiGraphics, font, x, y, highlightSlot, this::drawBackgroundSlots);
        }
        this.drawSlots(guiGraphics, font, x, y, highlightSlot, this::drawSlotContents);
        this.drawSlots(guiGraphics, font, x, y, highlightSlot, this::drawHighlightSlotContents);
        this.drawSelectedSlotTooltip(guiGraphics, font, x, y, highlightSlot);
        ACTIVE_CONTAINER_ITEM_TOOLTIPS.decrement();
    }

    private boolean defaultSize() {
        // this is by far the most common size, we use a pre-built image for that
        return this.getGridSizeX() == 9 && this.getGridSizeY() == 3;
    }

    private int getBackgroundColor() {
        return ItemInteractions.CONFIG.get(ClientConfig.class).colorfulTooltips ? this.backgroundColor : -1;
    }

    private void drawSlots(GuiGraphics guiGraphics, Font font, int x, int y, int highlightSlot, SlotRenderer slotRenderer) {
        int slotIndex = 0;
        for (int gridY = 0; gridY < this.getGridSizeY(); ++gridY) {
            for (int gridX = 0; gridX < this.getGridSizeX(); ++gridX) {
                int posX = x + gridX * GRID_SIZE + BORDER_SIZE;
                int posY = y + gridY * GRID_SIZE + BORDER_SIZE;
                slotRenderer.drawSlot(guiGraphics, font, posX, posY, slotIndex, slotIndex == highlightSlot);
                slotIndex++;
            }
        }
    }

    private void drawBackgroundSlots(GuiGraphics guiGraphics, Font font, int posX, int posY, int slotIndex, boolean isHighlightSlot) {
        if (this.isSlotBlocked(slotIndex)) {
            ContainerTexture.BLOCKED_SLOT.blit(guiGraphics, posX, posY, this.getBackgroundColor());
        } else {
            ContainerTexture.SLOT.blit(guiGraphics, posX, posY, this.getBackgroundColor());
        }
    }

    private void drawSlotContents(GuiGraphics guiGraphics, Font font, int posX, int posY, int slotIndex, boolean isHighlightSlot) {
        if (!isHighlightSlot) {
            this.drawSlot(guiGraphics, font, posX, posY, slotIndex);
        }
    }

    private void drawSlot(GuiGraphics guiGraphics, Font font, int posX, int posY, int slotIndex) {
        if (slotIndex < this.items.size()) {
            ItemStack itemstack = this.items.get(slotIndex);
            guiGraphics.renderItem(itemstack, posX + 1, posY + 1, slotIndex);
            guiGraphics.renderItemDecorations(font, itemstack, posX + 1, posY + 1);
        }
    }

    private void drawHighlightSlotContents(GuiGraphics guiGraphics, Font font, int posX, int posY, int slotIndex, boolean isHighlightSlot) {
        if (isHighlightSlot) {
            this.drawSlotHighlight(guiGraphics, posX, posY, HOTBAR_SELECTION_SPRITE, SLOT_HIGHLIGHT_BACK_SPRITE);
            this.drawSlot(guiGraphics, font, posX, posY, slotIndex);
            this.drawSlotHighlight(guiGraphics, posX, posY, null, SLOT_HIGHLIGHT_FRONT_SPRITE);
        }
    }

    private void drawSelectedSlotTooltip(GuiGraphics guiGraphics, Font font, int mouseX, int mouseY, int highlightSlot) {
        if (!ItemInteractions.CONFIG.get(ClientConfig.class).selectedItemTooltips.isActive()) return;
        if (ACTIVE_CONTAINER_ITEM_TOOLTIPS.intValue() > 1) return;
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.screen != null && !willTooltipBeMoved(minecraft, font, mouseX, mouseY) && highlightSlot >= 0
                && highlightSlot < this.items.size()) {
            ItemStack itemStack = this.items.get(highlightSlot);
            List<Component> itemTooltip = Screen.getTooltipFromItem(minecraft, itemStack);
            Optional<TooltipComponent> itemTooltipImage = itemStack.getTooltipImage();
            List<ClientTooltipComponent> tooltipComponents = TooltipRenderHelper.getTooltip(itemStack);
            int maxWidth = tooltipComponents.stream()
                    .mapToInt(tooltipComponent -> tooltipComponent.getWidth(font))
                    .max()
                    .orElse(0);
            guiGraphics.setTooltipForNextFrame(font,
                    itemTooltip,
                    itemTooltipImage,
                    mouseX - maxWidth - 2 * GRID_SIZE,
                    mouseY);
        }
    }

    private static boolean willTooltipBeMoved(Minecraft minecraft, Font font, int mouseX, int mouseY) {
        if (!(minecraft.screen instanceof AbstractContainerScreen<?> containerScreen)) return false;
        ItemStack stack = ClientInputActionHandler.getContainerItemStack(containerScreen, true);
        if (stack.isEmpty()) return false;
        List<ClientTooltipComponent> tooltipComponents = TooltipRenderHelper.getTooltip(stack);
        int maxWidth = tooltipComponents.stream()
                .mapToInt(tooltipComponent -> tooltipComponent.getWidth(font))
                .max()
                .orElse(0);
        // actual mouseX, tooltip components are passed the adjusted position where the tooltip should be rendered
        mouseX = (int) (minecraft.mouseHandler.xpos() * (double) minecraft.getWindow().getGuiScaledWidth()
                / (double) minecraft.getWindow().getScreenWidth());
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

    private void drawBorder(int mouseX, int mouseY, int gridSizeX, int gridSizeY, GuiGraphics guiGraphics) {
        int color = this.getBackgroundColor();
        ContainerTexture.BORDER_TOP_LEFT.blit(guiGraphics, mouseX, mouseY, color);
        ContainerTexture.BORDER_TOP_RIGHT.blit(guiGraphics,
                mouseX + gridSizeX * GRID_SIZE + BORDER_SIZE,
                mouseY,
                color);

        for (int i = 0; i < gridSizeX; ++i) {
            ContainerTexture.BORDER_TOP.blit(guiGraphics, mouseX + BORDER_SIZE + i * GRID_SIZE, mouseY, color);
            ContainerTexture.BORDER_BOTTOM.blit(guiGraphics,
                    mouseX + BORDER_SIZE + i * GRID_SIZE,
                    mouseY + gridSizeY * GRID_SIZE + BORDER_SIZE,
                    color);
        }

        for (int j = 0; j < gridSizeY; ++j) {
            ContainerTexture.BORDER_LEFT.blit(guiGraphics, mouseX, mouseY + j * GRID_SIZE + BORDER_SIZE, color);
            ContainerTexture.BORDER_RIGHT.blit(guiGraphics,
                    mouseX + gridSizeX * GRID_SIZE + BORDER_SIZE,
                    mouseY + j * GRID_SIZE + BORDER_SIZE,
                    color);
        }

        ContainerTexture.BORDER_BOTTOM_LEFT.blit(guiGraphics,
                mouseX,
                mouseY + gridSizeY * GRID_SIZE + BORDER_SIZE,
                color);
        ContainerTexture.BORDER_BOTTOM_RIGHT.blit(guiGraphics,
                mouseX + gridSizeX * GRID_SIZE + BORDER_SIZE,
                mouseY + gridSizeY * GRID_SIZE + BORDER_SIZE,
                color);
    }

    private void drawSlotHighlight(GuiGraphics guiGraphics, int posX, int posY, @Nullable ResourceLocation hotbarSelectionSprite, @Nullable ResourceLocation slotHighlightSprite) {
        if (ACTIVE_CONTAINER_ITEM_TOOLTIPS.intValue() > 1) return;
        ClientConfig.SlotOverlay slotOverlay = ItemInteractions.CONFIG.get(ClientConfig.class).slotOverlay;
        switch (slotOverlay) {
            case HOTBAR -> {
                if (hotbarSelectionSprite != null) {
                    GuiGraphicsHelper.blitTiledSprite(guiGraphics,
                            RenderPipelines.GUI_TEXTURED,
                            hotbarSelectionSprite,
                            posX - 3,
                            posY - 3,
                            24,
                            24,
                            24,
                            23);
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

        void drawSlot(GuiGraphics guiGraphics, Font font, int posX, int posY, int slotIndex, boolean isHighlightSlot);
    }

    private enum ContainerTexture {
        SLOT(BORDER_SIZE, BORDER_SIZE, 18, 18),
        BLOCKED_SLOT(BORDER_SIZE * 3 + 18, BORDER_SIZE, 18, 18),
        BORDER_TOP_LEFT(0, 0, BORDER_SIZE, BORDER_SIZE),
        BORDER_TOP(BORDER_SIZE, 0, 18, BORDER_SIZE),
        BORDER_TOP_RIGHT(18 + BORDER_SIZE, 0, BORDER_SIZE, BORDER_SIZE),
        BORDER_RIGHT(18 + BORDER_SIZE, BORDER_SIZE, BORDER_SIZE, 18),
        BORDER_BOTTOM_RIGHT(18 + BORDER_SIZE, 18 + BORDER_SIZE, BORDER_SIZE, BORDER_SIZE),
        BORDER_BOTTOM(BORDER_SIZE, 18 + BORDER_SIZE, 18, BORDER_SIZE),
        BORDER_BOTTOM_LEFT(0, 18 + BORDER_SIZE, BORDER_SIZE, BORDER_SIZE),
        BORDER_LEFT(0, BORDER_SIZE, BORDER_SIZE, 18),
        FULL(0, 18 + 2 * BORDER_SIZE, 18 * 9 + 2 * BORDER_SIZE, 18 * 3 + 2 * BORDER_SIZE);

        public final int textureX;
        public final int textureY;
        public final int width;
        public final int height;

        ContainerTexture(int textureX, int textureY, int width, int height) {
            this.textureX = textureX;
            this.textureY = textureY;
            this.width = width;
            this.height = height;
        }

        public void blit(GuiGraphics guiGraphics, int posX, int posY, int color) {
            guiGraphics.blit(RenderPipelines.GUI_TEXTURED,
                    TEXTURE_LOCATION,
                    posX,
                    posY,
                    this.textureX,
                    this.textureY,
                    this.width,
                    this.height,
                    256,
                    256,
                    color);
        }
    }
}