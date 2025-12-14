package fuzs.iteminteractions.api.v1.tooltip;

import fuzs.iteminteractions.api.v1.DyeBackedColor;
import net.minecraft.core.NonNullList;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.math.Fraction;
import org.jspecify.annotations.Nullable;

public record BundleContentsTooltip(NonNullList<ItemStack> items,
                                    Fraction weight,
                                    @Nullable DyeBackedColor dyeColor) implements TooltipComponent {

}
