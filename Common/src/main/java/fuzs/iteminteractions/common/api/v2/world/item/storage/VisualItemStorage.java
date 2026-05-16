package fuzs.iteminteractions.common.api.v2.world.item.storage;

import net.minecraft.core.NonNullList;
import net.minecraft.util.Mth;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.BundleItem;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.math.Fraction;

import java.util.Optional;
import java.util.OptionalInt;

public interface VisualItemStorage extends ContainerItemStorage {

    @Override
    default Optional<Optional<TooltipComponent>> getTooltipImage(ItemStack itemStack, Player player) {
        if (this.hasContents(itemStack) && !this.getItemContainer(itemStack, player).isEmpty()) {
            NonNullList<ItemStack> itemList = this.getItemContainer(itemStack, player, false).getItems();
            return Optional.of(Optional.of(this.createTooltipImageComponent(itemStack, player, itemList)));
        } else {
            return Optional.empty();
        }
    }

    TooltipComponent createTooltipImageComponent(ItemStack itemStack, Player player, NonNullList<ItemStack> itemList);

    /**
     * @see net.minecraft.world.item.BundleItem#isBarVisible(ItemStack)
     */
    @Override
    default Optional<Boolean> isBarVisible(ItemStack itemStack, Player player) {
        return Optional.of(!this.getItemContainer(itemStack, player).isEmpty());
    }

    /**
     * @see net.minecraft.world.item.BundleItem#getBarWidth(ItemStack)
     */
    @Override
    default OptionalInt getBarWidth(ItemStack itemStack, Player player) {
        Container container = this.getItemContainer(itemStack, player);
        Fraction fraction = Fraction.getFraction(this.nonEmptySlots(container), container.getContainerSize());
        return OptionalInt.of(Math.min(1 + Mth.mulAndTruncate(fraction, 12), 13));
    }

    /**
     * @see net.minecraft.world.item.BundleItem#getBarColor(ItemStack)
     */
    @Override
    default OptionalInt getBarColor(ItemStack itemStack, Player player) {
        Container container = this.getItemContainer(itemStack, player);
        boolean hasRemainingCapacity = this.nonEmptySlots(container) < container.getContainerSize();
        return OptionalInt.of(hasRemainingCapacity ? BundleItem.BAR_COLOR : BundleItem.FULL_BAR_COLOR);
    }

    private int nonEmptySlots(Container container) {
        int nonEmptySlots = 0;
        for (int slotNum = 0; slotNum < container.getContainerSize(); slotNum++) {
            if (!container.getItem(slotNum).isEmpty()) {
                nonEmptySlots++;
            }
        }

        return nonEmptySlots;
    }
}
