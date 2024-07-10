package fuzs.iteminteractions.impl.data;

import fuzs.iteminteractions.api.v1.DyeBackedColor;
import fuzs.iteminteractions.api.v1.data.AbstractItemContentsProvider;
import fuzs.iteminteractions.api.v1.provider.impl.BundleProvider;
import fuzs.iteminteractions.api.v1.provider.impl.ContainerProvider;
import fuzs.iteminteractions.api.v1.provider.impl.EnderChestProvider;
import fuzs.puzzleslib.api.data.v2.core.DataProviderContext;
import net.minecraft.world.item.Items;

public class DynamicItemContentsProvider extends AbstractItemContentsProvider {

    public DynamicItemContentsProvider(DataProviderContext context) {
        super(context);
    }

    @Override
    public void addItemProviders() {
        this.add(new EnderChestProvider(), Items.ENDER_CHEST);
        this.add(new ContainerProvider(9, 3).filterContainerItems(true), Items.SHULKER_BOX);
        this.add("bundle", new BundleProvider(DyeBackedColor.fromRgb(0XFC7703)), Items.BUNDLE, Items.SADDLE);
    }
}
