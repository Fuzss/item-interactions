package fuzs.iteminteractions.impl.network;

import fuzs.iteminteractions.impl.handler.EnderChestSyncHandler;
import fuzs.puzzleslib.api.network.v2.WritableMessage;
import fuzs.puzzleslib.api.network.v3.codec.ExtraStreamCodecs;
import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class S2CEnderChestContentMessage implements WritableMessage<S2CEnderChestContentMessage> {
    private final NonNullList<ItemStack> items;

    public S2CEnderChestContentMessage(FriendlyByteBuf buf) {
        this.items = buf.readCollection(NonNullList::createWithCapacity, ExtraStreamCodecs::readItem);
    }

    public S2CEnderChestContentMessage(NonNullList<ItemStack> items) {
        this.items = NonNullList.withSize(items.size(), ItemStack.EMPTY);
        for (int k = 0; k < items.size(); ++k) {
            this.items.set(k, items.get(k).copy());
        }
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeCollection(this.items, ExtraStreamCodecs::writeItem);
    }

    @Override
    public MessageHandler<S2CEnderChestContentMessage> makeHandler() {
        return new MessageHandler<>() {

            @Override
            public void handle(S2CEnderChestContentMessage message, Player player, Object gameInstance) {
                EnderChestSyncHandler.setEnderChestContent(player, message.items);
            }
        };
    }
}
