package fuzs.iteminteractions.common.api.v1.world.item.storage;

import com.mojang.serialization.MapCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/**
 * A type for identifying and serializing item container provider implementations.
 *
 * @param codec the item container provider codec
 */
public record ItemStorageType<T extends ItemStorage>(MapCodec<T> codec,
                                                     StreamCodec<? super RegistryFriendlyByteBuf, T> streamCodec) {

    public ItemStorageType(MapCodec<T> codec) {
        this(codec, ByteBufCodecs.fromCodecWithRegistries(codec.codec()));
    }
}
