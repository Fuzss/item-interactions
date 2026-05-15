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
import net.minecraft.world.item.Items;
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

    final int capacityMultiplier;

    public BundleContentsStorage() {
        this(1);
    }

    public BundleContentsStorage(int capacityMultiplier) {
        this(capacityMultiplier, StorageOptions.DEFAULT);
    }

    public BundleContentsStorage(int capacityMultiplier, StorageOptions storageOptions) {
        super(storageOptions);
        this.capacityMultiplier = capacityMultiplier;
    }

    protected static <T extends BundleContentsStorage> RecordCodecBuilder<T, Integer> capacityMultiplierCodec() {
        return ExtraCodecs.POSITIVE_INT.fieldOf("capacity_multiplier")
                .forGetter((T storage) -> storage.capacityMultiplier);
    }

    public Fraction getCapacityMultiplier(ItemStack itemStack) {
        return Fraction.getFraction(this.capacityMultiplier, 1);
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
        BundleContents contents = itemStack.getOrDefault(DataComponents.BUNDLE_CONTENTS, BundleContents.EMPTY);
        // TODO make this add in front again
        // Add one additional slot at the front, so we can add items in the inventory.
        // Adding items at the front is consistent with vanilla behavior.
        ItemStack[] itemStacks = Stream.concat(contents.itemCopyStream(), Stream.of(ItemStack.EMPTY))
                .toArray(ItemStack[]::new);
        NonNullList<ItemStack> itemList = NonNullList.of(ItemStack.EMPTY, itemStacks);
        return ContainerMenuHelper.createListBackedContainer(itemList, (Container container) -> {
            if (!isMutable) {
                throw new UnsupportedOperationException();
            }

            BundleContents updatedContents;
            if (container.isEmpty()) {
                updatedContents = BundleContents.EMPTY;
            } else {
                // empty stacks must not get in here, the codec will fail otherwise
                ImmutableList.Builder<ItemStackTemplate> builder = ImmutableList.builder();
                for (ItemStack item : itemList) {
                    if (!item.isEmpty()) {
                        builder.add(ItemStackTemplate.fromNonEmptyStack(item));
                    }
                }

                updatedContents = new BundleContents(builder.build());
            }

            itemStack.set(DataComponents.BUNDLE_CONTENTS, updatedContents);
        });
    }

    @Override
    public boolean canAddItem(ItemStack itemStack, ItemStack stackToAdd, Player player) {
        return this.getMaxAmountToAdd(itemStack, stackToAdd, player) > 0;
    }

    @Override
    public int getAcceptableItemCount(ItemStack itemStack, ItemStack stackToAdd, Player player) {
        return Math.min(this.getMaxAmountToAdd(itemStack, stackToAdd, player),
                super.getAcceptableItemCount(itemStack, stackToAdd, player));
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
    public void toggleSelectedItem(ItemStack itemStack, int selectedItem) {
        super.toggleSelectedItem(itemStack, selectedItem);
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
                this.getGridWidth(contents.size()),
                this.getGridHeight(contents.size()));
    }

    @Override
    public Optional<Boolean> isBarVisible(ItemStack itemStack, Player player) {
        return Optional.of(Items.BUNDLE.isBarVisible(itemStack));
    }

    @Override
    public OptionalInt getBarWidth(ItemStack itemStack, Player player) {
        return OptionalInt.of(Items.BUNDLE.getBarWidth(itemStack));
    }

    @Override
    public OptionalInt getBarColor(ItemStack itemStack, Player player) {
        return OptionalInt.of(Items.BUNDLE.getBarColor(itemStack));
    }

    /**
     * @see BundleContents.Mutable#getMaxAmountToAdd(Fraction)
     */
    public int getMaxAmountToAdd(ItemStack itemStack, ItemStack stackToAdd, Player player) {
        Fraction fraction = this.getCapacityMultiplier(itemStack)
                .subtract(this.computeContentWeight(itemStack, player));
        return Math.max(fraction.divideBy(BundleContents.getWeight(stackToAdd).getOrThrow()).intValue(), 0);
    }

    public Fraction computeContentWeight(ItemStack itemStack, Player player) {
        NonNullList<ItemStack> itemList = this.getItemContainer(itemStack, player, false).getItems();
        return BundleContents.computeContentWeight(itemList).getOrThrow();
    }

    @Override
    public ItemStorageType<?> getType() {
        return ModRegistry.BUNDLE_ITEM_CONTENTS_PROVIDER_TYPE.value();
    }
}
