package fuzs.iteminteractions.neoforge.impl;

import fuzs.iteminteractions.common.api.v1.world.inventory.ItemClickedInMenuCallback;
import fuzs.iteminteractions.common.impl.ItemInteractions;
import fuzs.puzzleslib.common.api.core.v1.ModConstructor;
import fuzs.puzzleslib.common.api.event.v1.core.EventResult;
import fuzs.puzzleslib.neoforge.api.event.v1.core.NeoForgeEventInvokerRegistry;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.ItemStackedOnOtherEvent;

@Mod(ItemInteractions.MOD_ID)
public class ItemInteractionsNeoForge {

    public ItemInteractionsNeoForge() {
        ModConstructor.construct(ItemInteractions.MOD_ID, ItemInteractions::new);
        NeoForgeEventInvokerRegistry.INSTANCE.register(ItemClickedInMenuCallback.class,
                ItemStackedOnOtherEvent.class,
                (ItemClickedInMenuCallback callback, ItemStackedOnOtherEvent event) -> {
                    EventResult eventResult = callback.onItemClickedInMenu(event.getStackedOnItem(),
                            event.getSlot(),
                            event.getCarriedItem(),
                            event.getCarriedSlotAccess(),
                            event.getClickAction(),
                            event.getPlayer());
                    if (eventResult.isInterrupt()) {
                        event.setCanceled(true);
                    }
                });
    }
}
