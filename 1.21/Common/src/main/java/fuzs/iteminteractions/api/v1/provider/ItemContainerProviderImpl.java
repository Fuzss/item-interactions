package fuzs.iteminteractions.api.v1.provider;

import com.google.gson.JsonObject;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

/**
 * A bare-bones implementation of {@link ItemContainerProvider}.
 */
public class ItemContainerProviderImpl implements ItemContainerProvider {

    @Override
    public boolean allowsPlayerInteractions(ItemStack containerStack, Player player) {
        return false;
    }

    @Override
    public boolean hasItemContainerData(ItemStack containerStack) {
        return false;
    }

    @Override
    public @Nullable CompoundTag getItemContainerData(ItemStack containerStack) {
        return null;
    }

    @Override
    public void setItemContainerData(ItemStack containerStack, ListTag itemsTag, String nbtKey) {
        // NO-OP
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
    public SimpleContainer getItemContainer(ItemStack containerStack, @Nullable Player player, boolean allowSaving) {
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
    public void toJson(JsonObject jsonObject) {
        // NO-OP
    }
}
