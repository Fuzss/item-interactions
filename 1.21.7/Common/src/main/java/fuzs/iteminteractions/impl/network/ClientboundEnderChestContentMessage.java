package fuzs.iteminteractions.impl.network;

import fuzs.iteminteractions.impl.handler.EnderChestSyncHandler;
import fuzs.puzzleslib.api.network.v4.message.MessageListener;
import fuzs.puzzleslib.api.network.v4.message.play.ClientboundPlayMessage;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public record ClientboundEnderChestContentMessage(List<ItemStack> items) implements ClientboundPlayMessage {
    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundEnderChestContentMessage> STREAM_CODEC = StreamCodec.composite(
            ItemStack.OPTIONAL_LIST_STREAM_CODEC,
            ClientboundEnderChestContentMessage::items,
            ClientboundEnderChestContentMessage::new);

    @Override
    public MessageListener<Context> getListener() {
        return new MessageListener<Context>() {
            @Override
            public void accept(Context context) {
                EnderChestSyncHandler.setEnderChestContent(context.player(),
                        ClientboundEnderChestContentMessage.this.items);
            }
        };
    }
}
