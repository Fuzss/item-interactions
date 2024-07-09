package fuzs.iteminteractions.api.v1.provider.impl;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fuzs.iteminteractions.api.v1.DyeBackedColor;
import fuzs.iteminteractions.api.v1.provider.AbstractProvider;
import fuzs.iteminteractions.api.v1.tooltip.BundleContentsTooltip;
import fuzs.iteminteractions.impl.init.ModRegistry;
import fuzs.puzzleslib.api.container.v1.ContainerMenuHelper;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.HolderSet;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.BundleContents;
import org.apache.commons.lang3.math.Fraction;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class BundleProvider extends AbstractProvider {
    public static final String KEY_BUNDLE_CAPACITY = Items.BUNDLE.getDescriptionId() + ".capacity";
    public static final MapCodec<BundleProvider> CODEC = RecordCodecBuilder.mapCodec(instance -> {
        return instance.group(capacityMultiplierCodec(), backgroundColorCodec(), disallowedItemsCodec())
                .apply(instance,
                        (Integer capacityMultiplier, Optional<DyeBackedColor> dyeColor, HolderSet<Item> disallowedItems) -> {
                            return new BundleProvider(capacityMultiplier, dyeColor.orElse(null)).disallowedItems(
                                    disallowedItems);
                        }
                );
    });
    public static final Codec<Fraction> FRACTION_CODEC = RecordCodecBuilder.create(instance -> instance.group(Codec.INT.fieldOf(
                    "numerator").forGetter(Fraction::getNumerator),
            Codec.INT.fieldOf("denominator").forGetter(Fraction::getDenominator)
    ).apply(instance, Fraction::getFraction));
    private static final Codec<BundleContents> BUNDLE_CONTENTS_CODEC = Codec.either(RecordCodecBuilder.<BundleContents>create(
            instance -> instance.group(ItemStack.CODEC.listOf()
                            .fieldOf("items")
                            .forGetter(bundleContents -> bundleContents.itemCopyStream().toList()),
                    FRACTION_CODEC.fieldOf("fraction").forGetter(BundleContents::weight)
            ).apply(instance, BundleContents::new)), BundleContents.CODEC).xmap(Either::unwrap, Either::left);
    public static final StreamCodec<ByteBuf, Fraction> FRACTION_STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.VAR_INT,
            Fraction::getNumerator,
            ByteBufCodecs.VAR_INT,
            Fraction::getDenominator,
            Fraction::getFraction
    );
    private static final StreamCodec<RegistryFriendlyByteBuf, BundleContents> BUNDLE_CONTENTS_STREAM_CODEC = StreamCodec.composite(
            ItemStack.STREAM_CODEC.apply(ByteBufCodecs.list()),
            bundleContents -> bundleContents.itemCopyStream().toList(),
            FRACTION_STREAM_CODEC,
            BundleContents::weight,
            BundleContents::new
    );

    private final int capacityMultiplier;

    public BundleProvider(int capacityMultiplier, @Nullable DyeBackedColor dyeColor) {
        super(dyeColor);
        this.capacityMultiplier = capacityMultiplier;
    }

    /**
     * Changes {@link DataComponents#BUNDLE_CONTENTS} to serialize the weight in addition to the stored items. This
     * allows our custom weight calculations to be preserved.
     * <p>
     * The codec has a built-in fallback for the vanilla format, so that reading vanilla bundle contents is still fully
     * supported.
     */
    public static void setBundleContentsComponentCodecs(DataComponentType<BundleContents> dataComponentType) {
        ((DataComponentType.Builder.SimpleType<BundleContents>) dataComponentType).codec = BundleProvider.BUNDLE_CONTENTS_CODEC;
        ((DataComponentType.Builder.SimpleType<BundleContents>) dataComponentType).streamCodec = BundleProvider.BUNDLE_CONTENTS_STREAM_CODEC;
    }

    protected static <T extends BundleProvider> RecordCodecBuilder<T, Integer> capacityMultiplierCodec() {
        return ExtraCodecs.POSITIVE_INT.fieldOf("capacity_multiplier").forGetter(BundleProvider::getCapacityMultiplier);
    }

    @Override
    public BundleProvider disallowedItems(HolderSet<Item> disallowedItems) {
        return (BundleProvider) super.disallowedItems(disallowedItems);
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
                List<ItemStack> newItems = builder.build();
                Fraction fraction = BundleContents.computeContentWeight(newItems)
                        .divideBy(Fraction.getFraction(this.getCapacityMultiplier(), 1));
                newContents = new BundleContents(newItems, fraction);
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
        return new BundleContentsTooltip(items,
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
    public Type getType() {
        return ModRegistry.BUNDLE_ITEM_CONTENTS_PROVIDER_TYPE.value();
    }
}
