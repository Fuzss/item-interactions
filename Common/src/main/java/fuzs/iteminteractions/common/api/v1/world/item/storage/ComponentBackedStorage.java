package fuzs.iteminteractions.common.api.v1.world.item.storage;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;

public abstract class ComponentBackedStorage implements ItemStorageWithTooltip {
    ItemContents itemContents = ItemContents.EMPTY;

    protected static <T extends ComponentBackedStorage> RecordCodecBuilder<T, ItemContents> itemContentsCodec() {
        return ItemContents.CODEC.lenientOptionalFieldOf("item_contents", ItemContents.EMPTY)
                .forGetter((T provider) -> provider.itemContents);
    }

    protected ComponentBackedStorage itemContents(ItemContents itemContents) {
        this.itemContents = itemContents;
        return this;
    }

    public ComponentBackedStorage filterContainerItems(boolean filterContainerItems) {
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
