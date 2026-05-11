package fuzs.iteminteractions.common.impl.config;

import fuzs.iteminteractions.common.api.v1.client.gui.screens.inventory.tooltip.ClientItemContentsTooltip;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

public enum SlotHighlight {
    SELECTION(null, ClientItemContentsTooltip.SLOT_SELECTION_SPRITE),
    HIGHLIGHT(AbstractContainerScreen.SLOT_HIGHLIGHT_BACK_SPRITE, AbstractContainerScreen.SLOT_HIGHLIGHT_FRONT_SPRITE);

    @Nullable
    private final Identifier backSprite;
    private final Identifier frontSprite;

    SlotHighlight(@Nullable Identifier backSprite, Identifier frontSprite) {
        this.backSprite = backSprite;
        this.frontSprite = frontSprite;
    }

    public void blitBackSprite(GuiGraphicsExtractor guiGraphics, int posX, int posY) {
        this.blitSprite(guiGraphics, posX, posY, this.backSprite);
    }

    public void blitFrontSprite(GuiGraphicsExtractor guiGraphics, int posX, int posY) {
        this.blitSprite(guiGraphics, posX, posY, this.frontSprite);
    }

    private void blitSprite(GuiGraphicsExtractor guiGraphics, int posX, int posY, @Nullable Identifier sprite) {
        if (sprite != null) {
            guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, sprite, posX - 3, posY - 3, 24, 24);
        }
    }
}
