package fuzs.iteminteractions.impl.network.client;

import fuzs.iteminteractions.impl.handler.EnderChestSyncHandler;
import fuzs.puzzleslib.api.network.v2.WritableMessage;
import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class C2SEnderChestContentMessage implements WritableMessage<C2SEnderChestContentMessage> {
    private final NonNullList<ItemStack> items;

    public C2SEnderChestContentMessage(FriendlyByteBuf buf) {
        this.items = buf.readCollection(NonNullList::createWithCapacity, FriendlyByteBuf::readItem);
    }

    public C2SEnderChestContentMessage(NonNullList<ItemStack> items) {
        this.items = NonNullList.withSize(items.size(), ItemStack.EMPTY);
        for (int k = 0; k < items.size(); ++k) {
            this.items.set(k, items.get(k).copy());
        }
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeCollection(this.items, FriendlyByteBuf::writeItem);
    }

    @Override
    public MessageHandler<C2SEnderChestContentMessage> makeHandler() {
        return new MessageHandler<>() {

            @Override
            public void handle(C2SEnderChestContentMessage message, Player player, Object gameInstance) {
                if (((ServerPlayer) player).gameMode.isCreative()) {
                    EnderChestSyncHandler.setEnderChestContent(player, message.items);
                }
            }
        };
    }
}
