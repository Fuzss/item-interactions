package fuzs.iteminteractions.impl.network;

import fuzs.puzzleslib.api.network.v2.WritableMessage;
import fuzs.puzzleslib.api.network.v3.codec.ExtraStreamCodecs;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class S2CEnderChestSlotMessage implements WritableMessage<S2CEnderChestSlotMessage> {
    private final int slot;
    private final ItemStack itemStack;

    public S2CEnderChestSlotMessage(int slot, ItemStack itemStack) {
        this.slot = slot;
        this.itemStack = itemStack.copy();
    }

    public S2CEnderChestSlotMessage(FriendlyByteBuf buf) {
        this.slot = buf.readShort();
        this.itemStack = ExtraStreamCodecs.readItem(buf);
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeShort(this.slot);
        ExtraStreamCodecs.writeItem(buf, this.itemStack);
    }

    @Override
    public MessageHandler<S2CEnderChestSlotMessage> makeHandler() {
        return new MessageHandler<>() {

            @Override
            public void handle(S2CEnderChestSlotMessage message, Player player, Object gameInstance) {
                if (message.slot < player.getEnderChestInventory().getContainerSize()) {
                    player.getEnderChestInventory().setItem(message.slot, message.itemStack);
                }
            }
        };
    }
}
