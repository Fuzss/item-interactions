package fuzs.iteminteractions.impl.capability;

import fuzs.iteminteractions.impl.ItemInteractions;
import fuzs.iteminteractions.impl.network.S2CEnderChestMenuMessage;
import fuzs.iteminteractions.impl.world.inventory.EnderChestSynchronizer;
import fuzs.puzzleslib.api.capability.v3.data.CapabilityComponent;
import fuzs.puzzleslib.api.network.v3.PlayerSet;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ClientCommonPacketListener;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import org.jetbrains.annotations.Nullable;

public class EnderChestMenuCapability extends CapabilityComponent<Player> {
    private AbstractContainerMenu menu;

    @Override
    public void setChanged(@Nullable PlayerSet playerSet) {
        if (playerSet != null && !this.getHolder().level().isClientSide) {
            Packet<ClientCommonPacketListener> packet = ItemInteractions.NETWORK.toClientboundPacket(new S2CEnderChestMenuMessage());
            playerSet.notify(packet);
            // run this after our custom message so the menu has been created client-side and is ready for having contents synced to it
            this.initContainerMenu(true);
        }

        super.setChanged(playerSet);
    }

    public void initContainerMenu(boolean attachSynchronizer) {
        if (this.menu == null) {
            // just some dummy inventory that does not change (everything is empty by default)
            Inventory inventory = new Inventory(this.getHolder());
            // container id doesn't matter since we do the syncing ourselves where the id is never used
            this.menu = ChestMenu.threeRows(-1, inventory, this.getHolder().getEnderChestInventory());
            if (attachSynchronizer) {
                this.menu.setSynchronizer(new EnderChestSynchronizer((ServerPlayer) this.getHolder()));
            }
        }
    }

    public AbstractContainerMenu getEnderChestMenu() {
        return this.menu;
    }
}
