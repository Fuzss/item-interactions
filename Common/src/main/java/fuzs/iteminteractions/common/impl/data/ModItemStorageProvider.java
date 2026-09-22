package fuzs.iteminteractions.common.impl.data;

import fuzs.iteminteractions.common.api.v2.data.ItemStorageProvider;
import fuzs.iteminteractions.common.api.v2.world.item.storage.BundleContentsStorage;
import fuzs.iteminteractions.common.api.v2.world.item.storage.ContainerStorage;
import fuzs.iteminteractions.common.api.v2.world.item.storage.EnderChestStorage;
import fuzs.iteminteractions.common.api.v2.world.item.storage.ItemStorage;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Items;

public class ModItemStorageProvider implements ItemStorageProvider {
    @Override
    public void run(BootstrapContext<ItemStorage.Definition> output) {
        this.add(output, EnderChestStorage.INSTANCE, Items.ENDER_CHEST);
        this.add(output, new ContainerStorage(), ItemTags.SHULKER_BOXES);
        this.add(output, new BundleContentsStorage(), Items.BUNDLE);
    }
}
