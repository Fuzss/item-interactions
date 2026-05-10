package fuzs.iteminteractions.common.impl.data;

import fuzs.iteminteractions.common.api.v1.DyeBackedColor;
import fuzs.iteminteractions.common.api.v1.data.AbstractItemContentsProvider;
import fuzs.iteminteractions.common.api.v1.provider.impl.BundleProvider;
import fuzs.iteminteractions.common.api.v1.provider.impl.ContainerProvider;
import fuzs.iteminteractions.common.api.v1.provider.impl.EnderChestProvider;
import fuzs.iteminteractions.common.impl.ItemInteractions;
import fuzs.puzzleslib.common.api.data.v2.core.DataProviderContext;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Items;

public class DynamicItemContentsProvider extends AbstractItemContentsProvider {

    public DynamicItemContentsProvider(DataProviderContext context) {
        super(context);
    }

    @Override
    public void addItemProviders(HolderLookup.Provider registries) {
        this.add(new EnderChestProvider(), Items.ENDER_CHEST);
        this.add(registries.lookupOrThrow(Registries.ITEM),
                new ContainerProvider(9, 3).filterContainerItems(true),
                ItemTags.SHULKER_BOXES);
        this.add(ItemInteractions.id("bundle"),
                new BundleProvider(DyeBackedColor.fromRgb(0XFC7703)).filterContainerItems(true),
                Items.BUNDLE,
                Items.SADDLE);
    }
}
