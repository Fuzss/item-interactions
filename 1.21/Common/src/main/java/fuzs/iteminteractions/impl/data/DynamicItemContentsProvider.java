package fuzs.iteminteractions.impl.data;

import fuzs.iteminteractions.api.v1.DyeBackedColor;
import fuzs.iteminteractions.api.v1.data.AbstractItemContentsProvider;
import fuzs.iteminteractions.api.v1.provider.impl.BundleProvider;
import fuzs.iteminteractions.api.v1.provider.impl.EnderChestProvider;
import fuzs.iteminteractions.api.v1.provider.impl.ContainerProvider;
import fuzs.puzzleslib.api.data.v2.core.DataProviderContext;
import net.minecraft.world.item.Items;

public class DynamicItemContentsProvider extends AbstractItemContentsProvider {

    public DynamicItemContentsProvider(DataProviderContext context) {
        super(context);
    }

    @Override
    public void addItemProviders() {
        this.add(Items.ENDER_CHEST, new EnderChestProvider());
        this.add(Items.SHULKER_BOX, new ContainerProvider(9, 3).filterContainerItems(true));
        this.add(Items.BUNDLE, new BundleProvider(1, DyeBackedColor.fromRgb(0XFCBA03)));
    }
}
