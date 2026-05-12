package fuzs.iteminteractions.common.api.v1.world.item.storage;

import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.item.ItemStack;

public abstract class ComponentBackedStorage implements ItemStorageWithTooltip {
    StorageOptions storageOptions = StorageOptions.EMPTY;

    protected static <T extends ComponentBackedStorage> RecordCodecBuilder<T, StorageOptions> itemContentsCodec() {
        return StorageOptions.CODEC.lenientOptionalFieldOf("item_contents", StorageOptions.EMPTY)
                .forGetter((T provider) -> provider.storageOptions);
    }

    protected ComponentBackedStorage storageOptions(StorageOptions storageOptions) {
        this.storageOptions = storageOptions;
        return this;
    }

    public ComponentBackedStorage filterContainerItems(boolean filterContainerItems) {
        return this.storageOptions(this.storageOptions.filterContainerItems(filterContainerItems));
    }

    @Override
    public abstract boolean hasContents(ItemStack itemStack);

    @Override
    public boolean isItemAllowedInContainer(ItemStack stackToAdd) {
        return this.storageOptions.canFitInsideContainerItem(stackToAdd);
    }
}
