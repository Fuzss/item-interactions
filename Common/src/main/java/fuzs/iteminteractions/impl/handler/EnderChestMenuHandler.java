package fuzs.iteminteractions.impl.handler;

import fuzs.iteminteractions.impl.init.ModRegistry;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.ContainerListener;
import net.minecraft.world.item.ItemStack;

public class EnderChestMenuHandler {

    public static void onContainerOpen(ServerPlayer player, AbstractContainerMenu container) {
        if (container instanceof ChestMenu chestMenu && chestMenu.getContainer() == player.getEnderChestInventory()) {
            AbstractContainerMenu enderChestMenu = ModRegistry.ENDER_CHEST_MENU_CAPABILITY.get(player).getEnderChestMenu();
            enderChestMenu.broadcastFullState();
            chestMenu.addSlotListener(new ContainerListener() {
                @Override
                public void slotChanged(AbstractContainerMenu containerToSend, int dataSlotIndex, ItemStack stack) {
                    // vanilla only syncs ender chest contents to open ender chest menu, but not to Player::getEnderChestInventory
                    // but since this is what we use for item interactions make sure to sync it
                    if (containerToSend.getSlot(dataSlotIndex).container == player.getEnderChestInventory()) {
                        enderChestMenu.broadcastChanges();
                    }
                }

                @Override
                public void dataChanged(AbstractContainerMenu containerMenu, int dataSlotIndex, int value) {
                    // NO-OP
                }
            });
        }
    }
}
