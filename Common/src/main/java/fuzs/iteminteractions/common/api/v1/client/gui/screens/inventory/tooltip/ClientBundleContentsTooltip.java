package fuzs.iteminteractions.common.api.v1.client.gui.screens.inventory.tooltip;

import fuzs.iteminteractions.common.api.v1.world.inventory.tooltip.BundleContentsTooltip;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import org.apache.commons.lang3.math.Fraction;

/**
 * @see net.minecraft.client.gui.screens.inventory.tooltip.ClientBundleTooltip
 */
public class ClientBundleContentsTooltip extends ClientItemStorageTooltip {
    private static final Identifier PROGRESSBAR_BORDER_SPRITE = Identifier.withDefaultNamespace(
            "container/bundle/bundle_progressbar_border");
    private static final Identifier PROGRESSBAR_FILL_SPRITE = Identifier.withDefaultNamespace(
            "container/bundle/bundle_progressbar_fill");
    private static final Identifier PROGRESSBAR_FULL_SPRITE = Identifier.withDefaultNamespace(
            "container/bundle/bundle_progressbar_full");
    private static final Component BUNDLE_FULL_TEXT = Component.translatable("item.minecraft.bundle.full");
    private static final Component BUNDLE_EMPTY_TEXT = Component.translatable("item.minecraft.bundle.empty");
    private static final Component BUNDLE_EMPTY_DESCRIPTION = Component.translatable(
            "item.minecraft.bundle.empty.description");
    private static final int PROGRESSBAR_BORDER_SIZE = 1;

    private final Fraction weight;

    public ClientBundleContentsTooltip(BundleContentsTooltip tooltip) {
        super(tooltip.items(), tooltip.dyeColor(), tooltip.selectedItem());
        this.weight = tooltip.weight();
    }

    @Override
    protected int getGridSizeX() {
        return Math.max(2, (int) Math.ceil(Math.sqrt((double) this.items.size() + 1.0)));
    }

    @Override
    protected int getGridSizeY() {
        return (int) Math.ceil(((double) this.items.size() + 1.0) / (double) this.getGridSizeX());
    }

    @Override
    protected boolean isSlotBlocked(int itemIndex) {
        // container is larger by one to allow for adding items, we need to subtract that additional slot again when checking if it is full
        return itemIndex >= this.items.size() - 1 && this.isBundleFull();
    }

    private boolean isBundleEmpty() {
        return this.weight.compareTo(Fraction.ZERO) <= 0;
    }

    private boolean isBundleFull() {
        return this.weight.compareTo(Fraction.ONE) >= 0;
    }

    @Override
    public int getHeight(Font font) {
        return this.isBundleEmpty() ? this.getEmptyBundleBackgroundHeight(font) : super.getHeight(font) + 13 + 8;
    }

    @Override
    public int getWidth(Font font) {
        return this.isBundleEmpty() ? this.getGridSize(4) : super.getWidth(font);
    }

    private int getEmptyBundleBackgroundHeight(Font font) {
        return this.getEmptyBundleDescriptionTextHeight(font) + 13 + 8;
    }

    @Override
    public void extractImage(Font font, int x, int y, int width, int height, GuiGraphicsExtractor guiGraphics) {
        if (this.isBundleEmpty()) {
            this.renderEmptyBundleTooltip(font, x, y, guiGraphics);
        } else {
            super.extractImage(font, x, y, width, height, guiGraphics);
            this.drawProgressbar(x, y + super.getHeight(font) + 4, font, guiGraphics);
        }
    }

    private void renderEmptyBundleTooltip(Font font, int x, int y, GuiGraphicsExtractor guiGraphics) {
        this.drawEmptyBundleDescriptionText(x, y, font, guiGraphics);
        this.drawProgressbar(x, y + this.getEmptyBundleDescriptionTextHeight(font) + 4, font, guiGraphics);
    }

    private void drawProgressbar(int x, int y, Font font, GuiGraphicsExtractor guiGraphics) {
        guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED,
                this.getProgressBarTexture(),
                x + PROGRESSBAR_BORDER_SIZE,
                y,
                this.getProgressBarFill(font),
                13);
        guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, PROGRESSBAR_BORDER_SPRITE, x, y, this.getWidth(font), 13);
        Component component = this.getProgressBarFillText();
        if (component != null) {
            guiGraphics.centeredText(font, component, x + this.getWidth(font) / 2, y + 3, -1);
        }
    }

    private void drawEmptyBundleDescriptionText(int x, int y, Font font, GuiGraphicsExtractor guiGraphics) {
        guiGraphics.textWithWordWrap(font, BUNDLE_EMPTY_DESCRIPTION, x, y, this.getWidth(font), 0XFFAAAAAA);
    }

    private int getEmptyBundleDescriptionTextHeight(Font font) {
        return font.split(BUNDLE_EMPTY_DESCRIPTION, this.getWidth(font)).size() * 9;
    }

    private int getProgressBarFill(Font font) {
        int maxWidth = this.getWidth(font) - PROGRESSBAR_BORDER_SIZE * 2;
        return Mth.clamp(Mth.mulAndTruncate(this.weight, maxWidth), 0, maxWidth);
    }

    private Identifier getProgressBarTexture() {
        return this.isBundleFull() ? PROGRESSBAR_FULL_SPRITE : PROGRESSBAR_FILL_SPRITE;
    }

    private Component getProgressBarFillText() {
        if (this.isBundleEmpty()) {
            return BUNDLE_EMPTY_TEXT;
        } else if (this.isBundleFull()) {
            return BUNDLE_FULL_TEXT;
        } else {
            return Component.translatable("loading.progress", (int) (this.weight.doubleValue() * 100.0));
        }
    }
}
