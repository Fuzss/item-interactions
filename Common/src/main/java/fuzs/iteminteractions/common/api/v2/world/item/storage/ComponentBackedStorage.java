package fuzs.iteminteractions.common.api.v2.world.item.storage;

import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.item.ItemStack;

public abstract class ComponentBackedStorage implements VisualItemStorage {
    final StorageOptions storageOptions;

    public ComponentBackedStorage(StorageOptions storageOptions) {
        this.storageOptions = storageOptions;
    }

    protected static <T extends ComponentBackedStorage> RecordCodecBuilder<T, StorageOptions> itemContentsCodec() {
        return StorageOptions.CODEC.lenientOptionalFieldOf("storage_options", StorageOptions.DEFAULT)
                .forGetter((T provider) -> provider.storageOptions);
    }

    @Override
    public boolean mayPlace(ItemStack otherItem) {
        return this.storageOptions.mayPlace(otherItem);
    }
}
