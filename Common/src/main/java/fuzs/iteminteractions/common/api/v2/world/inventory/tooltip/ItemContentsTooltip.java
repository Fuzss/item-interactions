package fuzs.iteminteractions.common.api.v2.world.inventory.tooltip;

import fuzs.iteminteractions.common.api.v2.world.item.DyeBackedColor;
import net.minecraft.core.NonNullList;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

public record ItemContentsTooltip(NonNullList<ItemStack> itemList,
                                  int selectedItem,
                                  int gridWidth,
                                  int gridHeight,
                                  @Nullable DyeBackedColor dyeColor) implements TooltipComponent {

}
