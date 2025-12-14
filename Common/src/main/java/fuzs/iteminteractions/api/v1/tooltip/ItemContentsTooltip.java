package fuzs.iteminteractions.api.v1.tooltip;

import fuzs.iteminteractions.api.v1.DyeBackedColor;
import net.minecraft.core.NonNullList;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

public record ItemContentsTooltip(NonNullList<ItemStack> items,
                                  int gridSizeX,
                                  int gridSizeY,
                                  @Nullable DyeBackedColor dyeColor) implements TooltipComponent {

}
