package fuzs.iteminteractions.impl.capability;

import fuzs.puzzleslib.api.capability.v3.data.CapabilityComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;

public class EnderChestMenuCapability extends CapabilityComponent<Player> {
    private AbstractContainerMenu menu;

    public void setEnderChestMenu(AbstractContainerMenu menu) {
        if (this.menu != menu) {
            this.menu = menu;
            this.setChanged();
        }
    }

    public AbstractContainerMenu getEnderChestMenu() {
        return this.menu;
    }
}
