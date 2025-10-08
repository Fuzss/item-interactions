package fuzs.iteminteractions.api.v1.provider;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fuzs.iteminteractions.api.v1.DyeBackedColor;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public abstract class AbstractProvider implements TooltipProvider {
    @Nullable
    protected final DyeBackedColor dyeColor;
    ItemContents itemContents = ItemContents.EMPTY;

    protected AbstractProvider(@Nullable DyeBackedColor dyeColor) {
        this.dyeColor = dyeColor;
    }

    protected static <T extends AbstractProvider> RecordCodecBuilder<T, Optional<DyeBackedColor>> backgroundColorCodec() {
        return DyeBackedColor.CODEC.optionalFieldOf("background_color")
                .forGetter((T provider) -> Optional.ofNullable(provider.dyeColor));
    }

    protected static <T extends AbstractProvider> RecordCodecBuilder<T, ItemContents> itemContentsCodec() {
        return ItemContents.CODEC.lenientOptionalFieldOf("item_contents", ItemContents.EMPTY)
                .forGetter((T provider) -> provider.itemContents);
    }

    protected AbstractProvider itemContents(ItemContents itemContents) {
        this.itemContents = itemContents;
        return this;
    }

    public AbstractProvider filterContainerItems(boolean filterContainerItems) {
        return this.itemContents(this.itemContents.filterContainerItems(filterContainerItems));
    }

    @Override
    public boolean isItemAllowedInContainer(ItemStack stackToAdd) {
        return this.itemContents.canFitInsideContainerItem(stackToAdd);
    }

    protected record ItemContents(Optional<HolderSet<Item>> items, boolean disallow, boolean filterContainerItems) {
        public static final ItemContents EMPTY = new ItemContents(Optional.empty(), false, false);
        public static final Codec<ItemContents> CODEC = RecordCodecBuilder.create((RecordCodecBuilder.Instance<ItemContents> instance) -> instance.group(
                        RegistryCodecs.homogeneousList(Registries.ITEM)
                                .lenientOptionalFieldOf("items")
                                .forGetter((ItemContents itemContents) -> itemContents.items),
                        Codec.BOOL.lenientOptionalFieldOf("disallow", false)
                                .forGetter((ItemContents itemContents) -> itemContents.filterContainerItems),
                        Codec.BOOL.lenientOptionalFieldOf("filter_container_items", false)
                                .forGetter((ItemContents itemContents) -> itemContents.filterContainerItems))
                .apply(instance, ItemContents::new));

        public ItemContents filterContainerItems(boolean filterContainerItems) {
            return new ItemContents(this.items, this.disallow, filterContainerItems);
        }

        public boolean canFitInsideContainerItem(ItemStack itemStack) {
            if (!this.disallow) {
                return this.items.isEmpty() || this.items.filter(itemStack::is).isPresent();
            } else {
                boolean canFitInsideContainerItems =
                        !this.filterContainerItems || itemStack.getItem().canFitInsideContainerItems();
                return canFitInsideContainerItems && this.items.filter(itemStack::is).isEmpty();
            }
        }
    }
}
