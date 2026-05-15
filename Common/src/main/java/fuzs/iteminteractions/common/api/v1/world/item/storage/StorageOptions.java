package fuzs.iteminteractions.common.api.v1.world.item.storage;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;

public record StorageOptions(Optional<HolderSet<Item>> items, boolean disallowed, boolean filterContainerItems) {
    public static final StorageOptions DEFAULT = new StorageOptions(Optional.empty(), false, false);
    public static final Codec<StorageOptions> CODEC = RecordCodecBuilder.create((RecordCodecBuilder.Instance<StorageOptions> instance) -> instance.group(
                    RegistryCodecs.homogeneousList(Registries.ITEM)
                            .lenientOptionalFieldOf("items")
                            .forGetter((StorageOptions storageOptions) -> storageOptions.items),
                    Codec.BOOL.lenientOptionalFieldOf("disallowed", false)
                            .forGetter((StorageOptions storageOptions) -> storageOptions.filterContainerItems),
                    Codec.BOOL.lenientOptionalFieldOf("filter_container_items", false)
                            .forGetter((StorageOptions storageOptions) -> storageOptions.filterContainerItems))
            .apply(instance, StorageOptions::new));

    public StorageOptions setFilterContainerItems() {
        return new StorageOptions(this.items, this.disallowed, true);
    }

    /**
     * @see Item#canFitInsideContainerItems()
     */
    public boolean canFitInsideContainerItem(ItemStack itemStack) {
        if (!this.disallowed) {
            return this.items.isEmpty() || this.items.filter(itemStack::is).isPresent();
        } else {
            boolean canFitInsideContainerItems =
                    !this.filterContainerItems || itemStack.getItem().canFitInsideContainerItems();
            return canFitInsideContainerItems && this.items.filter(itemStack::is).isEmpty();
        }
    }
}
