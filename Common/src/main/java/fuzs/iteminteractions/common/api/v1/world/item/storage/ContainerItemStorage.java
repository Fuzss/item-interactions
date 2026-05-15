package fuzs.iteminteractions.common.api.v1.world.item.storage;

import fuzs.iteminteractions.common.impl.init.ModRegistry;
import fuzs.iteminteractions.common.impl.world.inventory.ItemSlot;
import fuzs.iteminteractions.common.impl.world.item.component.SelectedItem;
import fuzs.iteminteractions.common.impl.world.item.container.ItemInteractionHelper;
import fuzs.iteminteractions.common.impl.world.item.container.ItemStackingContext;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.Container;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.joml.Vector2ic;

public interface ContainerItemStorage extends ItemStorage {

    int getGridWidth(int itemCount);

    int getGridHeight(int itemCount);

    default boolean extractSingleItemOnly(Player player) {
        return ModRegistry.MOVE_SINGLE_ITEM_ATTACHMENT_TYPE.has(player);
    }

    @Override
    default int getSelectedItem(ItemStack itemStack) {
        return itemStack.getOrDefault(ModRegistry.SELECTED_ITEM_DATA_COMPONENT_TYPE.value(), SelectedItem.DEFAULT)
                .selectedItem();
    }

    default void setSelectedItem(ItemStack itemStack, int selectedItem) {
        itemStack.set(ModRegistry.SELECTED_ITEM_DATA_COMPONENT_TYPE.value(),
                selectedItem == SelectedItem.DEFAULT_SELECTED_ITEM ? SelectedItem.DEFAULT :
                        SelectedItem.of(selectedItem));
    }

    @Override
    default void toggleSelectedItem(ItemStack itemStack, int selectedItem) {
        this.setSelectedItem(itemStack, selectedItem);
    }

    @Override
    default int scrollSelectedItem(ItemStack itemStack, Container container, Vector2ic scrollXY) {
        int selectedItem = this.getSelectedItem(itemStack);
        int gridWidth = this.getGridWidth(container.getContainerSize());
        int gridHeight = this.getGridHeight(container.getContainerSize());
        int gridSize = gridWidth * gridHeight;
        for (int slotNum = 0; slotNum < gridSize; slotNum++) {
            int x = selectedItem % gridWidth;
            int y = selectedItem / gridWidth;
            if (scrollXY.x() == 0) {
                y += Mth.sign(scrollXY.y());
                if (y < 0) {
                    y = gridHeight - 1;
                    x--;
                } else if (y >= gridHeight) {
                    y = 0;
                    x++;
                }

                x = Mth.positiveModulo(x, gridWidth);
            } else {
                x += Mth.sign(scrollXY.x());
                if (x < 0) {
                    x = gridWidth - 1;
                    y--;
                } else if (x >= gridWidth) {
                    x = 0;
                    y++;
                }

                y = Mth.positiveModulo(y, gridHeight);
            }

            selectedItem = y * gridWidth + x;
            if (selectedItem < container.getContainerSize() && !container.getItem(selectedItem).isEmpty()) {
                return selectedItem;
            }
        }

        return SelectedItem.DEFAULT_SELECTED_ITEM;
    }

    /**
     * @see net.minecraft.world.item.BundleItem#overrideStackedOnOther(ItemStack, Slot, ClickAction, Player)
     */
    @Override
    default boolean overrideStackedOnOther(ItemStorageHolder holder, ItemStack itemStack, Slot slot, ClickAction clickAction, Player player) {
        if (false) {
            return ItemInteractionHelper.overrideStackedOnOther(itemStack,
                    () -> holder.getMutableContainer(itemStack, player),
                    slot,
                    clickAction,
                    player,
                    (ItemStack item) -> {
                        return holder.getAcceptableItemCount(itemStack, item, player);
                    },
                    Container::getMaxStackSize);
        }

        ItemStackingContext context = new ItemStackingContext(holder, this, player);
        ItemStack otherItem = slot.getItem();
        if (clickAction == ClickAction.PRIMARY && !otherItem.isEmpty()) {
            otherItem = slot.safeTake(otherItem.getCount(), otherItem.getCount(), player);
            int transferredCount = context.tryInsert(itemStack, otherItem);
            otherItem.shrink(transferredCount);
            if (!this.extractSingleItemOnly(player)) {
                if (transferredCount > 0) {
                    this.playInsertSound(player);
                } else {
                    this.playInsertFailSound(player);
                }
            }

            slot.safeInsert(otherItem);
            this.broadcastChangesOnContainerMenu(itemStack, player);
            return true;
        } else if (clickAction == ClickAction.SECONDARY && (otherItem.isEmpty()
                || this.extractSingleItemOnly(player))) {
            ItemSlot itemSlot = context.removeOne(itemStack, otherItem);
            if (!itemSlot.item().isEmpty()) {
                context.tryInsert(itemStack, slot.safeInsert(itemSlot.item()), itemSlot.slotNum());
                if (!this.extractSingleItemOnly(player)) {
                    this.playRemoveOneSound(player);
                }
            }

            this.broadcastChangesOnContainerMenu(itemStack, player);
            return true;
        } else {
            return false;
        }
    }

