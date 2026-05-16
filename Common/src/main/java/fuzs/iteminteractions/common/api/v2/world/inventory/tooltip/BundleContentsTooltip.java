package fuzs.iteminteractions.common.api.v2.world.inventory.tooltip;

import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.component.BundleContents;
import org.apache.commons.lang3.math.Fraction;

public record BundleContentsTooltip(BundleContents contents,
                                    Fraction weight,
                                    int gridWidth,
                                    int gridHeight) implements TooltipComponent {

}
