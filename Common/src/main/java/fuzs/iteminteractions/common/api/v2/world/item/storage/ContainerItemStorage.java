package fuzs.iteminteractions.common.api.v2.world.item.storage;

import fuzs.iteminteractions.common.impl.config.ClickActionScheme;
import fuzs.iteminteractions.common.impl.init.ModRegistry;
import fuzs.iteminteractions.common.impl.world.inventory.ItemSlot;
import fuzs.iteminteractions.common.impl.world.item.component.ControlScheme;
import fuzs.iteminteractions.common.impl.world.item.component.SelectedItem;
import fuzs.iteminteractions.common.impl.world.item.container.ItemStackingContext;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
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

    default boolean extractSingleItemOnly(ItemStack itemStack, Player player) {
        return ModRegistry.CONTROL_SCHEME_ATTACHMENT_TYPE.getOrDefault(player, ControlScheme.DEFAULT).moveSingleItem();
    }

    private ClickActionScheme controlScheme(ItemStack itemStack, Player player) {
        if (this.extractSingleItemOnly(itemStack, player)) {
            return ClickActionScheme.SPLIT_INPUT;
        } else {
            return ModRegistry.CONTROL_SCHEME_ATTACHMENT_TYPE.getOrDefault(player, ControlScheme.DEFAULT)
                    .controlScheme();
        }
    }

    @Override
    default int getSelectedItem(ItemStack itemStack, Player player) {
        return ModRegistry.SELECTED_ITEM_ATTACHMENT_TYPE.getOrDefault(player, SelectedItem.DEFAULT).selectedItem();
    }

    default void setSelectedItem(ItemStack itemStack, Player player, int selectedItem) {
        ModRegistry.SELECTED_ITEM_ATTACHMENT_TYPE.set(player, SelectedItem.of(selectedItem));
    }

    @Override
    default void toggleSelectedItem(ItemStack itemStack, Player player, int selectedItem, boolean slotClicked) {
        if (!slotClicked) {
            this.setSelectedItem(itemStack, player, selectedItem);
        }
    }

    default int getPrioritizedSlot(ItemStack itemStack, Player player) {
        int prioritizedSlot = this.getSelectedItem(itemStack, player);
        if (this.offsetPrioritizedSlot(prioritizedSlot)) {
            return prioritizedSlot + 1;
        } else {
            return prioritizedSlot;
        }
    }

    default void setPrioritizedSlot(ItemStack itemStack, Player player, int prioritizedSlot) {
        this.setSelectedItem(itemStack,
                player,
                this.offsetPrioritizedSlot(prioritizedSlot) ? prioritizedSlot - 1 : prioritizedSlot);
    }

    default boolean offsetPrioritizedSlot(int prioritizedSlot) {
        return prioritizedSlot != SelectedItem.DEFAULT_SELECTED_ITEM
                && this.getRemovalDirection() == Direction.AxisDirection.POSITIVE;
    }

    @Override
    default int scrollSelectedItem(ItemStack itemStack, Player player, Container container, Vector2ic scrollXY) {
        int selectedItem = this.getSelectedItem(itemStack, player);
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
            if (selectedItem >= 0 && selectedItem < container.getContainerSize() && !container.getItem(selectedItem)
                    .isEmpty()) {
                return selectedItem;
            }
        }

        return SelectedItem.DEFAULT_SELECTED_ITEM;
    }

    @Override
    default boolean mayPlace(ItemStack otherItem) {
        return true;
    }

    @Override
    default Container getItemContainer(ItemStack itemStack, Player player) {
        return this.getItemContainer(itemStack, player, false);
    }

    SimpleContainer getItemContainer(ItemStack itemStack, Player player, boolean isMutable);

    @Override
    default boolean canAddItem(ItemStack itemStack, ItemStack otherItem, Player player) {
        return this.getItemContainer(itemStack, player, false).canAddItem(otherItem);
    }

    @Override
    default int getAcceptableItemCount(ItemStack itemStack, ItemStack otherItem, Player player) {
        return otherItem.getCount();
    }

    /**
     * @see net.minecraft.world.item.BundleItem#overrideStackedOnOther(ItemStack, Slot, ClickAction, Player)
     */
    @Override
    default boolean overrideStackedOnOther(ItemStorageHolder holder, ItemStack itemStack, Slot slot, ClickAction clickAction, Player player) {
        boolean extractSingleItemOnly = this.extractSingleItemOnly(itemStack, player);
        ClickActionScheme scheme = this.controlScheme(itemStack, player);
        ItemStackingContext context = new ItemStackingContext(holder, this, player);
        ItemStack otherItem = slot.getItem();
        if (scheme.removeStackedOnOther(clickAction) && (otherItem.isEmpty() || extractSingleItemOnly)) {
            ItemSlot itemSlot = context.removeOne(itemStack, otherItem);
            if (!itemSlot.item().isEmpty()) {
                context.tryInsert(itemStack, slot.safeInsert(itemSlot.item()), itemSlot.slotNum());
                if (!extractSingleItemOnly) {
                    this.playRemoveOneSound(player);
                }
            }

            this.broadcastChangesOnContainerMenu(itemStack, player);
            return true;
        } else if (scheme.insertStackedOnOther(clickAction) && (!otherItem.isEmpty() || extractSingleItemOnly)) {
            otherItem = slot.safeTake(otherItem.getCount(), otherItem.getCount(), player);
            int transferredCount = context.tryInsert(itemStack, otherItem);
            otherItem.shrink(transferredCount);
            if (!extractSingleItemOnly) {
                if (transferredCount > 0) {
                    this.playInsertSound(player);
                } else {
                    this.playInsertFailSound(player);
                }
            }

            slot.safeInsert(otherItem);
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
        boolean extractSingleItemOnly = this.extractSingleItemOnly(itemStack, player);
        ClickActionScheme scheme = this.controlScheme(itemStack, player);
        if (clickAction == ClickAction.PRIMARY && itemHeldByCursor.isEmpty()) {
            if (!extractSingleItemOnly) {
                this.toggleSelectedItem(itemStack, player, SelectedItem.DEFAULT_SELECTED_ITEM, true);
                slot.set(itemStack);
                return false;
            } else {
                return true;
            }
        } else {
            ItemStackingContext context = new ItemStackingContext(holder, this, player);
            if (scheme.removeOtherStackedOnMe(clickAction) && (itemHeldByCursor.isEmpty() || extractSingleItemOnly)) {
                if (slot.allowModification(player)) {
                    ItemStack itemRemainder = context.removeOne(itemStack, itemHeldByCursor).item();
                    if (!itemRemainder.isEmpty()) {
                        // When extracting single items only, the item held by cursor may not be empty, so we cannot just replace it straight away.
                        if (itemHeldByCursor.isEmpty()) {
                            slotHeldByCursor.set(itemRemainder);
                        } else {
                            itemHeldByCursor.grow(itemRemainder.getCount());
                        }

                        if (!extractSingleItemOnly) {
                            this.playRemoveOneSound(player);
                        }
                    }
                }

                slot.set(itemStack);
                this.broadcastChangesOnContainerMenu(itemStack, player);
                return true;
            } else if (scheme.insertOtherStackedOnMe(clickAction) && !itemHeldByCursor.isEmpty()) {
                if (slot.allowModification(player)) {
                    int transferredCount = context.tryInsert(itemStack, itemHeldByCursor);
                    itemHeldByCursor.shrink(transferredCount);
                    if (!extractSingleItemOnly) {
                        if (transferredCount > 0) {
                            this.playInsertSound(player);
                        } else {
                            this.playInsertFailSound(player);
                        }
                    }
                }

                slot.set(itemStack);
                this.broadcastChangesOnContainerMenu(itemStack, player);
                return true;
            } else {
                this.toggleSelectedItem(itemStack, player, SelectedItem.DEFAULT_SELECTED_ITEM, true);
                slot.set(itemStack);
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

    default Direction.AxisDirection getRemovalDirection() {
        return Direction.AxisDirection.NEGATIVE;
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
    @Override
    default void playRemoveOneSound(Player player) {
        player.playSound(SoundEvents.BUNDLE_REMOVE_ONE, 0.8F, 0.8F + player.level().getRandom().nextFloat() * 0.4F);
    }

    /**
     * @see net.minecraft.world.item.BundleItem#playInsertSound(Entity)
     */
    @Override
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
