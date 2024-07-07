package fuzs.iteminteractions.impl.handler;

import fuzs.iteminteractions.impl.ItemInteractions;
import fuzs.iteminteractions.impl.network.S2CEnderChestContentMessage;
import fuzs.iteminteractions.impl.network.S2CEnderChestSlotMessage;
import net.minecraft.core.NonNullList;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;

public class EnderChestSyncHandler {

    public static void onLoggedIn(ServerPlayer player) {
        broadcastFullState(player);
    }

    public static void onAfterChangeDimension(ServerPlayer player, ServerLevel from, ServerLevel to) {
        broadcastFullState(player);
    }

    public static void onRespawn(ServerPlayer player, boolean originalStillAlive) {
        broadcastFullState(player);
    }

    public static void onContainerOpen(ServerPlayer player, AbstractContainerMenu container) {
        if (container instanceof ChestMenu chestMenu && chestMenu.getContainer() == player.getEnderChestInventory()) {
            broadcastFullState(player);
            chestMenu.addSlotListener(new ContainerListener() {
                @Override
                public void slotChanged(AbstractContainerMenu menu, int slotIndex, ItemStack itemStack) {
                    // vanilla only syncs ender chest contents to open ender chest menu, but not to Player::getEnderChestInventory
                    // but since this is what we use for item interactions make sure to sync it
                    Slot slot = menu.getSlot(slotIndex);
                    if (slot.container == player.getEnderChestInventory()) {
                        ItemInteractions.NETWORK.sendTo(new S2CEnderChestSlotMessage(slot.getContainerSlot(), itemStack),
                                player
                        );
                    }
                }

                @Override
                public void dataChanged(AbstractContainerMenu menu, int dataIndex, int dataValue) {
                    // NO-OP
                }
            });
        }
    }

    public static void broadcastFullState(ServerPlayer player) {
        ItemInteractions.NETWORK.sendTo(new S2CEnderChestContentMessage(player.getEnderChestInventory().items),
                player
        );
    }

    public static void setEnderChestContent(Player player, NonNullList<ItemStack> items) {
        PlayerEnderChestContainer enderChestInventory = player.getEnderChestInventory();
        // safeguard against mods only changing ender chest size on one side
        int size = Math.min(items.size(), enderChestInventory.getContainerSize());
        for (int i = 0; i < size; ++i) {
            enderChestInventory.setItem(i, items.get(i));
        }
    }
}
