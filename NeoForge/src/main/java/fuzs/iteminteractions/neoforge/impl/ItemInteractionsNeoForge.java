package fuzs.iteminteractions.neoforge.impl;

import fuzs.iteminteractions.common.api.v2.world.item.storage.ItemStorage;
import fuzs.iteminteractions.common.impl.ItemInteractions;
import fuzs.iteminteractions.common.impl.data.ModItemStorageProvider;
import fuzs.puzzleslib.common.api.core.v1.ModConstructor;
import fuzs.puzzleslib.neoforge.api.data.v3.core.DataProviderBuilder;
import net.minecraft.server.packs.PackType;
import net.neoforged.fml.common.Mod;

@Mod(ItemInteractions.MOD_ID)
public class ItemInteractionsNeoForge {

    public ItemInteractionsNeoForge() {
        ModConstructor.construct(ItemInteractions.MOD_ID, ItemInteractions::new);
        // TODO This should be registered as a reloadable registry; when possible in NeoForge.
        DataProviderBuilder.ofBuiltIn(ItemInteractions.DEVELOPMENT_ID, PackType.SERVER_DATA)
                .add(ItemStorage.Definition.REGISTRY_KEY, new ModItemStorageProvider());
    }
}
