package fuzs.iteminteractions.impl.client.helper;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.Material;
import net.minecraft.resources.ResourceLocation;

/**
 * TODO remove and replace with original class from Puzzles Lib
 */
@Deprecated
public class GuiGraphicsHelper {

    /**
     * Manually draws a sprite using tile mode. Width &amp; height portions outside the texture dimensions are filled by
     * repeating the original texture.
     * <p>
     * Most sprites by default use the stretch mode, which squeezes them into the provided width &amp; height.
     *
     * @param guiGraphics    the gui graphics instance
     * @param renderPipeline the render pipeline, usually
     *                       {@link net.minecraft.client.renderer.RenderPipelines#GUI_TEXTURED}
     * @param sprite         the sprite resource location
     * @param x              the x-position on the screen
     * @param y              the y-position on the screen
     * @param width          the width to draw
     * @param height         the height to draw
     * @param spriteWidth    the sprite texture file width
     * @param spriteHeight   the sprite texture file height
     */
    public static void blitTiledSprite(GuiGraphics guiGraphics, RenderPipeline renderPipeline, ResourceLocation sprite, int x, int y, int width, int height, int spriteWidth, int spriteHeight) {
        blitTiledSprite(guiGraphics, renderPipeline, sprite, x, y, width, height, spriteWidth, spriteHeight, -1);
    }

    /**
     * Manually draws a sprite using tile mode. Width &amp; height portions outside the texture dimensions are filled by
     * repeating the original texture.
     * <p>
     * Most sprites by default use the stretch mode, which squeezes them into the provided width &amp; height.
     *
     * @param guiGraphics    the gui graphics instance
     * @param renderPipeline the render pipeline, usually
     *                       {@link net.minecraft.client.renderer.RenderPipelines#GUI_TEXTURED}
     * @param sprite         the sprite resource location
     * @param x              the x-position on the screen
     * @param y              the y-position on the screen
     * @param width          the width to draw
     * @param height         the height to draw
     * @param spriteWidth    the sprite texture file width
     * @param spriteHeight   the sprite texture file height
     * @param color          the vertex color, usually {@code -1}
     */
    public static void blitTiledSprite(GuiGraphics guiGraphics, RenderPipeline renderPipeline, ResourceLocation sprite, int x, int y, int width, int height, int spriteWidth, int spriteHeight, int color) {
        blitTiledSprite(guiGraphics,
                renderPipeline,
                sprite,
                x,
                y,
                width,
                height,
                spriteWidth,
                spriteHeight,
                0,
                0,
                color);
    }

    /**
     * Manually draws a sprite using tile mode. Width &amp; height portions outside the texture dimensions are filled by
     * repeating the original texture.
     * <p>
     * Most sprites by default use the stretch mode, which squeezes them into the provided width &amp; height.
     *
     * @param guiGraphics    the gui graphics instance
     * @param renderPipeline the render pipeline, usually
     *                       {@link net.minecraft.client.renderer.RenderPipelines#GUI_TEXTURED}
     * @param sprite         the sprite resource location
     * @param x              the x-position on the screen
     * @param y              the y-position on the screen
     * @param width          the width to draw
     * @param height         the height to draw
     * @param spriteWidth    the sprite texture file width
     * @param spriteHeight   the sprite texture file height
     * @param uOffset        the sprite u-offset on the texture sheet
     * @param vOffset        the sprite v-offset on the texture sheet
     * @param color          the vertex color, usually {@code -1}
     */
    public static void blitTiledSprite(GuiGraphics guiGraphics, RenderPipeline renderPipeline, ResourceLocation sprite, int x, int y, int width, int height, int spriteWidth, int spriteHeight, int uOffset, int vOffset, int color) {
        TextureAtlasSprite textureAtlasSprite = guiGraphics.getSprite(new Material(Sheets.GUI_SHEET, sprite));
        guiGraphics.blitTiledSprite(renderPipeline,
                textureAtlasSprite,
                x,
                y,
                width,
                height,
                uOffset,
                vOffset,
                spriteWidth,
                spriteHeight,
                spriteWidth,
                spriteHeight,
                color);
    }
}
