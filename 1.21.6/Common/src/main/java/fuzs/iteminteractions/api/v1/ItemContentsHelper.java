package fuzs.iteminteractions.api.v1;

import fuzs.iteminteractions.api.v1.provider.ItemContentsBehavior;
import fuzs.iteminteractions.impl.world.item.container.ItemContentsProviders;
import net.minecraft.world.item.ItemStack;

public final class ItemContentsHelper {

    private ItemContentsHelper() {
        // NO-OP
    }

    public static ItemContentsBehavior getItemContentsBehavior(ItemStack itemStack) {
        return ItemContentsProviders.get(itemStack);
    }
}
