package fuzs.iteminteractions.impl.network;

import fuzs.puzzleslib.api.network.v4.message.MessageListener;
import fuzs.puzzleslib.api.network.v4.message.play.ClientboundPlayMessage;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;

public record ClientboundEnderChestSlotMessage(int slot, ItemStack item) implements ClientboundPlayMessage {
    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundEnderChestSlotMessage> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.SHORT.map(Short::intValue, Integer::shortValue),
            ClientboundEnderChestSlotMessage::slot,
            ItemStack.OPTIONAL_STREAM_CODEC,
            ClientboundEnderChestSlotMessage::item,
            ClientboundEnderChestSlotMessage::new);

    @Override
    public MessageListener<Context> getListener() {
        return new MessageListener<Context>() {
            @Override
            public void accept(Context context) {
                if (ClientboundEnderChestSlotMessage.this.slot <
                        context.player().getEnderChestInventory().getContainerSize()) {
                    context.player()
                            .getEnderChestInventory()
                            .setItem(ClientboundEnderChestSlotMessage.this.slot,
                                    ClientboundEnderChestSlotMessage.this.item);
                }
            }
        };
    }
}
