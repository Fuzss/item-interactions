package fuzs.iteminteractions.api.v1;

import fuzs.iteminteractions.api.v1.provider.ItemContentsBehavior;
import fuzs.iteminteractions.impl.world.item.container.ItemContentsProviders;
import net.minecraft.util.ARGB;
import net.minecraft.world.entity.animal.sheep.Sheep;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

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
            Vector3f vector3f = ARGB.vector3fFromRGB24(value);
            return new float[]{vector3f.x(), vector3f.y(), vector3f.z()};
        }
    }
}
