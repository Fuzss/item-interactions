package fuzs.iteminteractions.impl.network.client;

import fuzs.iteminteractions.impl.ItemInteractions;
import fuzs.iteminteractions.impl.world.inventory.ContainerSlotHelper;
import fuzs.puzzleslib.api.network.v4.message.MessageListener;
import fuzs.puzzleslib.api.network.v4.message.play.ServerboundPlayMessage;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;

public record ServerboundContainerClientInputMessage(int currentSlot,
                                                     boolean extractSingleItem) implements ServerboundPlayMessage {
    public static final StreamCodec<ByteBuf, ServerboundContainerClientInputMessage> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.SHORT.map(Short::intValue, Integer::shortValue),
            ServerboundContainerClientInputMessage::currentSlot,
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
                if (ServerboundContainerClientInputMessage.this.currentSlot >= -1) {
                    ContainerSlotHelper.setCurrentContainerSlot(player,
                            ServerboundContainerClientInputMessage.this.currentSlot);
                } else {
                    ItemInteractions.LOGGER.warn("{} tried to set an invalid current container item slot", player);
                }
                ContainerSlotHelper.extractSingleItem(player,
                        ServerboundContainerClientInputMessage.this.extractSingleItem);
            }
        };
    }
}
