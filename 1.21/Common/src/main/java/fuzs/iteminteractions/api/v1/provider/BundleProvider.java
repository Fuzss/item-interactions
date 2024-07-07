package fuzs.iteminteractions.api.v1.provider;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonObject;
import fuzs.iteminteractions.api.v1.tooltip.ModBundleTooltip;
import fuzs.iteminteractions.impl.world.item.container.ItemInteractionHelper;
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
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class BundleProvider extends AbstractItemContainerProvider {
    private final int capacity;

    public BundleProvider(int capacity, @Nullable DyeColor dyeColor) {
        this(capacity, dyeColor, ItemInteractionHelper.TAG_ITEMS);
    }

    public BundleProvider(int capacity, @Nullable DyeColor dyeColor, String... nbtKey) {
        super(dyeColor, nbtKey);
        this.capacity = capacity;
    }

    public int getCapacity() {
        return this.capacity;
    }

    @Override
    public boolean hasContents(ItemStack containerStack) {
        return !containerStack.getOrDefault(DataComponents.BUNDLE_CONTENTS, BundleContents.EMPTY).isEmpty();
    }

    @Override
    public SimpleContainer getItemContainer(ItemStack containerStack, Player player, boolean allowSaving) {
        BundleContents contents = containerStack.getOrDefault(DataComponents.BUNDLE_CONTENTS, BundleContents.EMPTY);
        ItemStack[] itemStacks = Stream.concat(contents.itemCopyStream(), Stream.of(ItemStack.EMPTY)).toArray(ItemStack[]::new);
        NonNullList<ItemStack> items = NonNullList.of(ItemStack.EMPTY, itemStacks);
        return ContainerMenuHelper.createListBackedContainer(items, allowSaving ? (Container container) -> {
            BundleContents newContents;
            if (container.isEmpty()) {
                newContents = BundleContents.EMPTY;
            } else {
                newContents = new BundleContents(ImmutableList.copyOf(items));
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
        return this.getAvailableBundleItemSpace(containerStack, stackToAdd, player) > 0;
    }

    @Override
    public int getAcceptableItemCount(ItemStack containerStack, ItemStack stackToAdd, Player player) {
        return Math.min(this.getAvailableBundleItemSpace(containerStack, stackToAdd, player),
                super.getAcceptableItemCount(containerStack, stackToAdd, player)
        );
    }

    protected int getAvailableBundleItemSpace(ItemStack containerStack, ItemStack stackToAdd, Player player) {
        int itemWeight = this.getItemWeight(stackToAdd);
        // fix java.lang.ArithmeticException: / by zero from Numismatic Overhaul as their coins stack to 99 instead of 64
        if (itemWeight <= 0) return 0;
        return (this.getCapacity() - this.getContentWeight(containerStack, player)) / itemWeight;
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
                this.getContentWeight(containerStack, player) >= this.getCapacity(),
                this.getBackgroundColor()
        );
    }

    public int getContentWeight(ItemStack containerStack, Player player) {
        SimpleContainer container = this.getItemContainer(containerStack, player, false);
        return container.items.stream().mapToInt(stack -> {
            return this.getItemWeight(stack) * stack.getCount();
        }).sum();
    }

    public int getItemWeight(ItemStack itemStack) {
        // TODO fix this
//        return BundleContents.getWeight(itemStack);
        return 0;
    }

    @Override
    public void toJson(JsonObject jsonObject) {
        jsonObject.addProperty("capacity", this.getCapacity());
        super.toJson(jsonObject);
    }
}
