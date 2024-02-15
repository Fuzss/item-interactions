package fuzs.iteminteractions.impl.init;

import fuzs.iteminteractions.impl.ItemInteractions;
import fuzs.iteminteractions.impl.capability.ContainerClientInputCapability;
import fuzs.iteminteractions.impl.capability.EnderChestMenuCapability;
import fuzs.puzzleslib.api.capability.v3.CapabilityController;
import fuzs.puzzleslib.api.capability.v3.data.CopyStrategy;
import fuzs.puzzleslib.api.capability.v3.data.EntityCapabilityKey;
import net.minecraft.world.entity.player.Player;

public class ModRegistry {
    static final CapabilityController CAPABILITIES = CapabilityController.from(ItemInteractions.MOD_ID);
    public static final EntityCapabilityKey<Player, ContainerClientInputCapability> CONTAINER_SLOT_CAPABILITY = CAPABILITIES.registerEntityCapability(
            "container_client_input",
            ContainerClientInputCapability.class,
            ContainerClientInputCapability::new,
            Player.class
    ).setCopyStrategy(CopyStrategy.ALWAYS);
    public static final EntityCapabilityKey<Player, EnderChestMenuCapability> ENDER_CHEST_MENU_CAPABILITY = CAPABILITIES.registerEntityCapability(
            "ender_chest_menu",
            EnderChestMenuCapability.class,
            EnderChestMenuCapability::new,
            Player.class
    );

    public static void touch() {

    }
}
