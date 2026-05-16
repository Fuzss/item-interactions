package fuzs.iteminteractions.common.api.v2.world.item.storage;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fuzs.iteminteractions.common.api.v2.world.inventory.tooltip.BundleContentsTooltip;
import fuzs.iteminteractions.common.impl.init.ModRegistry;
import fuzs.puzzleslib.common.api.container.v1.ContainerMenuHelper;
import net.minecraft.core.Direction;
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

import java.util.Optional;
import java.util.OptionalInt;
import java.util.stream.Stream;

public class BundleContentsStorage extends ComponentBackedStorage {
    public static final MapCodec<BundleContentsStorage> CODEC = RecordCodecBuilder.mapCodec(instance -> {
        return instance.group(capacityMultiplierCodec(), itemContentsCodec())
                .apply(instance, BundleContentsStorage::new);
    });
    public static final int DEFAULT_CAPACITY_MULTIPLIER = 1;

    final int capacityMultiplier;

    public BundleContentsStorage() {
        this(StorageOptions.DEFAULT);
    }

    public BundleContentsStorage(StorageOptions storageOptions) {
        this(DEFAULT_CAPACITY_MULTIPLIER, storageOptions);
    }

    public BundleContentsStorage(int capacityMultiplier, StorageOptions storageOptions) {
        super(storageOptions);
        Preconditions.checkArgument(capacityMultiplier > 0, "capacity multiplier must be positive");
        this.capacityMultiplier = capacityMultiplier;
    }

    protected static <T extends BundleContentsStorage> RecordCodecBuilder<T, Integer> capacityMultiplierCodec() {
        return ExtraCodecs.POSITIVE_INT.fieldOf("capacity_multiplier")
                .forGetter((T storage) -> storage.capacityMultiplier);
    }

    public Fraction getMaxWeight(ItemStack itemStack) {
        return Fraction.getFraction(this.capacityMultiplier, 1);
    }

    public Fraction getWeight(ItemStack itemStack) {
        BundleContents contents = itemStack.getOrDefault(DataComponents.BUNDLE_CONTENTS, BundleContents.EMPTY);
        return BundleItem.getWeightSafe(contents);
    }

    public Fraction getRelativeWeight(ItemStack itemStack) {
        return this.getWeight(itemStack).divideBy(this.getMaxWeight(itemStack));
    }

    @Override
    public boolean hasContents(ItemStack itemStack) {
        return !itemStack.getOrDefault(DataComponents.BUNDLE_CONTENTS, BundleContents.EMPTY).isEmpty();
    }

    @Override
    public boolean canPlayerInteractWith(ItemStack itemStack, Player player) {
        return itemStack.has(DataComponents.BUNDLE_CONTENTS) && super.canPlayerInteractWith(itemStack, player);
    }

    @Override
    public SimpleContainer getItemContainer(ItemStack itemStack, boolean isMutable) {
        NonNullList<ItemStack> itemList = NonNullList.of(ItemStack.EMPTY,
                this.getItemStream(itemStack, isMutable).toArray(ItemStack[]::new));
        return ContainerMenuHelper.createListBackedContainer(itemList, (Container container) -> {
            if (isMutable) {
                itemStack.set(DataComponents.BUNDLE_CONTENTS, this.createUpdatedContents(container, itemList));
            } else {
                throw new UnsupportedOperationException();
            }
        });
    }

    private Stream<ItemStack> getItemStream(ItemStack itemStack, boolean isMutable) {
        Stream<ItemStack> itemStream = itemStack.getOrDefault(DataComponents.BUNDLE_CONTENTS, BundleContents.EMPTY)
                .itemCopyStream();
        if (isMutable) {
            // Add one additional slot, so we can add items in the inventory.
            return switch (this.getRemovalDirection()) {
                case POSITIVE -> Stream.concat(Stream.of(ItemStack.EMPTY), itemStream);
                case NEGATIVE -> Stream.concat(itemStream, Stream.of(ItemStack.EMPTY));
            };
        } else {
            return itemStream;
        }
    }

