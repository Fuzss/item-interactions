package fuzs.iteminteractions.impl.network;

import fuzs.iteminteractions.impl.init.ModRegistry;
import fuzs.puzzleslib.api.network.v2.MessageV2;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;

public class S2CEnderChestMenuMessage implements MessageV2<S2CEnderChestMenuMessage> {

    @Override
    public void write(FriendlyByteBuf buf) {

    }

    @Override
    public void read(FriendlyByteBuf buf) {

    }

    @Override
    public MessageHandler<S2CEnderChestMenuMessage> makeHandler() {
        return new MessageHandler<>() {

            @Override
            public void handle(S2CEnderChestMenuMessage message, Player player, Object gameInstance) {
                ModRegistry.ENDER_CHEST_MENU_CAPABILITY.get(player).initContainerMenu(false);
            }
        };
    }
}
