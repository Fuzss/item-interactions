package fuzs.iteminteractions.impl.world.item.container;

import fuzs.iteminteractions.api.v1.ContainerItemHelper;
import fuzs.iteminteractions.api.v1.provider.ItemContainerBehavior;
import fuzs.iteminteractions.api.v1.provider.ItemContainerProvider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.function.IntFunction;

public final class ContainerItemHelperImpl implements ContainerItemHelper {

    @Override
    public ItemContainerBehavior getItemContainerBehavior(ItemStack itemStack) {
        return ItemContainerProviders.INSTANCE.get(itemStack);
    }

    @Override
    public ItemContainerBehavior getItemContainerBehavior(Item item) {
        return ItemContainerProviders.INSTANCE.get(item);
    }

    @Override
    public SimpleContainer loadItemContainer(ItemStack stack, ItemContainerProvider provider, IntFunction<SimpleContainer> containerFactory, boolean allowSaving, String nbtKey) {
        CompoundTag tag = provider.getItemContainerData(stack);
        ListTag items = null;
        if (tag != null && tag.contains(nbtKey)) {
            items = tag.getList(nbtKey, Tag.TAG_COMPOUND);
        }
        SimpleContainer simpleContainer = containerFactory.apply(items != null ? items.size() : 0);
        if (items != null) {
            simpleContainer.fromTag(items);
        }
        if (allowSaving) {
            simpleContainer.addListener(container -> {
                ListTag itemsTag = ((SimpleContainer) container).createTag();
                provider.setItemContainerData(stack, itemsTag, nbtKey);
            });
        }
        return simpleContainer;
    }

    @Override
    public float[] getBackgroundColor(@Nullable DyeColor backgroundColor) {
        if (backgroundColor == null) {
            return new float[]{1.0F, 1.0F, 1.0F};
        } else if (backgroundColor == DyeColor.WHITE) {
            return new float[]{0.9019608F, 0.9019608F, 0.9019608F};
        } else {
            return backgroundColor.getTextureDiffuseColors();
        }
    }
}
