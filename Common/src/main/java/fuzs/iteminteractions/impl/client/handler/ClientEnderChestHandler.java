package fuzs.iteminteractions.impl.client.handler;

import fuzs.iteminteractions.impl.ItemInteractions;
import fuzs.iteminteractions.impl.network.client.C2SEnderChestContentMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;

public class ClientEnderChestHandler {

    public static void broadcastFullState(NonNullList<ItemStack> items) {
        // this is only required for the creative mode inventory, as it doesn't sync contents using default menu packets,
        // instead it uses custom packets which do not work for item interactions in a menu
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.gameMode.hasInfiniteItems() &&
                minecraft.player.containerMenu instanceof CreativeModeInventoryScreen.ItemPickerMenu) {
            ItemInteractions.NETWORK.sendMessage(new C2SEnderChestContentMessage(items).toServerboundMessage());
        }
    }
}
