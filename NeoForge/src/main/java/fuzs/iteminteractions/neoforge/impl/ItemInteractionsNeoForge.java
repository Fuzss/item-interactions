package fuzs.iteminteractions.neoforge.impl;

import fuzs.iteminteractions.impl.ItemInteractions;
import fuzs.iteminteractions.impl.data.client.ModLanguageProvider;
import fuzs.puzzleslib.api.core.v1.ModConstructor;
import fuzs.puzzleslib.neoforge.api.data.v2.core.DataProviderHelper;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLConstructModEvent;

@Mod(ItemInteractions.MOD_ID)
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class ItemInteractionsNeoForge {

    @SubscribeEvent
    public static void onConstructMod(final FMLConstructModEvent evt) {
        ModConstructor.construct(ItemInteractions.MOD_ID, ItemInteractions::new);
        DataProviderHelper.registerDataProviders(ItemInteractions.MOD_ID, ModLanguageProvider::new);
    }
}
