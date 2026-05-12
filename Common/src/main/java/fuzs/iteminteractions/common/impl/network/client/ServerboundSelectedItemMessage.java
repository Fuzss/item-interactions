package fuzs.iteminteractions.common.impl.network.client;

import fuzs.iteminteractions.common.api.v1.world.item.storage.ItemStorageHolder;
import fuzs.iteminteractions.common.impl.world.inventory.ContainerSlotHelper;
import fuzs.puzzleslib.common.api.network.v4.message.MessageListener;
import fuzs.puzzleslib.common.api.network.v4.message.play.ServerboundPlayMessage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

/**
 * @see net.minecraft.network.protocol.game.ServerboundSelectBundleItemPacket
 */
public record ServerboundSelectedItemMessage(int slotId, int selectedItemIndex) implements ServerboundPlayMessage {
    public static final StreamCodec<FriendlyByteBuf, ServerboundSelectedItemMessage> STREAM_CODEC = StreamCodec.ofMember(
            ServerboundSelectedItemMessage::write,
            ServerboundSelectedItemMessage::new);

    private ServerboundSelectedItemMessage(FriendlyByteBuf input) {
        this(input.readVarInt(), input.readVarInt());
        if (this.selectedItemIndex < 0 && this.selectedItemIndex != -1) {
            throw new IllegalArgumentException("Invalid selectedItemIndex: " + this.selectedItemIndex);
        }
    }

    private void write(FriendlyByteBuf output) {
        output.writeVarInt(this.slotId);
        output.writeVarInt(this.selectedItemIndex);
    }

    @Override
    public MessageListener<Context> getListener() {
        return new MessageListener<Context>() {
            @Override
            public void accept(Context context) {
                int slotIndex = ServerboundSelectedItemMessage.this.slotId;
                AbstractContainerMenu menu = context.player().containerMenu;
                if (slotIndex >= 0 && slotIndex < menu.slots.size()) {
                    ItemStack itemStack = menu.slots.get(slotIndex).getItem();
                    int previousSelectedItem = ContainerSlotHelper.getSelectedItem(itemStack);
                    ContainerSlotHelper.setSelectedItem(itemStack,
                            ServerboundSelectedItemMessage.this.selectedItemIndex);
                    ItemStorageHolder holder = ItemStorageHolder.ofItem(itemStack);
                    holder.storage()
                            .onToggleSelectedItem(itemStack,
                                    previousSelectedItem,
                                    ServerboundSelectedItemMessage.this.selectedItemIndex);
                }
            }
        };
    }
}
