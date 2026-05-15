package fuzs.iteminteractions.common.impl.client.handler;

import fuzs.iteminteractions.common.impl.ItemInteractions;
import fuzs.iteminteractions.common.impl.config.ClientConfig;
import fuzs.iteminteractions.common.impl.init.ModRegistry;
import fuzs.iteminteractions.common.impl.network.client.ServerboundContainerClientInputMessage;
import fuzs.puzzleslib.common.api.network.v4.MessageSender;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.player.Player;

public class ClientInputActionHandler {
    private static boolean lastSentSingleItemOnly;

    /**
     * @see MultiPlayerGameMode#ensureHasSentCarriedItem()
     */
    public static void ensureHasSentContainerClientInput(Player player) {
        boolean singleItemOnly = ItemInteractions.CONFIG.get(ClientConfig.class).extractSingleItemOnly();
        if (singleItemOnly != lastSentSingleItemOnly) {
            lastSentSingleItemOnly = singleItemOnly;
            ModRegistry.MOVE_SINGLE_ITEM_ATTACHMENT_TYPE.set(player, singleItemOnly ? Unit.INSTANCE : null);
            MessageSender.broadcast(new ServerboundContainerClientInputMessage(singleItemOnly));
        }
    }
}
