package fuzs.iteminteractions.api.v1.provider.impl;

import com.mojang.serialization.MapCodec;
import fuzs.iteminteractions.api.v1.provider.ItemContentsProvider;
import fuzs.iteminteractions.impl.init.ModRegistry;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;

/**
 * A bare-bones implementation of {@link ItemContentsProvider}.
 */
public class EmptyProvider implements ItemContentsProvider {
    public static final ItemContentsProvider INSTANCE = new EmptyProvider();
    public static final MapCodec<ItemContentsProvider> CODEC = MapCodec.unit(INSTANCE);

    @Override
    public boolean allowsPlayerInteractions(ItemStack containerStack, Player player) {
        return false;
    }

    @Override
    public boolean hasContents(ItemStack containerStack) {
        return false;
    }

    @Override
    public boolean isItemAllowedInContainer(ItemStack containerStack, ItemStack stackToAdd) {
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
    public Type getType() {
        return ModRegistry.EMPTY.value();
    }
}