    /**
     * @see net.minecraft.world.item.BundleItem#overrideOtherStackedOnMe(ItemStack, ItemStack, Slot, ClickAction,
     *         Player, SlotAccess)
     */
    @Override
    default boolean overrideOtherStackedOnMe(ItemStorageHolder holder, ItemStack itemStack, ItemStack itemHeldByCursor, Slot slot, ClickAction clickAction, Player player, SlotAccess slotHeldByCursor) {
        if (false) {
            return ItemInteractionHelper.overrideOtherStackedOnMe(itemStack,
                    () -> holder.getMutableContainer(itemStack, player),
                    itemHeldByCursor,
                    slot,
                    clickAction,
                    player,
                    slotHeldByCursor,
                    (ItemStack item) -> {
                        return this.getAcceptableItemCount(itemStack, item, player);
                    },
                    Container::getMaxStackSize,
                    () -> holder.storage().toggleSelectedItem(itemStack, SelectedItem.DEFAULT_SELECTED_ITEM));
        }

        if (clickAction == ClickAction.PRIMARY && itemHeldByCursor.isEmpty()) {
            if (!this.extractSingleItemOnly(player)) {
                this.toggleSelectedItem(itemStack, SelectedItem.DEFAULT_SELECTED_ITEM);
                return false;
            } else {
                return true;
            }
        } else {
            ItemStackingContext context = new ItemStackingContext(holder, this, player);
            if (clickAction == ClickAction.PRIMARY && !itemHeldByCursor.isEmpty()) {
                if (slot.allowModification(player)) {
                    int transferredCount = context.tryInsert(itemStack, itemHeldByCursor);
                    itemHeldByCursor.shrink(transferredCount);
                    if (!this.extractSingleItemOnly(player)) {
                        if (transferredCount > 0) {
                            this.playInsertSound(player);
                        } else {
                            this.playInsertFailSound(player);
                        }
                    }
                }

                this.broadcastChangesOnContainerMenu(itemStack, player);
                return true;
            } else if (clickAction == ClickAction.SECONDARY && (itemHeldByCursor.isEmpty()
                    || this.extractSingleItemOnly(player))) {
                if (slot.allowModification(player)) {
                    ItemStack itemRemainder = context.removeOne(itemStack, itemHeldByCursor).item();
                    if (!itemRemainder.isEmpty()) {
                        // When extracting single items only, the item held by cursor may not be empty, so we cannot just replace it straight away.
                        if (itemHeldByCursor.isEmpty()) {
                            slotHeldByCursor.set(itemRemainder);
                        } else {
                            itemHeldByCursor.grow(itemRemainder.getCount());
                        }

                        if (!this.extractSingleItemOnly(player)) {
                            this.playRemoveOneSound(player);
                        }
                    }
                }

                this.broadcastChangesOnContainerMenu(itemStack, player);
                return true;
            } else {
                this.toggleSelectedItem(itemStack, SelectedItem.DEFAULT_SELECTED_ITEM);
                return false;
            }
        }
    }

    /**
     * Used to synchronize item storage changes.
     *
     * @param itemStack the item stack providing the storage
     * @param player    the player performing the interaction
     * @see net.minecraft.world.item.BundleItem#broadcastChangesOnContainerMenu(Player)
     */
    default void broadcastChangesOnContainerMenu(ItemStack itemStack, Player player) {
        player.containerMenu.slotsChanged(player.getInventory());
    }

    /**
     * Get the maximum stack size for this item in the current container.
     *
     * @param container the container
     * @param slotNum   the slot index
     * @param itemStack the item stack
     * @return the max stack size
     */
    default int getMaxStackSize(Container container, int slotNum, ItemStack itemStack) {
        return container.getMaxStackSize(itemStack);
    }

    /**
     * @see net.minecraft.world.item.BundleItem#playRemoveOneSound(Entity)
     */
    default void playRemoveOneSound(Player player) {
        player.playSound(SoundEvents.BUNDLE_REMOVE_ONE, 0.8F, 0.8F + player.level().getRandom().nextFloat() * 0.4F);
    }

    /**
     * @see net.minecraft.world.item.BundleItem#playInsertSound(Entity)
     */
    default void playInsertSound(Player player) {
        player.playSound(SoundEvents.BUNDLE_INSERT, 0.8F, 0.8F + player.level().getRandom().nextFloat() * 0.4F);
    }

    /**
     * @see net.minecraft.world.item.BundleItem#playInsertFailSound(Entity)
     */
    default void playInsertFailSound(Player player) {
        player.playSound(SoundEvents.BUNDLE_INSERT_FAIL, 1.0F, 1.0F);
    }
}
