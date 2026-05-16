package fuzs.iteminteractions.common.api.v2.world.inventory.tooltip;

import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.component.BundleContents;

public record BundleContentsTooltip(BundleContents contents,
                                    int gridWidth,
                                    int gridHeight) implements TooltipComponent {

}
