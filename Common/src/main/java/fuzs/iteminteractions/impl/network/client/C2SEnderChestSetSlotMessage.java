package fuzs.iteminteractions.impl.network.client;

import fuzs.iteminteractions.impl.init.ModRegistry;
import fuzs.puzzleslib.api.network.v2.WritableMessage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class C2SEnderChestSetSlotMessage implements WritableMessage<C2SEnderChestSetSlotMessage> {
    private final int stateId;
    private final int slot;
    private final ItemStack itemStack;

    public C2SEnderChestSetSlotMessage(int stateId, int slot, ItemStack itemStack) {
        this.stateId = stateId;
        this.slot = slot;
        this.itemStack = itemStack.copy();
    }

    public C2SEnderChestSetSlotMessage(FriendlyByteBuf buf) {
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
    public MessageHandler<C2SEnderChestSetSlotMessage> makeHandler() {
        return new MessageHandler<>() {

            @Override
            public void handle(C2SEnderChestSetSlotMessage message, Player player, Object gameInstance) {
                if (((ServerPlayer) player).gameMode.isCreative()) {
                    ModRegistry.ENDER_CHEST_MENU_CAPABILITY.get(player).getEnderChestMenu().setItem(message.slot, message.stateId, message.itemStack);
                }
            }
        };
    }
}
