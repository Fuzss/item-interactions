package fuzs.iteminteractions.impl.world.item.container;

import com.mojang.serialization.Codec;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

import java.util.HashMap;
import java.util.Map;

/**
 * Fixes a bug in NeoForge, where the {@link com.mojang.serialization.DynamicOps} instance passed is an instance of
 * {@code ConditionalOps}, but vanilla in some cases checks for {@link net.minecraft.resources.RegistryOps} specifically
 * for certain codec operations, such as in {@link net.minecraft.resources.HolderSetCodec}.
 */
public abstract class UnconditionalSimpleJsonResourceReloadListener<T> extends SimpleJsonResourceReloadListener<T> {

    protected UnconditionalSimpleJsonResourceReloadListener(HolderLookup.Provider registries, Codec<T> codec, String directory) {
        super(registries, codec, directory);
    }

    protected UnconditionalSimpleJsonResourceReloadListener(Codec<T> codec, String directory) {
        super(codec, directory);
    }

    @Override
    protected Map<ResourceLocation, T> prepare(ResourceManager resourceManager, ProfilerFiller profiler) {
        Map<ResourceLocation, T> map = new HashMap<>();
        scanDirectory(resourceManager, this.directory, this.ops, this.codec, map);
        return map;
    }
}
