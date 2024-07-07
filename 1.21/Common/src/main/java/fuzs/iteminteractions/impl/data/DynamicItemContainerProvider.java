package fuzs.iteminteractions.impl.data;

import fuzs.iteminteractions.api.v1.data.AbstractItemContainerProvider;
import fuzs.iteminteractions.api.v1.provider.BlockEntityProvider;
import fuzs.iteminteractions.api.v1.provider.EnderChestProvider;
import fuzs.puzzleslib.api.data.v2.core.DataProviderContext;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class DynamicItemContainerProvider extends AbstractItemContainerProvider {

    public DynamicItemContainerProvider(DataProviderContext context) {
        super(context);
    }

    @Override
    public void addItemProviders() {
        this.add(Items.ENDER_CHEST, new EnderChestProvider());
        this.add(Items.SHULKER_BOX, BlockEntityProvider.shulkerBoxProvider(BlockEntityType.SHULKER_BOX, 9, 3, null));
    }
}