    private BundleContents createUpdatedContents(Container container, NonNullList<ItemStack> itemList) {
        if (container.isEmpty()) {
            return BundleContents.EMPTY;
        } else {
            ImmutableList.Builder<ItemStackTemplate> builder = ImmutableList.builder();
            for (ItemStack item : itemList) {
                if (!item.isEmpty()) {
                    builder.add(ItemStackTemplate.fromNonEmptyStack(item));
                }
            }

            // We discard the selected item, but as this is only triggered when the container has changed, the selected item would have been erased anyways in vanilla.
            return new BundleContents(builder.build());
        }
    }

    @Override
    public boolean canAddItem(ItemStack itemStack, ItemStack otherItem, Player player) {
        return this.getMaxAmountToAdd(itemStack, otherItem) > 0;
    }

    @Override
    public int getAcceptableItemCount(ItemStack itemStack, ItemStack otherItem, Player player) {
        return Math.min(this.getMaxAmountToAdd(itemStack, otherItem),
                super.getAcceptableItemCount(itemStack, otherItem, player));
    }

    /**
     * @see BundleContents.Mutable#getMaxAmountToAdd(Fraction)
     * @see BundleContents.Mutable#tryInsert(ItemStack)
     */
    public int getMaxAmountToAdd(ItemStack itemStack, ItemStack otherItem) {
        Fraction itemWeight = this.getWeight(itemStack);
        Fraction remainingWeight = this.getMaxWeight(itemStack).subtract(itemWeight);
        DataResult<Fraction> otherItemWeight = BundleContents.getWeight(otherItem);
        if (otherItemWeight.isError()) {
            return 0;
        } else {
            return Math.max(remainingWeight.divideBy(otherItemWeight.getOrThrow()).intValue(), 0);
        }
    }

    /**
     * Adding items at the front is consistent with vanilla behavior.
     * <p>
     * This requires additional handling when dealing with the selected item.
     */
    @Override
    public Direction.AxisDirection getRemovalDirection() {
        return Direction.AxisDirection.POSITIVE;
    }

    @Override
    public int getGridWidth(int itemCount) {
        return Math.max(1, Mth.ceil(Math.sqrt(itemCount)));
    }

    @Override
    public int getGridHeight(int itemCount) {
        return Mth.positiveCeilDiv(itemCount, this.getGridWidth(itemCount));
    }

    @Override
    public void toggleSelectedItem(ItemStack itemStack, int selectedItem, boolean slotClicked) {
        this.setSelectedItem(itemStack, selectedItem);
        BundleItem.toggleSelectedItem(itemStack, selectedItem);
    }

    @Override
    public Optional<Optional<TooltipComponent>> getTooltipImage(ItemStack itemStack, Player player) {
        return Optional.of(Optional.of(this.createTooltipImageComponent(itemStack, player, NonNullList.create())));
    }

    @Override
    public TooltipComponent createTooltipImageComponent(ItemStack itemStack, Player player, NonNullList<ItemStack> itemList) {
        BundleContents contents = itemStack.getOrDefault(DataComponents.BUNDLE_CONTENTS, BundleContents.EMPTY);
        return new BundleContentsTooltip(contents,
                this.getRelativeWeight(itemStack),
                this.getGridWidth(contents.size()),
                this.getGridHeight(contents.size()));
    }

    /**
     * @see net.minecraft.world.item.BundleItem#isBarVisible(ItemStack)
     */
    @Override
    public Optional<Boolean> isBarVisible(ItemStack itemStack, Player player) {
        return Optional.of(this.getRelativeWeight(itemStack).compareTo(Fraction.ZERO) > 0);
    }

    /**
     * @see net.minecraft.world.item.BundleItem#getBarWidth(ItemStack)
     */
    @Override
    public OptionalInt getBarWidth(ItemStack itemStack, Player player) {
        return OptionalInt.of(Math.min(1 + Mth.mulAndTruncate(this.getRelativeWeight(itemStack), 12), 13));
    }

    /**
     * @see net.minecraft.world.item.BundleItem#getBarColor(ItemStack)
     */
    @Override
    public OptionalInt getBarColor(ItemStack itemStack, Player player) {
        boolean isFull = this.getRelativeWeight(itemStack).compareTo(Fraction.ONE) >= 0;
        return OptionalInt.of(isFull ? BundleItem.FULL_BAR_COLOR : BundleItem.BAR_COLOR);
    }

    @Override
    public ItemStorageType<?> getType() {
        return ModRegistry.BUNDLE_ITEM_STORAGE_TYPE.value();
    }
}
