package fuzs.iteminteractions.common.impl.data;

import fuzs.iteminteractions.common.api.v1.world.item.DyeBackedColor;
import fuzs.iteminteractions.common.api.v1.data.AbstractItemContentsProvider;
import fuzs.iteminteractions.common.api.v1.world.item.storage.BundleItemStorage;
import fuzs.iteminteractions.common.api.v1.world.item.storage.ContainerItemStorage;
import fuzs.iteminteractions.common.api.v1.world.item.storage.EnderChestItemStorage;
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
        this.add(new EnderChestItemStorage(), Items.ENDER_CHEST);
        this.add(registries.lookupOrThrow(Registries.ITEM),
                new ContainerItemStorage(9, 3).filterContainerItems(true),
                ItemTags.SHULKER_BOXES);
        this.add(ItemInteractions.id("bundle"),
                new BundleItemStorage(DyeBackedColor.fromRgb(0XFC7703)).filterContainerItems(true),
                Items.BUNDLE,
                Items.SADDLE);
    }
}
