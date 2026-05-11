package fuzs.iteminteractions.common.api.v1.world.item.storage;

import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;

public interface ItemStorageWithTooltip extends ItemStorage {

    @Override
    default boolean canProvideTooltipImage(ItemStack itemStack, Player player) {
        return this.hasContents(itemStack) && !this.getItemContainer(itemStack, player, false).isEmpty();
    }

    @Override
    default Optional<TooltipComponent> getTooltipImage(ItemStack itemStack, Player player) {
        NonNullList<ItemStack> items = this.getItemContainer(itemStack, player, false).getItems();
        return Optional.of(this.createTooltipImageComponent(itemStack, player, items));
    }

    TooltipComponent createTooltipImageComponent(ItemStack itemStack, Player player, NonNullList<ItemStack> items);
}
