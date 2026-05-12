package fuzs.iteminteractions.common.impl.client.gui.screens.inventory.tooltip;

import fuzs.iteminteractions.common.impl.ItemInteractions;
import fuzs.iteminteractions.common.impl.client.core.ActivationTypeProvider;
import fuzs.iteminteractions.common.impl.config.ClientConfig;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.tooltip.TooltipComponent;

import java.util.function.Function;

public record CollapsibleClientTooltipComponent(ClientTooltipComponent component) implements ClientTooltipComponent {
    public static final String REVEAL_CONTENTS_TRANSLATION_KEY = ItemInteractions.id("container")
            .toLanguageKey(Registries.elementsDirPath(Registries.ITEM), "tooltip.reveal_contents");

    public static <T extends TooltipComponent> Function<? super T, CollapsibleClientTooltipComponent> wrapFactory(Function<? super T, ? extends ClientTooltipComponent> factory) {
        return (T tooltipComponent) -> new CollapsibleClientTooltipComponent(factory.apply(tooltipComponent));
    }

    @Override
    public int getHeight(Font font) {
        if (!ItemInteractions.CONFIG.get(ClientConfig.class).visualItemContents.isActive()) {
            return 10;
        } else {
            return this.component.getHeight(font);
        }
    }

    @Override
    public int getWidth(Font font) {
        ActivationTypeProvider activation = ItemInteractions.CONFIG.get(ClientConfig.class).visualItemContents;
        if (!activation.isActive()) {
            Component component = activation.getComponent(REVEAL_CONTENTS_TRANSLATION_KEY);
            return font.width(component);
        } else {
            return this.component.getWidth(font);
        }
    }

    @Override
    public boolean showTooltipWithItemInHand() {
        if (ItemInteractions.CONFIG.get(ClientConfig.class).visualItemContents.isActive()) {
            return this.component.showTooltipWithItemInHand();
        } else {
            return false;
        }
    }

    @Override
    public void extractText(GuiGraphicsExtractor guiGraphics, Font font, int x, int y) {
        ActivationTypeProvider activation = ItemInteractions.CONFIG.get(ClientConfig.class).visualItemContents;
        if (!activation.isActive()) {
            Component component = activation.getComponent(REVEAL_CONTENTS_TRANSLATION_KEY);
            guiGraphics.text(font, component, x, y, -1);
        }
    }

    @Override
    public void extractImage(Font font, int x, int y, int width, int height, GuiGraphicsExtractor guiGraphics) {
        if (ItemInteractions.CONFIG.get(ClientConfig.class).visualItemContents.isActive()) {
            this.component.extractImage(font, x, y, width, height, guiGraphics);
        }
    }
}
