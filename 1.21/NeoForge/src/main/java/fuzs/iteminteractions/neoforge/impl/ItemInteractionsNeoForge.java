package fuzs.iteminteractions.neoforge.impl;

import fuzs.iteminteractions.impl.ItemInteractions;
import fuzs.iteminteractions.impl.data.DynamicItemContentsProvider;
import fuzs.puzzleslib.api.core.v1.ModConstructor;
import fuzs.puzzleslib.neoforge.api.data.v2.core.DataProviderHelper;
import net.neoforged.fml.common.Mod;

@Mod(ItemInteractions.MOD_ID)
public class ItemInteractionsNeoForge {

    public ItemInteractionsNeoForge() {
        ModConstructor.construct(ItemInteractions.MOD_ID, ItemInteractions::new);
        DataProviderHelper.registerDataProviders(ItemInteractions.MOD_ID, DynamicItemContentsProvider::new);
    }
}
