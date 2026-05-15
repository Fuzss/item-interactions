package fuzs.iteminteractions.common.impl.network.client;

import fuzs.iteminteractions.common.impl.init.ModRegistry;
import fuzs.puzzleslib.common.api.network.v4.message.MessageListener;
import fuzs.puzzleslib.common.api.network.v4.message.play.ServerboundPlayMessage;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.Unit;

public record ServerboundContainerClientInputMessage(boolean singleItemOnly) implements ServerboundPlayMessage {
    public static final StreamCodec<ByteBuf, ServerboundContainerClientInputMessage> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL,
            ServerboundContainerClientInputMessage::singleItemOnly,
            ServerboundContainerClientInputMessage::new);

    @Override
    public MessageListener<Context> getListener() {
        return new MessageListener<Context>() {
            @Override
            public void accept(Context context) {
                boolean singleItemOnly = ServerboundContainerClientInputMessage.this.singleItemOnly();
                ModRegistry.MOVE_SINGLE_ITEM_ATTACHMENT_TYPE.set(context.player(),
                        singleItemOnly ? Unit.INSTANCE : null);
            }
        };
    }
}
