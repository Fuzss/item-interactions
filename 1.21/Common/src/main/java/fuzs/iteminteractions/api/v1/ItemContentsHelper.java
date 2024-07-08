package fuzs.iteminteractions.api.v1;

import fuzs.iteminteractions.api.v1.provider.ItemContentsBehavior;
import fuzs.iteminteractions.impl.world.item.container.ItemContentsProviders;
import net.minecraft.util.FastColor;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public final class ItemContentsHelper {

    private ItemContentsHelper() {
        // NO-OP
    }

    public static ItemContentsBehavior getItemContentsBehavior(ItemStack itemStack) {
        return ItemContentsProviders.get(itemStack);
    }

    public static float[] getBackgroundColor(@Nullable DyeBackedColor color) {
        if (color == null) {
            return new float[]{1.0F, 1.0F, 1.0F};
        } else {
            DyeColor dyeColor = DyeColor.byName(color.serialize(), null);
            int value;
            if (dyeColor != null) {
                value = Sheep.createSheepColor(dyeColor);
            } else {
                value = color.getValue();
            }
            return new float[]{FastColor.ARGB32.red(value) / 255.0F, FastColor.ARGB32.green(value) / 255.0F, FastColor.ARGB32.blue(value) / 255.0F};
        }
    }
}
