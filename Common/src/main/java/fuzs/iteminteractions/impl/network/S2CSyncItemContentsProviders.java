package fuzs.iteminteractions.impl.network;

import fuzs.iteminteractions.api.v1.provider.ItemContentsProvider;
import fuzs.iteminteractions.impl.world.item.container.ItemContentsProviders;
import fuzs.puzzleslib.api.network.v2.WritableMessage;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.RegistryOps;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;

import java.util.Map;

public class S2CSyncItemContentsProviders implements WritableMessage<S2CSyncItemContentsProviders> {
    private final Map<Item, ItemContentsProvider> providers;

    public S2CSyncItemContentsProviders(Map<Item, ItemContentsProvider> providers) {
        this.providers = providers;
    }

    public S2CSyncItemContentsProviders(FriendlyByteBuf buf) {
        this.providers = buf.readMap((FriendlyByteBuf friend) -> {
            return ByteBufCodecs.registry(Registries.ITEM).decode((RegistryFriendlyByteBuf) friend);
        }, friendlyByteBuf -> {
            ItemContentsProvider.Type type = ByteBufCodecs.registry(ItemContentsProvider.REGISTRY_KEY)
                    .decode((RegistryFriendlyByteBuf) friendlyByteBuf);
            RegistryOps<Tag> registryOps = ((RegistryFriendlyByteBuf) friendlyByteBuf).registryAccess()
                    .createSerializationContext(NbtOps.INSTANCE);
            return friendlyByteBuf.readWithCodecTrusted(registryOps, type.codec());
        });
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeMap(this.providers, (FriendlyByteBuf friendlyByteBuf, Item item) -> {
            ByteBufCodecs.registry(Registries.ITEM).encode((RegistryFriendlyByteBuf) friendlyByteBuf, item);
        }, (FriendlyByteBuf friendlyByteBuf, ItemContentsProvider provider) -> {
            RegistryOps<Tag> registryOps = ((RegistryFriendlyByteBuf) friendlyByteBuf).registryAccess()
                    .createSerializationContext(NbtOps.INSTANCE);
            ByteBufCodecs.registry(ItemContentsProvider.REGISTRY_KEY)
                    .encode((RegistryFriendlyByteBuf) friendlyByteBuf, provider.getType());
            buf.writeWithCodec(registryOps, provider.getType().codec(), provider);
        });
    }

    @Override
    public MessageHandler<S2CSyncItemContentsProviders> makeHandler() {
        return new MessageHandler<>() {

            @Override
            public void handle(S2CSyncItemContentsProviders message, Player player, Object gameInstance) {
                ItemContentsProviders.setItemContainerProviders(message.providers);
            }
        };
    }
}
