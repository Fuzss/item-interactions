package fuzs.iteminteractions.api.v1.provider.impl;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fuzs.iteminteractions.api.v1.DyeBackedColor;
import fuzs.iteminteractions.api.v1.provider.AbstractProvider;
import fuzs.iteminteractions.api.v1.tooltip.BundleContentsTooltip;
import fuzs.iteminteractions.impl.init.ModRegistry;
import fuzs.puzzleslib.api.container.v1.ContainerMenuHelper;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.BundleItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.BundleContents;
import org.apache.commons.lang3.math.Fraction;
import org.jspecify.annotations.Nullable;

import java.util.Optional;
import java.util.stream.Stream;

public class BundleProvider extends AbstractProvider {
    public static final MapCodec<BundleProvider> CODEC = RecordCodecBuilder.mapCodec(instance -> {
        return instance.group(capacityMultiplierCodec(), backgroundColorCodec(), itemContentsCodec())
                .apply(instance,
                        (Integer capacityMultiplier, Optional<DyeBackedColor> dyeColor, ItemContents itemContents) -> {
                            return new BundleProvider(capacityMultiplier, dyeColor.orElse(null)).itemContents(
                                    itemContents);
                        });
    });

    final int capacityMultiplier;

    public BundleProvider(@Nullable DyeBackedColor dyeColor) {
        this(1, dyeColor);
    }

    public BundleProvider(int capacityMultiplier, @Nullable DyeBackedColor dyeColor) {
        super(dyeColor);
        this.capacityMultiplier = capacityMultiplier;
    }

    protected static <T extends BundleProvider> RecordCodecBuilder<T, Integer> capacityMultiplierCodec() {
        return ExtraCodecs.POSITIVE_INT.fieldOf("capacity_multiplier")
                .forGetter(provider -> provider.capacityMultiplier);
    }

    @Override
    protected BundleProvider itemContents(ItemContents itemContents) {
        return (BundleProvider) super.itemContents(itemContents);
    }

    @Override
    public BundleProvider filterContainerItems(boolean filterContainerItems) {
        return (BundleProvider) super.filterContainerItems(filterContainerItems);
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
    public boolean canAddItem(ItemStack containerStack, ItemStack stackToAdd, Player player) {
        return this.getMaxAmountToAdd(containerStack, stackToAdd, player) > 0;
    }

    @Override
    public int getAcceptableItemCount(ItemStack containerStack, ItemStack stackToAdd, Player player) {
        return Math.min(this.getMaxAmountToAdd(containerStack, stackToAdd, player),
                super.getAcceptableItemCount(containerStack, stackToAdd, player));
    }

    @Override
    public boolean canProvideTooltipImage(ItemStack containerStack, Player player) {
        return true;
    }

    @Override
    public void onToggleSelectedItem(ItemStack containerStack, int oldSelectedItem, int newSelectedItem) {
        if (oldSelectedItem != newSelectedItem) {
            BundleItem.toggleSelectedItem(containerStack, newSelectedItem);
        }
    }

    @Override
    public TooltipComponent createTooltipImageComponent(ItemStack containerStack, Player player, NonNullList<ItemStack> items) {
        return new BundleContentsTooltip(items,
                this.computeContentWeight(containerStack, player).divideBy(this.getCapacityMultiplier(containerStack)),
                this.dyeColor);
    }

    public int getMaxAmountToAdd(ItemStack containerStack, ItemStack stackToAdd, Player player) {
        Fraction fraction = this.getCapacityMultiplier(containerStack)
                .subtract(this.computeContentWeight(containerStack, player));
        return Math.max(fraction.divideBy(BundleContents.getWeight(stackToAdd)).intValue(), 0);
    }

    public Fraction computeContentWeight(ItemStack containerStack, Player player) {
        SimpleContainer container = this.getItemContainer(containerStack, player, false);
        return BundleContents.computeContentWeight(container.getItems());
    }

    @Override
    public Type getType() {
        return ModRegistry.BUNDLE_ITEM_CONTENTS_PROVIDER_TYPE.value();
    }
}
