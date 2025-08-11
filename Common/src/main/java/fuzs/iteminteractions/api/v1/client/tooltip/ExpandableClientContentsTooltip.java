package fuzs.iteminteractions.api.v1.client.tooltip;

import fuzs.iteminteractions.impl.ItemInteractions;
import fuzs.iteminteractions.impl.client.core.ActivationTypeProvider;
import fuzs.iteminteractions.impl.config.ClientConfig;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;

public abstract class ExpandableClientContentsTooltip implements ClientTooltipComponent {
    public static final String REVEAL_CONTENTS_TRANSLATION_KEY = ItemInteractions.id("container")
            .toLanguageKey(Registries.elementsDirPath(Registries.ITEM), "tooltip.reveal_contents");

    @Override
    public final int getHeight(Font font) {
        if (!ItemInteractions.CONFIG.get(ClientConfig.class).visualItemContents.isActive()) {
            return 10;
        } else {
            return this.getExpandedHeight(font);
        }
    }

    public abstract int getExpandedHeight(Font font);

    @Override
    public final int getWidth(Font font) {
        ActivationTypeProvider activation = ItemInteractions.CONFIG.get(ClientConfig.class).visualItemContents;
        if (!activation.isActive()) {
            Component component = activation.getComponent(REVEAL_CONTENTS_TRANSLATION_KEY);
            return font.width(component);
        }
        return this.getExpandedWidth(font);
    }

    public abstract int getExpandedWidth(Font font);

    @Override
    public final void renderText(GuiGraphics guiGraphics, Font font, int x, int y) {
        ActivationTypeProvider activation = ItemInteractions.CONFIG.get(ClientConfig.class).visualItemContents;
        if (!activation.isActive()) {
            Component component = activation.getComponent(REVEAL_CONTENTS_TRANSLATION_KEY);
            guiGraphics.drawString(font, component, x, y, -1);
        }
    }

    @Override
    public final void renderImage(Font font, int x, int y, int width, int height, GuiGraphics guiGraphics) {
        if (!ItemInteractions.CONFIG.get(ClientConfig.class).visualItemContents.isActive()) return;
        this.renderExpandedImage(font, x, y, guiGraphics);
    }

    public abstract void renderExpandedImage(Font font, int x, int y, GuiGraphics guiGraphics);
}
