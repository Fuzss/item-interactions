package fuzs.iteminteractions.impl.network;

import fuzs.iteminteractions.impl.init.ModRegistry;
import fuzs.puzzleslib.api.network.v2.WritableMessage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class S2CEnderChestSetSlotMessage implements WritableMessage<S2CEnderChestSetSlotMessage> {
    private final int stateId;
    private final int slot;
    private final ItemStack itemStack;

    public S2CEnderChestSetSlotMessage(int stateId, int slot, ItemStack itemStack) {
        this.stateId = stateId;
        this.slot = slot;
        this.itemStack = itemStack.copy();
    }

    public S2CEnderChestSetSlotMessage(FriendlyByteBuf buf) {
        this.stateId = buf.readVarInt();
        this.slot = buf.readShort();
        this.itemStack = buf.readItem();
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeVarInt(this.stateId);
        buf.writeShort(this.slot);
        buf.writeItem(this.itemStack);
    }

    @Override
    public MessageHandler<S2CEnderChestSetSlotMessage> makeHandler() {
        return new MessageHandler<>() {

            @Override
            public void handle(S2CEnderChestSetSlotMessage message, Player player, Object gameInstance) {
                ModRegistry.ENDER_CHEST_MENU_CAPABILITY.get(player).getEnderChestMenu().setItem(message.slot, message.stateId, message.itemStack);
            }
        };
    }
}
