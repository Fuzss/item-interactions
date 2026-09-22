package fuzs.iteminteractions.common.api.v2.world.item.storage;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.registries.codec.RegistryCodecs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;

public record StorageOptions(Optional<HolderSet<Item>> items, boolean disallowed, boolean filterContainerItems) {
    public static final StorageOptions DEFAULT = new StorageOptions(Optional.empty(), true, true);
    public static final Codec<StorageOptions> CODEC = RecordCodecBuilder.create((RecordCodecBuilder.Instance<StorageOptions> instance) -> instance.group(
            RegistryCodecs.holderSet(Registries.ITEM)
                    .lenientOptionalFieldOf("items")
                    .forGetter(StorageOptions::items),
            Codec.BOOL.fieldOf("disallowed").orElse(Boolean.TRUE).forGetter(StorageOptions::disallowed),
            Codec.BOOL.fieldOf("filter_container_items")
                    .orElse(Boolean.TRUE)
                    .forGetter(StorageOptions::filterContainerItems)).apply(instance, StorageOptions::new));

    @Deprecated
    public StorageOptions setFilterContainerItems() {
        return this.setFilterContainerItems(true);
    }

    public StorageOptions setFilterContainerItems(boolean filterContainerItems) {
        return this.filterContainerItems() == filterContainerItems ? this :
                new StorageOptions(this.items, this.disallowed, filterContainerItems);
    }

    /**
     * @see net.minecraft.world.inventory.Slot#mayPlace(ItemStack)
     */
    public boolean mayPlace(ItemStack itemStack) {
        if (!this.canFitInsideContainerItems(itemStack)) {
            return false;
        } else if (!this.disallowed) {
            return this.items.isEmpty() || this.items.filter(itemStack::is).isPresent();
        } else {
            return this.items.filter(itemStack::is).isEmpty();
        }
    }

    private boolean canFitInsideContainerItems(ItemStack itemStack) {
        return !this.filterContainerItems() || itemStack.getItem().canFitInsideContainerItems();
    }
}
