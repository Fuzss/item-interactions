package fuzs.iteminteractions.neoforge.impl.client;

import fuzs.iteminteractions.impl.ItemInteractions;
import fuzs.iteminteractions.impl.client.ItemInteractionsClient;
import fuzs.iteminteractions.impl.data.client.ModLanguageProvider;
import fuzs.puzzleslib.api.client.core.v1.ClientModConstructor;
import fuzs.puzzleslib.neoforge.api.data.v2.core.DataProviderHelper;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLConstructModEvent;

@Mod.EventBusSubscriber(modid = ItemInteractions.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ItemInteractionsNeoForgeClient {

    @SubscribeEvent
    public static void onConstructMod(final FMLConstructModEvent evt) {
        ClientModConstructor.construct(ItemInteractions.MOD_ID, ItemInteractionsClient::new);
        DataProviderHelper.registerDataProviders(ItemInteractions.MOD_ID, ModLanguageProvider::new);
    }
}
