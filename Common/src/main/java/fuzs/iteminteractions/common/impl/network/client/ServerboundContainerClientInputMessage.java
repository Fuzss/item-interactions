package fuzs.iteminteractions.common.impl.network.client;

import fuzs.iteminteractions.common.impl.ItemInteractions;
import fuzs.iteminteractions.common.impl.init.ModRegistry;
import fuzs.puzzleslib.common.api.network.v4.message.MessageListener;
import fuzs.puzzleslib.common.api.network.v4.message.play.ServerboundPlayMessage;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;

public record ServerboundContainerClientInputMessage(boolean extractSingleItem) implements ServerboundPlayMessage {
    public static final StreamCodec<ByteBuf, ServerboundContainerClientInputMessage> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL,
            ServerboundContainerClientInputMessage::extractSingleItem,
            ServerboundContainerClientInputMessage::new);

    @Override
    public MessageListener<Context> getListener() {
        return new MessageListener<Context>() {
            @Override
            public void accept(Context context) {
                ServerPlayer player = context.player();
                AbstractContainerMenu containerMenu = player.containerMenu;
                if (!containerMenu.stillValid(player)) {
                    ItemInteractions.LOGGER.debug("Player {} interacted with invalid menu {}", player, containerMenu);
                    return;
                }

                ModRegistry.MOVE_SINGLE_ITEM_ATTACHMENT_TYPE.set(player,
                        ServerboundContainerClientInputMessage.this.extractSingleItem);
            }
        };
    }
}
