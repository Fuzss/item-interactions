package fuzs.iteminteractions.api.v1;

import fuzs.iteminteractions.api.v1.provider.ItemContentsBehavior;
import fuzs.iteminteractions.impl.world.item.container.ItemContentsProviders;
import net.minecraft.client.color.ColorLerper;
import net.minecraft.util.ARGB;
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

    public static int getBackgroundColor(@Nullable DyeBackedColor color) {
        if (color == null) {
            return -1;
        } else {
            DyeColor dyeColor = DyeColor.byName(color.serialize(), null);
            int colorValue;
            if (dyeColor != null) {
                colorValue = ColorLerper.Type.SHEEP.getColor(dyeColor);
            } else {
                colorValue = color.getValue();
            }
            return ARGB.opaque(colorValue);
        }
    }
}
