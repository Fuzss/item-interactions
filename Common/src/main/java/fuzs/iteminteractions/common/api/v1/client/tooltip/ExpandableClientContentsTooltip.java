package fuzs.iteminteractions.common.api.v1.client.tooltip;

import fuzs.iteminteractions.common.impl.ItemInteractions;
import fuzs.iteminteractions.common.impl.client.core.ActivationTypeProvider;
import fuzs.iteminteractions.common.impl.config.ClientConfig;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
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
        } else {
            return this.getExpandedWidth(font);
        }
    }

    public abstract int getExpandedWidth(Font font);

    @Override
    public final void extractText(GuiGraphicsExtractor guiGraphics, Font font, int x, int y) {
        ActivationTypeProvider activation = ItemInteractions.CONFIG.get(ClientConfig.class).visualItemContents;
        if (!activation.isActive()) {
            Component component = activation.getComponent(REVEAL_CONTENTS_TRANSLATION_KEY);
            guiGraphics.text(font, component, x, y, -1);
        }
    }

    @Override
    public final void extractImage(Font font, int x, int y, int width, int height, GuiGraphicsExtractor guiGraphics) {
        if (ItemInteractions.CONFIG.get(ClientConfig.class).visualItemContents.isActive()) {
            this.extractExpandedImage(font, x, y, guiGraphics);
        }
    }

    public abstract void extractExpandedImage(Font font, int x, int y, GuiGraphicsExtractor guiGraphics);
}
