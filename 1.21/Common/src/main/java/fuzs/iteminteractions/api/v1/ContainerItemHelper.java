package fuzs.iteminteractions.api.v1;

import fuzs.iteminteractions.api.v1.provider.ItemContainerBehavior;
import fuzs.iteminteractions.impl.world.item.container.ItemContainerProviders;
import net.minecraft.util.FastColor;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public final class ContainerItemHelper {

    private ContainerItemHelper() {
        // NO-OP
    }

    public static ItemContainerBehavior getItemContainerBehavior(ItemStack itemStack) {
        return ItemContainerProviders.INSTANCE.get(itemStack);
    }

    public static ItemContainerBehavior getItemContainerBehavior(Item item) {
        return ItemContainerProviders.INSTANCE.get(item);
    }

    public static float[] getBackgroundColor(@Nullable DyeColor dyeColor) {
        if (dyeColor == null) {
            return new float[]{1.0F, 1.0F, 1.0F};
        } else {
            int color = Sheep.createSheepColor(dyeColor);
            return new float[]{FastColor.ARGB32.red(color) / 255.0F, FastColor.ARGB32.green(color) / 255.0F, FastColor.ARGB32.blue(color) / 255.0F};
        }
    }
}
