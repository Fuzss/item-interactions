package fuzs.iteminteractions.common.api.v1.world.inventory.tooltip;

import fuzs.iteminteractions.common.api.v1.world.item.DyeBackedColor;
import net.minecraft.core.NonNullList;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.math.Fraction;
import org.jspecify.annotations.Nullable;

public record BundleContentsTooltip(NonNullList<ItemStack> items,
                                    Fraction weight,
                                    @Nullable DyeBackedColor dyeColor,
                                    int selectedItem) implements TooltipComponent {

}
