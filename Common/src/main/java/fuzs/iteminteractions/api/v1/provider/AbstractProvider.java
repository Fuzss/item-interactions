package fuzs.iteminteractions.api.v1.provider;

import com.mojang.serialization.codecs.RecordCodecBuilder;
import fuzs.iteminteractions.api.v1.DyeBackedColor;
import fuzs.iteminteractions.api.v1.ItemContentsHelper;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public abstract class AbstractProvider implements TooltipProvider {
    @Nullable
    final DyeBackedColor dyeColor;
    private final float[] backgroundColor;
    HolderSet<Item> disallowedItems = HolderSet.empty();

    protected AbstractProvider(@Nullable DyeBackedColor dyeColor) {
        this.dyeColor = dyeColor;
        this.backgroundColor = ItemContentsHelper.getBackgroundColor(dyeColor);
    }

    protected static <T extends AbstractProvider> RecordCodecBuilder<T, Optional<DyeBackedColor>> backgroundColorCodec() {
        return DyeBackedColor.CODEC.optionalFieldOf("background_color")
                .forGetter((T provider) -> Optional.ofNullable(provider.dyeColor));
    }

    protected static <T extends AbstractProvider> RecordCodecBuilder<T, HolderSet<Item>> disallowedItemsCodec() {
        return RegistryCodecs.homogeneousList(Registries.ITEM).fieldOf("disallowed_item_contents")
                .orElse(HolderSet.empty())
                .forGetter(provider -> provider.disallowedItems);
    }

    protected float[] getBackgroundColor() {
        return this.backgroundColor;
    }

    public AbstractProvider disallowedItems(HolderSet<Item> disallowedItems) {
        this.disallowedItems = disallowedItems;
        return this;
    }

    @Override
    public boolean isItemAllowedInContainer(ItemStack stackToAdd) {
        return !stackToAdd.is(this.disallowedItems);
    }
}
