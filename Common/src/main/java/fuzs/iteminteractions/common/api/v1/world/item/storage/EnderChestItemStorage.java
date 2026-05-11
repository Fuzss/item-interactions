package fuzs.iteminteractions.common.api.v1.world.item.storage;

import com.mojang.serialization.MapCodec;
import fuzs.iteminteractions.common.api.v1.world.item.DyeBackedColor;
import fuzs.iteminteractions.common.api.v1.world.inventory.tooltip.ItemContentsTooltip;
import fuzs.iteminteractions.common.impl.handler.EnderChestSyncHandler;
import fuzs.iteminteractions.common.impl.init.ModRegistry;
import fuzs.iteminteractions.common.impl.world.inventory.ContainerSlotHelper;
import net.minecraft.core.NonNullList;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;

public class EnderChestItemStorage implements ItemStorageWithTooltip {
    /**
     * Pretty ender color from <a href="https://www.curseforge.com/minecraft/mc-mods/tinted">Tinted mod</a>.
     */
    private static final DyeBackedColor DEFAULT_ENDER_CHEST_COLOR = DyeBackedColor.fromRgb(0X2A6255);
    private static final int GRID_SIZE_X = 9;
    public static final MapCodec<EnderChestItemStorage> CODEC = MapCodec.unit(EnderChestItemStorage::new);

    @Override
    public SimpleContainer getItemContainer(ItemStack containerStack, Player player, boolean allowSaving) {
        return player.getEnderChestInventory();
    }

    @Override
    public boolean hasContents(ItemStack containerStack) {
        return true;
    }

    @Override
    public TooltipComponent createTooltipImageComponent(ItemStack itemStack, Player player, NonNullList<ItemStack> items) {
        int selectedItem = ContainerSlotHelper.getSelectedItem(itemStack);
        return new ItemContentsTooltip(items,
                GRID_SIZE_X,
                this.getGridSizeY(items),
                DEFAULT_ENDER_CHEST_COLOR,
                selectedItem);
    }

    private int getGridSizeY(NonNullList<ItemStack> items) {
        if (items.size() % GRID_SIZE_X == 0) {
            // try support mods that add more ender chest rows, like Carpet mod
            return items.size() / GRID_SIZE_X;
        } else {
            return 3;
        }
    }

    @Override
    public void broadcastContainerChanges(ItemStack containerStack, Player player) {
        if (player.level().isClientSide()) {
            // will only actually broadcast when in the creative menu as that menu needs manual syncing
            NonNullList<ItemStack> items = this.getItemContainer(containerStack, player, false).getItems();
            EnderChestSyncHandler.broadcastCreativeState(player, items);
        } else {
            // sync full state, the client ender chest will otherwise likely be messed up when using item interactions
            // for the ender chest inside the ender chest menu due to packet spam and corresponding delays
            EnderChestSyncHandler.broadcastFullState((ServerPlayer) player);
        }
    }

    @Override
    public Type<?> getType() {
        return ModRegistry.ENDER_CHEST_ITEM_CONTENTS_PROVIDER_TYPE.value();
    }
}
