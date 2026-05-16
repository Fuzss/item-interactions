package fuzs.iteminteractions.common.api.v2.world.item.storage;

import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
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
    public abstract boolean hasContents(ItemStack itemStack, Player player);

    @Override
    public boolean isItemAllowedInContainer(ItemStack otherItem) {
        return this.storageOptions.canFitInsideContainerItem(otherItem);
    }

    @Override
    public final SimpleContainer getItemContainer(ItemStack itemStack, Player player, boolean isMutable) {
        return this.getItemContainer(itemStack, isMutable);
    }

    public abstract SimpleContainer getItemContainer(ItemStack itemStack, boolean isMutable);
}
