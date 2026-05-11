package fuzs.iteminteractions.common.impl.handler;

import fuzs.iteminteractions.common.impl.network.ClientboundEnderChestContentMessage;
import fuzs.iteminteractions.common.impl.network.ClientboundEnderChestSlotMessage;
import fuzs.iteminteractions.common.impl.network.client.ServerboundEnderChestContentMessage;
import fuzs.puzzleslib.common.api.network.v4.MessageSender;
import fuzs.puzzleslib.common.api.network.v4.PlayerSet;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.core.NonNullList;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class EnderChestSyncHandler {

    public static void onPlayerJoin(ServerPlayer player) {
        broadcastFullState(player);
    }

    public static void onAfterChangeDimension(ServerPlayer player, ServerLevel from, ServerLevel to) {
        broadcastFullState(player);
    }

    public static void onRespawn(ServerPlayer player, boolean originalStillAlive) {
        broadcastFullState(player);
    }

    public static void onContainerOpen(ServerPlayer serverPlayer, AbstractContainerMenu container) {
        if (container instanceof ChestMenu chestMenu
                && chestMenu.getContainer() == serverPlayer.getEnderChestInventory()) {
            broadcastFullState(serverPlayer);
            chestMenu.addSlotListener(new ContainerListener() {
                @Override
                public void slotChanged(AbstractContainerMenu menu, int slotIndex, ItemStack itemStack) {
                    // vanilla only syncs ender chest contents to open ender chest menu, but not to Player::getEnderChestInventory
                    // but since this is what we use for item interactions make sure to sync it
                    Slot slot = menu.getSlot(slotIndex);
                    if (slot.container == serverPlayer.getEnderChestInventory()) {
                        MessageSender.broadcast(PlayerSet.ofPlayer(serverPlayer),
                                new ClientboundEnderChestSlotMessage(slot.getContainerSlot(), itemStack));
                    }
                }

                @Override
                public void dataChanged(AbstractContainerMenu menu, int dataIndex, int dataValue) {
                    // NO-OP
                }
            });
        }
    }

    public static void broadcastCreativeState(Player player, NonNullList<ItemStack> items) {
        // this is only required for the creative mode inventory, as it doesn't sync contents using default menu packets,
        // instead it uses custom packets which do not work for item interactions in a menu
        if (player.hasInfiniteMaterials()
                && player.containerMenu instanceof CreativeModeInventoryScreen.ItemPickerMenu) {
            MessageSender.broadcast(new ServerboundEnderChestContentMessage(items));
        }
    }

    public static void broadcastFullState(ServerPlayer serverPlayer) {
        MessageSender.broadcast(PlayerSet.ofPlayer(serverPlayer),
                new ClientboundEnderChestContentMessage(serverPlayer.getEnderChestInventory().getItems()));
    }

    public static void setEnderChestContent(Player player, List<ItemStack> items) {
        PlayerEnderChestContainer enderChestInventory = player.getEnderChestInventory();
        // safeguard against mods only changing ender chest size on one side
        int size = Math.min(items.size(), enderChestInventory.getContainerSize());
        for (int i = 0; i < size; ++i) {
            enderChestInventory.setItem(i, items.get(i));
        }
    }
}
