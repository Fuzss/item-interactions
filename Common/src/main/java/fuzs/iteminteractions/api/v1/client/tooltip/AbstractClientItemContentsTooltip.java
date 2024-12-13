package fuzs.iteminteractions.api.v1.client.tooltip;

import fuzs.iteminteractions.impl.ItemInteractions;
import fuzs.iteminteractions.impl.client.handler.ClientInputActionHandler;
import fuzs.iteminteractions.impl.config.ClientConfig;
import fuzs.iteminteractions.impl.world.inventory.ContainerSlotHelper;
import fuzs.puzzleslib.api.client.gui.v2.components.GuiGraphicsHelper;
import fuzs.puzzleslib.api.client.gui.v2.components.TooltipRenderHelper;
import fuzs.puzzleslib.api.core.v1.utility.ResourceLocationHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ARGB;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.mutable.MutableInt;

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

    protected final NonNullList<ItemStack> items;
    private final float[] backgroundColor;

    public AbstractClientItemContentsTooltip(NonNullList<ItemStack> items, float[] backgroundColor) {
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
        int color = this.getBackgroundColor();
        if (this.defaultSize()) {
            ContainerTexture.FULL.blit(guiGraphics, x, y, color);
        } else {
            this.drawBorder(x, y, this.getGridSizeX(), this.getGridSizeY(), guiGraphics);
        }
        int itemIndex = 0;
        int lastFilledSlot = this.getLastFilledSlot();
        for (int l = 0; l < this.getGridSizeY(); ++l) {
            for (int i1 = 0; i1 < this.getGridSizeX(); ++i1) {
                int posX = x + i1 * GRID_SIZE + BORDER_SIZE;
                int posY = y + l * GRID_SIZE + BORDER_SIZE;
                if (!this.defaultSize()) {
                    if (this.isSlotBlocked(itemIndex)) {
                        ContainerTexture.BLOCKED_SLOT.blit(guiGraphics, posX, posY, color);
                    } else {
                        ContainerTexture.SLOT.blit(guiGraphics, posX, posY, color);
                    }
                }
                if (itemIndex == lastFilledSlot) {
                    this.drawSlotHighlightBack(guiGraphics, posX, posY);
                }
                this.drawSlot(guiGraphics, posX, posY, itemIndex, font);
                if (itemIndex == lastFilledSlot) {
                    this.drawSlotHighlightFront(guiGraphics, posX, posY);
                }
                itemIndex++;
            }
        }
        this.drawSelectedSlotTooltip(font, x, y, guiGraphics, lastFilledSlot);
        ACTIVE_CONTAINER_ITEM_TOOLTIPS.decrement();
    }

    private void drawSelectedSlotTooltip(Font font, int mouseX, int mouseY, GuiGraphics guiGraphics, int lastFilledSlot) {
        if (!ItemInteractions.CONFIG.get(ClientConfig.class).selectedItemTooltips.isActive()) return;
        if (ACTIVE_CONTAINER_ITEM_TOOLTIPS.intValue() > 1) return;
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.screen != null && !willTooltipBeMoved(minecraft, font, mouseX, mouseY) && lastFilledSlot >= 0 &&
                lastFilledSlot < this.items.size()) {
            ItemStack itemStack = this.items.get(lastFilledSlot);
            List<Component> itemTooltip = Screen.getTooltipFromItem(minecraft, itemStack);
            Optional<TooltipComponent> itemTooltipImage = itemStack.getTooltipImage();
            List<ClientTooltipComponent> tooltipComponents = TooltipRenderHelper.getTooltip(itemStack);
            int maxWidth = tooltipComponents.stream()
                    .mapToInt(tooltipComponent -> tooltipComponent.getWidth(font))
                    .max()
                    .orElse(0);
            guiGraphics.pose().pushPose();
            guiGraphics.renderTooltip(font, itemTooltip, itemTooltipImage, mouseX - maxWidth - 2 * GRID_SIZE, mouseY);
            guiGraphics.pose().popPose();
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
        mouseX = (int) (minecraft.mouseHandler.xpos() * (double) minecraft.getWindow().getGuiScaledWidth() /
                (double) minecraft.getWindow().getScreenWidth());
        return mouseX + 12 + maxWidth > containerScreen.width;
    }

    private boolean defaultSize() {
        // this is by far the most common size, we use a pre-built image for that
        return this.getGridSizeX() == 9 && this.getGridSizeY() == 3;
    }

    private int getBackgroundColor() {
        if (!ItemInteractions.CONFIG.get(ClientConfig.class).colorfulTooltips) {
            return -1;
        } else {
            return ARGB.colorFromFloat(1.0F, this.backgroundColor[0], this.backgroundColor[1], this.backgroundColor[2]);
        }
    }

    private int getLastFilledSlot() {
        Minecraft minecraft = Minecraft.getInstance();
        int currentContainerSlot = ContainerSlotHelper.getCurrentContainerSlot(minecraft.player);
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
        ContainerTexture.BORDER_TOP_RIGHT.blit(guiGraphics, mouseX + gridSizeX * GRID_SIZE + BORDER_SIZE, mouseY, color);

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

        ContainerTexture.BORDER_BOTTOM_LEFT.blit(guiGraphics, mouseX, mouseY + gridSizeY * GRID_SIZE + BORDER_SIZE, color);
        ContainerTexture.BORDER_BOTTOM_RIGHT.blit(guiGraphics,
                mouseX + gridSizeX * GRID_SIZE + BORDER_SIZE,
                mouseY + gridSizeY * GRID_SIZE + BORDER_SIZE,
                color);
    }

    private void drawSlot(GuiGraphics guiGraphics, int posX, int posY, int itemIndex, Font font) {
        if (itemIndex < this.items.size()) {
            ItemStack itemstack = this.items.get(itemIndex);
            guiGraphics.renderItem(itemstack, posX + 1, posY + 1, itemIndex);
            guiGraphics.renderItemDecorations(font, itemstack, posX + 1, posY + 1);
        }
    }

    private void drawSlotHighlightBack(GuiGraphics guiGraphics, int posX, int posY) {
        if (ACTIVE_CONTAINER_ITEM_TOOLTIPS.intValue() > 1) return;
        ClientConfig.SlotOverlay slotOverlay = ItemInteractions.CONFIG.get(ClientConfig.class).slotOverlay;
        if (slotOverlay == ClientConfig.SlotOverlay.HOVER) {
            guiGraphics.blitSprite(RenderType::guiTextured, SLOT_HIGHLIGHT_BACK_SPRITE, posX - 3, posY - 3, 24, 24);
        }
    }

    private void drawSlotHighlightFront(GuiGraphics guiGraphics, int posX, int posY) {
        if (ACTIVE_CONTAINER_ITEM_TOOLTIPS.intValue() > 1) return;
        ClientConfig.SlotOverlay slotOverlay = ItemInteractions.CONFIG.get(ClientConfig.class).slotOverlay;
        switch (slotOverlay) {
            case HOTBAR -> {
                // items render at 150 (or 160 for some 3d models in the recipe book)
                // item decorations (such as item stack count) render at 200
                // we want to slip in-between
                guiGraphics.pose().pushPose();
                guiGraphics.pose().translate(0.0F, 0.0F, 180.0F);
                GuiGraphicsHelper.blitTiledSprite(guiGraphics,
                        RenderType::guiTextured,
                        HOTBAR_SELECTION_SPRITE,
                        posX - 3,
                        posY - 3,
                        24,
                        24,
                        24,
                        23);
                guiGraphics.pose().popPose();
            }
            case HOVER -> guiGraphics.blitSprite(RenderType::guiTexturedOverlay,
                    SLOT_HIGHLIGHT_FRONT_SPRITE,
                    posX - 3,
                    posY - 3,
                    24,
                    24);
        }
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
            guiGraphics.blit(RenderType::guiTextured,
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