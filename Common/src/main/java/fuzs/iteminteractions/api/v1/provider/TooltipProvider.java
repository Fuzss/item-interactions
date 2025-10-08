package fuzs.iteminteractions.api.v1.provider;

import net.minecraft.core.NonNullList;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;

public interface TooltipProvider extends ItemContentsProvider {

    @Override
    default boolean canProvideTooltipImage(ItemStack containerStack, Player player) {
        return this.hasContents(containerStack) &&
                !this.getItemContainer(containerStack, player, false).isEmpty();
    }

    @Override
    default Optional<TooltipComponent> getTooltipImage(ItemStack containerStack, Player player) {
        SimpleContainer itemContainer = this.getItemContainer(containerStack, player, false);
        return Optional.of(this.createTooltipImageComponent(containerStack, player, itemContainer.getItems()));
    }

    TooltipComponent createTooltipImageComponent(ItemStack containerStack, Player player, NonNullList<ItemStack> items);
}
