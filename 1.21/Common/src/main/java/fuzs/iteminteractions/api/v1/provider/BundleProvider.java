package fuzs.iteminteractions.api.v1.provider;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonObject;
import fuzs.iteminteractions.api.v1.tooltip.ModBundleTooltip;
import fuzs.puzzleslib.api.container.v1.ContainerMenuHelper;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.BundleContents;
import org.apache.commons.lang3.math.Fraction;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.stream.Stream;

public class BundleProvider extends AbstractItemContainerProvider {
    private final int capacityMultiplier;

    public BundleProvider(int capacityMultiplier, @Nullable DyeColor dyeColor) {
        super(dyeColor);
        this.capacityMultiplier = capacityMultiplier;
    }

    public int getCapacityMultiplier() {
        return this.capacityMultiplier;
    }

    @Override
    public boolean hasContents(ItemStack containerStack) {
        return !containerStack.getOrDefault(DataComponents.BUNDLE_CONTENTS, BundleContents.EMPTY).isEmpty();
    }

    @Override
    public SimpleContainer getItemContainer(ItemStack containerStack, Player player, boolean allowSaving) {
        BundleContents contents = containerStack.getOrDefault(DataComponents.BUNDLE_CONTENTS, BundleContents.EMPTY);
        // add one additional slot, so we can add items in the inventory
        ItemStack[] itemStacks = Stream.concat(contents.itemCopyStream(), Stream.of(ItemStack.EMPTY))
                .toArray(ItemStack[]::new);
        NonNullList<ItemStack> items = NonNullList.of(ItemStack.EMPTY, itemStacks);
        return ContainerMenuHelper.createListBackedContainer(items, allowSaving ? (Container container) -> {
            BundleContents newContents;
            if (container.isEmpty()) {
                newContents = BundleContents.EMPTY;
            } else {
                // empty stacks must not get in here, the codec will fail otherwise
                ImmutableList.Builder<ItemStack> builder = ImmutableList.builder();
                for (ItemStack itemStack : items) {
                    if (!itemStack.isEmpty()) builder.add(itemStack);
                }
                newContents = new BundleContents(builder.build());
            }
            containerStack.set(DataComponents.BUNDLE_CONTENTS, newContents);
        } : null);
    }

    @Override
    public boolean isItemAllowedInContainer(ItemStack containerStack, ItemStack stackToAdd) {
        return super.isItemAllowedInContainer(containerStack, stackToAdd) &&
                stackToAdd.getItem().canFitInsideContainerItems();
    }

    @Override
    public boolean canAddItem(ItemStack containerStack, ItemStack stackToAdd, Player player) {
        return this.getMaxAmountToAdd(containerStack, stackToAdd, player) > 0;
    }

    @Override
    public int getAcceptableItemCount(ItemStack containerStack, ItemStack stackToAdd, Player player) {
        return Math.min(this.getMaxAmountToAdd(containerStack, stackToAdd, player),
                super.getAcceptableItemCount(containerStack, stackToAdd, player)
        );
    }

    public int getMaxAmountToAdd(ItemStack containerStack, ItemStack stackToAdd, Player player) {
        Fraction fraction = Fraction.ONE.subtract(this.computeContentWeight(containerStack, player));
        return Math.max(fraction.divideBy(this.getWeight(stackToAdd)).intValue(), 0);
    }

    @Override
    public boolean canProvideTooltipImage(ItemStack containerStack, Player player) {
        return true;
    }

    @Override
    public Optional<TooltipComponent> getTooltipImage(ItemStack containerStack, Player player) {
        // make sure to always override bundle tooltip, as otherwise vanilla tooltip would show for empty bundles
        if (this.hasContents(containerStack)) {
            return super.getTooltipImage(containerStack, player);
        } else {
            return Optional.empty();
        }
    }

    @Override
    public TooltipComponent createTooltipImageComponent(ItemStack containerStack, Player player, NonNullList<ItemStack> items) {
        return new ModBundleTooltip(items,
                this.computeContentWeight(containerStack, player).compareTo(Fraction.ONE) >= 0,
                this.getBackgroundColor()
        );
    }

    public Fraction computeContentWeight(ItemStack containerStack, Player player) {
        SimpleContainer container = this.getItemContainer(containerStack, player, false);
        return container.getItems().stream().map((ItemStack itemStack) -> {
            return this.getWeight(itemStack).multiplyBy(Fraction.getFraction(itemStack.getCount(), 1));
        }).reduce(Fraction::add).orElse(Fraction.ZERO);
    }

    public Fraction getWeight(ItemStack stackToAdd) {
        return BundleContents.getWeight(stackToAdd).multiplyBy(Fraction.getFraction(1, this.getCapacityMultiplier()));
    }

    @Override
    public void toJson(JsonObject jsonObject) {
        jsonObject.addProperty("capacity_multiplier", this.getCapacityMultiplier());
        super.toJson(jsonObject);
    }
}
