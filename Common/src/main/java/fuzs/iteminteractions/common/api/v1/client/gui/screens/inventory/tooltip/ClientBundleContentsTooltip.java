package fuzs.iteminteractions.common.api.v1.client.gui.screens.inventory.tooltip;

import fuzs.iteminteractions.common.api.v1.world.inventory.tooltip.BundleContentsTooltip;
import fuzs.iteminteractions.common.impl.ItemInteractions;
import fuzs.iteminteractions.common.impl.config.ClientConfig;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientBundleTooltip;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStackTemplate;
import org.apache.commons.lang3.math.Fraction;

import java.util.List;

public class ClientBundleContentsTooltip extends ClientBundleTooltip {
    private final int gridWidth;
    private final int gridHeight;

    public ClientBundleContentsTooltip(BundleContentsTooltip tooltip) {
        super(tooltip.contents());
        this.gridWidth = tooltip.gridWidth();
        this.gridHeight = tooltip.gridHeight();
    }

    @Override
    public int getWidth(Font font) {
        return this.contents.isEmpty() ? super.getWidth(font) : this.gridSizeX() * SLOT_SIZE;
    }

    /**
     * @see ClientBundleTooltip#getContentXOffset(int)
     */
    public int getCustomContentXOffset(Font font, int tooltipWidth) {
        return (tooltipWidth - this.getWidth(font)) / 2;
    }

    public int gridSizeX() {
        return this.gridWidth;
    }

    @Override
    public int gridSizeY() {
        return this.gridHeight;
    }

    @Override
    public int slotCount() {
        return this.contents.size();
    }

    @Override
    public boolean showTooltipWithItemInHand() {
        return !ItemInteractions.CONFIG.get(ClientConfig.class).carriedItemTooltips.isActive();
    }

    @Override
    public void extractBundleWithItemsTooltip(Font font, int x, int y, int w, int h, GuiGraphicsExtractor graphics, Fraction weight) {
        List<ItemStackTemplate> shownItems = this.getShownItems(this.contents.size());
        int xStartPos = x + this.getCustomContentXOffset(font, w) + this.getWidth(font);
        int yStartPos = y + this.gridSizeY() * SLOT_SIZE;
        int slotNumber = 1;

        for (int rowNumber = 1; rowNumber <= this.gridSizeY(); rowNumber++) {
            for (int columnNumber = 1; columnNumber <= this.gridSizeX(); columnNumber++) {
                int drawX = xStartPos - columnNumber * SLOT_SIZE;
                int drawY = yStartPos - rowNumber * SLOT_SIZE;
                if (shouldRenderItemSlot(shownItems, slotNumber)) {
                    this.extractSlot(slotNumber, drawX, drawY, shownItems, slotNumber, font, graphics);
                    slotNumber++;
                }
            }
        }

        this.extractSelectedItemTooltip(font, graphics, x, y, w);
        this.extractCustomProgressbar(x + this.getCustomContentXOffset(font, w),
                y + this.itemGridHeight() + 4,
                font,
                graphics,
                weight);
    }

    /**
     * @see ClientBundleTooltip#extractProgressbar(int, int, Font, GuiGraphicsExtractor, Fraction)
     */
    public void extractCustomProgressbar(int x, int y, Font font, GuiGraphicsExtractor graphics, Fraction weight) {
        int progressBarWidth = this.getWidth(font);
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED,
                getProgressBarTexture(weight),
                x + PROGRESSBAR_BORDER,
                y,
                this.getCustomProgressBarFill(weight, progressBarWidth),
                PROGRESSBAR_HEIGHT);
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED,
                PROGRESSBAR_BORDER_SPRITE,
                x,
                y,
                progressBarWidth,
                PROGRESSBAR_HEIGHT);
        Component progressBarFillText = getCustomProgressBarFillText(weight);
        graphics.centeredText(font, progressBarFillText, x + progressBarWidth / 2, y + 3, -1);
    }

    /**
     * @see ClientBundleTooltip#getProgressBarFill(Fraction)
     */
    public int getCustomProgressBarFill(Fraction weight, int progressBarWidth) {
        int progressBarFill = progressBarWidth - PROGRESSBAR_BORDER * 2;
        return Mth.clamp(Mth.mulAndTruncate(weight, progressBarFill), 0, progressBarFill);
    }

    /**
     * @see ClientBundleTooltip#getProgressBarFillText(Fraction)
     */
    public static Component getCustomProgressBarFillText(Fraction weight) {
        Component progressBarFillText = ClientBundleTooltip.getProgressBarFillText(weight);
        if (progressBarFillText != null) {
            return progressBarFillText;
        } else {
            return Component.translatable("loading.progress", (int) (weight.doubleValue() * 100.0));
        }
    }
}
