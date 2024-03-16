package fuzs.iteminteractions.api.v1;

import fuzs.iteminteractions.api.v1.provider.ItemContainerProvider;
import fuzs.iteminteractions.impl.world.inventory.SimpleSlotContainer;
import fuzs.iteminteractions.impl.world.item.container.ContainerItemHelperImpl;
import fuzs.iteminteractions.api.v1.provider.ItemContainerBehavior;
import fuzs.iteminteractions.impl.world.item.container.ItemInteractionHelper;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.function.IntFunction;

public interface ContainerItemHelper {
    ContainerItemHelper INSTANCE = new ContainerItemHelperImpl();

    ItemContainerBehavior getItemContainerBehavior(ItemStack itemStack);

    ItemContainerBehavior getItemContainerBehavior(Item item);

    default SimpleContainer loadItemContainer(ItemStack stack, ItemContainerProvider provider, int inventorySize, boolean allowSaving) {
        return this.loadItemContainer(stack, provider, inventorySize, allowSaving, ItemInteractionHelper.TAG_ITEMS);
    }

    default SimpleContainer loadItemContainer(ItemStack stack, ItemContainerProvider provider, int inventorySize, boolean allowSaving, String nbtKey) {
        return this.loadItemContainer(stack, provider, items -> new SimpleSlotContainer(inventorySize), allowSaving, nbtKey);
    }

    SimpleContainer loadItemContainer(ItemStack stack, ItemContainerProvider provider, IntFunction<SimpleContainer> containerFactory, boolean allowSaving, String nbtKey);

    float[] getBackgroundColor(@Nullable DyeColor backgroundColor);
}
