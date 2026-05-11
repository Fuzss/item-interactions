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
public class DefaultItemStorage implements ItemStorage {
    public static final ItemStorage INSTANCE = new DefaultItemStorage();
    public static final MapCodec<ItemStorage> CODEC = MapCodec.unit(INSTANCE);

    @Override
    public boolean allowsPlayerInteractions(ItemStack containerStack, Player player) {
        return false;
    }

    @Override
    public boolean hasContents(ItemStack containerStack) {
        return false;
    }

    @Override
    public boolean isItemAllowedInContainer(ItemStack stackToAdd) {
        return false;
    }

    @Override
    public boolean canAddItem(ItemStack containerStack, ItemStack stackToAdd, Player player) {
        return false;
    }

    @Override
    public SimpleContainer getItemContainer(ItemStack containerStack, Player player, boolean allowSaving) {
        // should never be able to reach here
        throw new UnsupportedOperationException();
    }

    @Override
    public int getAcceptableItemCount(ItemStack containerStack, ItemStack stackToAdd, Player player) {
        return 0;
    }

    @Override
    public boolean canProvideTooltipImage(ItemStack containerStack, Player player) {
        return false;
    }

    @Override
    public Optional<TooltipComponent> getTooltipImage(ItemStack containerStack, Player player) {
        return Optional.empty();
    }

    @Override
    public Type<?> getType() {
        return ModRegistry.EMPTY_ITEM_CONTENTS_PROVIDER_TYPE.value();
    }
}
