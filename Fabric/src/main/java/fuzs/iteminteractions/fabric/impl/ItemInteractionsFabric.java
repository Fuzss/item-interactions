package fuzs.iteminteractions.fabric.impl;

import fuzs.iteminteractions.common.api.v1.world.inventory.ItemClickedInMenuCallback;
import fuzs.iteminteractions.common.impl.ItemInteractions;
import fuzs.puzzleslib.common.api.core.v1.ModConstructor;
import fuzs.puzzleslib.fabric.api.event.v1.core.FabricEventFactory;
import fuzs.puzzleslib.fabric.api.event.v1.core.FabricEventInvokerRegistry;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.Event;

public class ItemInteractionsFabric implements ModInitializer {
    public static final Event<ItemClickedInMenuCallback> ITEM_CLICKED_IN_MENU_EVENT = FabricEventFactory.createResult(
            ItemClickedInMenuCallback.class);

    @Override
    public void onInitialize() {
        ModConstructor.construct(ItemInteractions.MOD_ID, ItemInteractions::new);
        FabricEventInvokerRegistry.INSTANCE.register(ItemClickedInMenuCallback.class, ITEM_CLICKED_IN_MENU_EVENT);
    }
}
