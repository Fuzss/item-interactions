package fuzs.iteminteractions.api.v1.provider;

import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import fuzs.iteminteractions.api.v1.ContainerItemHelper;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public abstract class AbstractItemContainerProvider implements TooltipItemContainerProvider {
    @Nullable
    private final DyeColor dyeColor;
    private final float[] backgroundColor;
    private HolderSet<Item> disallowedItems = HolderSet.empty();

    public AbstractItemContainerProvider(@Nullable DyeColor dyeColor) {
        this.dyeColor = dyeColor;
        this.backgroundColor = ContainerItemHelper.getBackgroundColor(dyeColor);
    }

    protected float[] getBackgroundColor() {
        return this.backgroundColor;
    }

    public AbstractItemContainerProvider disallowedItems(HolderSet<Item> disallowedItems) {
        this.disallowedItems = disallowedItems;
        return this;
    }

    @Override
    public boolean isItemAllowedInContainer(ItemStack containerStack, ItemStack stackToAdd) {
        return !stackToAdd.is(this.disallowedItems);
    }

    @Override
    public void toJson(JsonObject jsonObject) {
        if (this.dyeColor != null) {
            jsonObject.addProperty("background_color", this.dyeColor.getName());
        }
        jsonObject.add("disallowed_items", RegistryCodecs.homogeneousList(Registries.ITEM)
                .encodeStart(JsonOps.INSTANCE, this.disallowedItems)
                .getOrThrow());
    }
}
