package fuzs.iteminteractions.common.impl.network;

import fuzs.iteminteractions.common.api.v1.provider.ItemContentsProvider;
import fuzs.iteminteractions.common.impl.world.item.container.ItemContentsProviders;
import fuzs.puzzleslib.common.api.network.v4.message.MessageListener;
import fuzs.puzzleslib.common.api.network.v4.message.play.ClientboundPlayMessage;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.Item;

import java.util.HashMap;
import java.util.Map;

public record ClientboundSyncItemContentsProviders(Map<Item, ItemContentsProvider> providers) implements ClientboundPlayMessage {
    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundSyncItemContentsProviders> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.map(HashMap::new, ByteBufCodecs.registry(Registries.ITEM), ItemContentsProvider.STREAM_CODEC),
            ClientboundSyncItemContentsProviders::providers,
            ClientboundSyncItemContentsProviders::new);

    @Override
    public MessageListener<Context> getListener() {
        return new MessageListener<Context>() {
            @Override
            public void accept(Context context) {
                ItemContentsProviders.setItemContainerProviders(ClientboundSyncItemContentsProviders.this.providers);
            }
        };
    }
}
