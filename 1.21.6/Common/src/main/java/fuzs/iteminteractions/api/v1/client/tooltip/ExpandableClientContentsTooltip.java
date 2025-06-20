package fuzs.iteminteractions.api.v1.client.tooltip;

import fuzs.iteminteractions.impl.ItemInteractions;
import fuzs.iteminteractions.impl.client.core.HeldActivationType;
import fuzs.iteminteractions.impl.config.ClientConfig;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;

public abstract class ExpandableClientContentsTooltip implements ClientTooltipComponent {
    public static final String REVEAL_CONTENTS_TRANSLATION_KEY = "item.container.tooltip.revealContents";

    @Override
    public final int getHeight(Font font) {
        if (!ItemInteractions.CONFIG.get(ClientConfig.class).revealContents.isActive()) {
            return 10;
        } else {
            return this.getExpandedHeight(font);
        }
    }

    public abstract int getExpandedHeight(Font font);

    @Override
    public final int getWidth(Font font) {
        HeldActivationType activation = ItemInteractions.CONFIG.get(ClientConfig.class).revealContents;
        if (!activation.isActive()) {
            Component component = activation.getComponent(REVEAL_CONTENTS_TRANSLATION_KEY);
            return font.width(component);
        }
        return this.getExpandedWidth(font);
    }

    public abstract int getExpandedWidth(Font font);

    @Override
    public final void renderText(GuiGraphics guiGraphics, Font font, int x, int y) {
        HeldActivationType activation = ItemInteractions.CONFIG.get(ClientConfig.class).revealContents;
        if (!activation.isActive()) {
            Component component = activation.getComponent(REVEAL_CONTENTS_TRANSLATION_KEY);
            guiGraphics.drawString(font, component, x, y, -1);
        }
    }

    @Override
    public final void renderImage(Font font, int x, int y, int width, int height, GuiGraphics guiGraphics) {
        if (!ItemInteractions.CONFIG.get(ClientConfig.class).revealContents.isActive()) return;
        this.renderExpandedImage(font, x, y, guiGraphics);
    }

    public abstract void renderExpandedImage(Font font, int x, int y, GuiGraphics guiGraphics);
}
