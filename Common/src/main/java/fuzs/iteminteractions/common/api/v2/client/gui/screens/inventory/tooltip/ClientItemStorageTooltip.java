package fuzs.iteminteractions.common.api.v2.client.gui.screens.inventory.tooltip;

import fuzs.iteminteractions.common.api.v2.world.item.storage.ItemStorageHolder;
import fuzs.iteminteractions.common.impl.ItemInteractions;
import fuzs.iteminteractions.common.impl.config.ClientConfig;
import fuzs.iteminteractions.common.impl.config.ContentsPreview;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientBundleTooltip;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner;
import net.minecraft.client.gui.screens.inventory.tooltip.DefaultTooltipPositioner;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.stream.Stream;

public interface ClientItemStorageTooltip extends ClientTooltipComponent, SelectedItemPreviewProvider {
    Identifier ELLIPSIS_SPRITE = ItemInteractions.id("container/ellipsis");

    int gridSizeX();

    int gridSizeY();

    default int maxGridSizeX() {
        return this.gridSizeX();
    }

    default int maxGridSizeY() {
        return ItemInteractions.CONFIG.get(ClientConfig.class).maxContentsPreviewHeight;
    }

    /**
     * @see ClientBundleTooltip#extractSelectedItemTooltip(Font, GuiGraphicsExtractor, int, int, int)
     * @see GuiGraphicsExtractor#tooltip(Font, List, int, int, ClientTooltipPositioner, Identifier, boolean)
     */
    default void extractSelectedItemTooltip(Font font, GuiGraphicsExtractor graphics, int x, int y, int w) {
        ItemStack selectedItem = this.getSelectedItem();
        if (!selectedItem.isEmpty()) {
            ItemStorageHolder holder = ItemStorageHolder.ofItem(selectedItem);
            SelectedItemPreviewProvider tooltipProvider = SelectedItemPreviewProvider.getSelectedItemTooltipProvider(
                    holder,
                    selectedItem);
            List<ClientTooltipComponent> components = tooltipProvider.createSelectedItemComponents(holder,
                    selectedItem);
            int tooltipWidth = 0;
            int tooltipHeight = components.size() > 1 ? 2 : 0;
            for (ClientTooltipComponent component : components) {
                tooltipWidth = Math.max(tooltipWidth, component.getWidth(font));
                tooltipHeight += component.getHeight(font);
            }

            int centerTooltip = x + w / 2 - 12;
            int tooltipY = y - 15 - (tooltipHeight - font.lineHeight);
            graphics.tooltip(font,
                    components,
                    centerTooltip - tooltipWidth / 2,
                    tooltipY,
                    DefaultTooltipPositioner.INSTANCE,
                    selectedItem.get(DataComponents.TOOLTIP_STYLE),
                    true);
        }
    }

    ItemStack getSelectedItem();

    @Override
    default List<ClientTooltipComponent> createSelectedItemComponents(ItemStorageHolder holder, ItemStack selectedItem) {
        List<ClientTooltipComponent> tooltipComponents = SelectedItemPreviewProvider.super.createSelectedItemComponents(
                holder,
                selectedItem);
        ContentsPreview contentsPreview = ItemInteractions.CONFIG.get(ClientConfig.class).contentsPreview;
        ClientTooltipComponent itemPreview = this.createItemPreview(contentsPreview);
        if (itemPreview != null) {
            tooltipComponents.add(itemPreview);
        }

        return tooltipComponents;
    }

    private @Nullable ClientTooltipComponent createItemPreview(ContentsPreview contentsPreview) {
        List<ItemStack> previewItems = contentsPreview.buildPreviewItems(this.getAllItems().toList());
        if (previewItems.isEmpty()) {
            return null;
        }

        int gridWidth = Math.min(this.maxGridSizeX(), previewItems.size());
        int gridHeight = Math.min(this.maxGridSizeY(), Math.ceilDiv(previewItems.size(), gridWidth));
        int updatedGridWidth = Math.min(gridWidth, Math.ceilDiv(previewItems.size(), gridHeight));
        int updatedGridHeight = gridHeight;
        int slotsNum = updatedGridWidth * updatedGridHeight;
        NonNullList<ItemStack> nonEmptyItems = NonNullList.of(ItemStack.EMPTY,
                previewItems.subList(0, Math.min(previewItems.size(), slotsNum)).toArray(ItemStack[]::new));
        boolean overflow = previewItems.size() > slotsNum;
        return this.createItemPreview(nonEmptyItems,
                updatedGridWidth,
                updatedGridHeight,
                overflow,
                contentsPreview == ContentsPreview.COMBINED_ITEMS);
    }

    ClientTooltipComponent createItemPreview(NonNullList<ItemStack> previewItems, int gridWidth, int gridHeight, boolean overflow, boolean combined);

    Stream<ItemStack> getAllItems();
}
