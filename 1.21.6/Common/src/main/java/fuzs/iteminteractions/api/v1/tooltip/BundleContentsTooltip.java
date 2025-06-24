package fuzs.iteminteractions.api.v1.tooltip;

import net.minecraft.core.NonNullList;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.math.Fraction;

public record BundleContentsTooltip(NonNullList<ItemStack> items, Fraction weight, int backgroundColor) implements TooltipComponent {

}