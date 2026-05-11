package fuzs.iteminteractions.common.api.v1.world.inventory.tooltip;

import fuzs.iteminteractions.common.api.v1.world.item.DyeBackedColor;
import net.minecraft.core.NonNullList;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

public record ItemContentsTooltip(NonNullList<ItemStack> items,
                                  int gridSizeX,
                                  int gridSizeY,
                                  @Nullable DyeBackedColor dyeColor,
                                  int selectedItem) implements TooltipComponent {

}
