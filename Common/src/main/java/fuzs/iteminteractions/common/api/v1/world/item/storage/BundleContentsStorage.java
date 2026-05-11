package fuzs.iteminteractions.common.api.v1.world.item.storage;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fuzs.iteminteractions.common.api.v1.world.inventory.tooltip.BundleContentsTooltip;
import fuzs.iteminteractions.common.impl.init.ModRegistry;
import fuzs.puzzleslib.common.api.container.v1.ContainerMenuHelper;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.Mth;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.BundleItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.component.BundleContents;
import org.apache.commons.lang3.math.Fraction;

import java.util.stream.Stream;

public class BundleContentsStorage extends ComponentBackedStorage {
    public static final MapCodec<BundleContentsStorage> CODEC = RecordCodecBuilder.mapCodec(instance -> {
        return instance.group(capacityMultiplierCodec(), itemContentsCodec())
                .apply(instance, (Integer capacityMultiplier, ItemContents itemContents) -> {
                    return new BundleContentsStorage(capacityMultiplier).itemContents(itemContents);
                });
    });

    final int capacityMultiplier;

    public BundleContentsStorage() {
        this(1);
    }

    public BundleContentsStorage(int capacityMultiplier) {
        this.capacityMultiplier = capacityMultiplier;
    }

    protected static <T extends BundleContentsStorage> RecordCodecBuilder<T, Integer> capacityMultiplierCodec() {
        return ExtraCodecs.POSITIVE_INT.fieldOf("capacity_multiplier")
                .forGetter(provider -> provider.capacityMultiplier);
    }

    @Override
    protected BundleContentsStorage itemContents(ItemContents itemContents) {
        return (BundleContentsStorage) super.itemContents(itemContents);
    }

    @Override
    public BundleContentsStorage filterContainerItems(boolean filterContainerItems) {
        return (BundleContentsStorage) super.filterContainerItems(filterContainerItems);
    }

    public Fraction getCapacityMultiplier(ItemStack containerStack) {
        return Fraction.getFraction(this.capacityMultiplier, 1);
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
            BundleContents updatedContents;
            if (container.isEmpty()) {
                updatedContents = BundleContents.EMPTY;
            } else {
                // empty stacks must not get in here, the codec will fail otherwise
                ImmutableList.Builder<ItemStackTemplate> builder = ImmutableList.builder();
                for (ItemStack itemStack : items) {
                    if (!itemStack.isEmpty()) {
                        builder.add(ItemStackTemplate.fromNonEmptyStack(itemStack));
                    }
                }

                updatedContents = new BundleContents(builder.build());
            }

            containerStack.set(DataComponents.BUNDLE_CONTENTS, updatedContents);
        } : null);
    }

    @Override
    public boolean canAddItem(ItemStack containerStack, ItemStack stackToAdd, Player player) {
        return this.getMaxAmountToAdd(containerStack, stackToAdd, player) > 0;
    }

    @Override
    public int getAcceptableItemCount(ItemStack containerStack, ItemStack stackToAdd, Player player) {
        return Math.min(this.getMaxAmountToAdd(containerStack, stackToAdd, player),
                super.getAcceptableItemCount(containerStack, stackToAdd, player));
    }

    @Override
    public int getGridWidth(int itemCount) {
        return Math.max(4, Mth.ceil(Math.sqrt(itemCount)));
    }

    @Override
    public int getGridHeight(int itemCount) {
        return Mth.positiveCeilDiv(itemCount, this.getGridWidth(itemCount));
    }

    @Override
    public boolean canProvideTooltipImage(ItemStack itemStack, Player player) {
        return true;
    }

    @Override
    public void onToggleSelectedItem(ItemStack containerStack, int oldSelectedItem, int newSelectedItem) {
        if (oldSelectedItem != newSelectedItem) {
            BundleItem.toggleSelectedItem(containerStack, newSelectedItem);
        }
    }

    @Override
    public NonNullList<ItemStack> getTooltipContents(ItemStack itemStack, Player player) {
        BundleContents contents = itemStack.getOrDefault(DataComponents.BUNDLE_CONTENTS, BundleContents.EMPTY);
        return NonNullList.of(ItemStack.EMPTY, contents.itemCopyStream().toArray(ItemStack[]::new));
    }

    @Override
    public TooltipComponent createTooltipImageComponent(ItemStack itemStack, Player player, NonNullList<ItemStack> items) {
        BundleContents contents = itemStack.getOrDefault(DataComponents.BUNDLE_CONTENTS, BundleContents.EMPTY);
        return new BundleContentsTooltip(contents, this.getGridWidth(items.size()), this.getGridHeight(items.size()));
    }

    public int getMaxAmountToAdd(ItemStack containerStack, ItemStack stackToAdd, Player player) {
        Fraction fraction = this.getCapacityMultiplier(containerStack)
                .subtract(this.computeContentWeight(containerStack, player));
        return Math.max(fraction.divideBy(BundleContents.getWeight(stackToAdd).getOrThrow()).intValue(), 0);
    }

    public Fraction computeContentWeight(ItemStack containerStack, Player player) {
        NonNullList<ItemStack> items = this.getItemContainer(containerStack, player, false).getItems();
        return BundleContents.computeContentWeight(items).getOrThrow();
    }

    @Override
    public Type<?> getType() {
        return ModRegistry.BUNDLE_ITEM_CONTENTS_PROVIDER_TYPE.value();
    }
}
