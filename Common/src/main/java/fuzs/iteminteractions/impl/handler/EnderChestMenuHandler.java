package fuzs.iteminteractions.impl.handler;

import fuzs.iteminteractions.impl.init.ModRegistry;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;

public class EnderChestMenuHandler {

    public static void onStartPlayerTick(Player player) {
        if (player.level().isClientSide) return;
        // vanilla only syncs ender chest contents to open ender chest menu, but not to Player::getEnderChestInventory
        // but since this is what we use for item interactions make sure to sync it
        if (player.containerMenu instanceof ChestMenu menu && menu.getContainer() == player.getEnderChestInventory()) {
            ModRegistry.ENDER_CHEST_MENU_CAPABILITY.get(player).getEnderChestMenu().broadcastChanges();
        }
    }

    public static AbstractContainerMenu openEnderChestMenu(Player player) {
        // container id doesn't matter since we do the syncing ourselves where the id is never used
        ChestMenu menu = ChestMenu.threeRows(-100, new Inventory(player), player.getEnderChestInventory());
        ModRegistry.ENDER_CHEST_MENU_CAPABILITY.get(player).setEnderChestMenu(menu);
        return menu;
    }
}
