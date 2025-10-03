package fuzs.iteminteractions.impl.data;

import fuzs.iteminteractions.api.v1.DyeBackedColor;
import fuzs.iteminteractions.api.v1.data.AbstractItemContentsProvider;
import fuzs.iteminteractions.api.v1.provider.impl.BundleProvider;
import fuzs.iteminteractions.api.v1.provider.impl.ContainerProvider;
import fuzs.iteminteractions.api.v1.provider.impl.EnderChestProvider;
import fuzs.puzzleslib.api.data.v2.core.DataProviderContext;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

public class DynamicItemContentsProvider extends AbstractItemContentsProvider {

    public DynamicItemContentsProvider(DataProviderContext context) {
        super(context);
    }

    @Override
    public void addItemProviders(HolderLookup.Provider registries) {
        HolderLookup.RegistryLookup<Item> items = registries.lookupOrThrow(Registries.ITEM);
        this.add(items, new EnderChestProvider(), Items.ENDER_CHEST);
        this.add(items, new ContainerProvider(9, 3).filterContainerItems(true), ItemTags.SHULKER_BOXES);
        this.add(items,
                "bundle",
                new BundleProvider(DyeBackedColor.fromRgb(0XFC7703)).filterContainerItems(true),
                Items.BUNDLE,
                Items.SADDLE);
    }
}
