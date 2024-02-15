package fuzs.iteminteractions.impl.capability;

import fuzs.puzzleslib.api.capability.v3.data.CapabilityComponent;
import net.minecraft.world.entity.player.Player;

public class ContainerClientInputCapability extends CapabilityComponent<Player> {
    private int currentSlot = -1;
    private boolean modifierActive;

    public int getCurrentSlot() {
        return this.currentSlot;
    }

    public void setCurrentSlot(int currentSlot) {
        if (this.currentSlot != currentSlot) {
            this.currentSlot = currentSlot;
            this.setChanged();
        }
    }

    public boolean extractSingleItemOnly() {
        return this.modifierActive;
    }

    public void extractSingleItem(boolean singleItem) {
        if (this.modifierActive != singleItem) {
            this.modifierActive = singleItem;
            this.setChanged();
        }
    }
}
