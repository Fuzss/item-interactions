package fuzs.iteminteractions.common.api.v1.world.item.storage;

import com.mojang.serialization.MapCodec;
import fuzs.iteminteractions.common.impl.init.ModRegistry;
import fuzs.iteminteractions.common.impl.world.item.component.SelectedItem;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import org.joml.Vector2ic;

import java.util.Optional;

/**
 * A bare-bones implementation of {@link ItemStorage}.
 */
public class VoidStorage implements ItemStorage {
    public static final ItemStorage INSTANCE = new VoidStorage();
    public static final MapCodec<ItemStorage> CODEC = MapCodec.unit(INSTANCE);

    protected VoidStorage() {
        // NO-OP
    }

    @Override
    public boolean canPlayerInteractWith(ItemStack itemStack, Player player) {
        return false;
    }

    @Override
    public boolean hasContents(ItemStack itemStack) {
        return false;
    }

    @Override
    public boolean overrideStackedOnOther(ItemStorageHolder holder, ItemStack itemStack, Slot slot, ClickAction clickAction, Player player) {
        return false;
    }

    @Override
    public boolean overrideOtherStackedOnMe(ItemStorageHolder holder, ItemStack itemStack, ItemStack itemHeldByCursor, Slot slot, ClickAction clickAction, Player player, SlotAccess slotHeldByCursor) {
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
    public Container getItemContainer(ItemStack itemStack, Player player) {
        return new SimpleContainer();
    }

    @Override
    public int getAcceptableItemCount(ItemStack itemStack, ItemStack stackToAdd, Player player) {
        return 0;
    }

    @Override
    public Optional<Optional<TooltipComponent>> getTooltipImage(ItemStack itemStack, Player player) {
        return Optional.empty();
    }

    @Override
    public int getSelectedItem(ItemStack itemStack) {
        return SelectedItem.DEFAULT_SELECTED_ITEM;
    }

    @Override
    public int scrollSelectedItem(ItemStack itemStack, Container container, Vector2ic scrollXY) {
        return this.getSelectedItem(itemStack);
    }

    @Override
    public void toggleSelectedItem(ItemStack itemStack, int selectedItem) {
        // NO-OP
    }

    @Override
    public ItemStorageType<?> getType() {
        return ModRegistry.EMPTY_ITEM_CONTENTS_PROVIDER_TYPE.value();
    }
}
