package fuzs.iteminteractions.common.api.v1.world.item.storage;

import com.mojang.serialization.MapCodec;
import fuzs.iteminteractions.common.impl.init.ModRegistry;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;

/**
 * A bare-bones implementation of {@link ItemStorage}.
 */
public class VoidStorage implements ItemStorage {
    public static final ItemStorage INSTANCE = new VoidStorage();
    public static final MapCodec<ItemStorage> CODEC = MapCodec.unit(INSTANCE);

    @Override
    public boolean canPlayerInteractWith(ItemStack itemStack, Player player) {
        return false;
    }

    @Override
    public boolean hasContents(ItemStack itemStack) {
        return false;
    }

    @Override
    public boolean isItemAllowedInContainer(ItemStack stackToAdd) {
        return false;
    }

    @Override
    public boolean canAddItem(ItemStack itemStack, ItemStack stackToAdd, Player player) {
        return false;
    }

    @Override
    public SimpleContainer getItemContainer(ItemStack itemStack, Player player, boolean isMutable) {
        return new SimpleContainer();
    }

    @Override
    public int getAcceptableItemCount(ItemStack itemStack, ItemStack stackToAdd, Player player) {
        return 0;
    }

    @Override
    public boolean canProvideTooltipImage(ItemStack itemStack, Player player) {
        return false;
    }

    @Override
    public Optional<TooltipComponent> getTooltipImage(ItemStack itemStack, Player player) {
        return Optional.empty();
    }

    @Override
    public ItemStorageType<?> getType() {
        return ModRegistry.EMPTY_ITEM_CONTENTS_PROVIDER_TYPE.value();
    }
}
