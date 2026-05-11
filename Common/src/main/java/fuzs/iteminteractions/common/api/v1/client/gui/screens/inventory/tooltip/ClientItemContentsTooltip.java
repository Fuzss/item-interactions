package fuzs.iteminteractions.common.api.v1.client.gui.screens.inventory.tooltip;

import fuzs.iteminteractions.common.api.v1.world.inventory.tooltip.ItemContentsTooltip;

public class ClientItemContentsTooltip extends ClientItemStorageTooltip {
    private final int gridSizeX;
    private final int gridSizeY;

    public ClientItemContentsTooltip(ItemContentsTooltip tooltip) {
        super(tooltip.items(), tooltip.dyeColor(), tooltip.selectedItem());
        this.gridSizeX = tooltip.gridSizeX();
        this.gridSizeY = tooltip.gridSizeY();
    }

    @Override
    protected int getGridSizeX() {
        return this.gridSizeX;
    }

    @Override
    protected int getGridSizeY() {
        return this.gridSizeY;
    }
}
