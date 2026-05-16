package fuzs.iteminteractions.common.api.v1.world.item.storage;

import com.mojang.serialization.MapCodec;
import fuzs.iteminteractions.common.api.v1.world.inventory.tooltip.ItemContentsTooltip;
import fuzs.iteminteractions.common.api.v1.world.item.DyeBackedColor;
import fuzs.iteminteractions.common.impl.handler.EnderChestSyncHandler;
import fuzs.iteminteractions.common.impl.init.ModRegistry;
import net.minecraft.core.NonNullList;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;
import java.util.OptionalInt;

public class EnderChestStorage implements VisualItemStorage {
    public static final ItemStorage INSTANCE = new EnderChestStorage();
    public static final MapCodec<ItemStorage> CODEC = MapCodec.unit(INSTANCE);
    /**
     * Pretty ender color from the <a href="https://www.curseforge.com/minecraft/mc-mods/tinted">Tinted</a> mod.
     */
    private static final DyeBackedColor DEFAULT_ENDER_CHEST_COLOR = DyeBackedColor.fromRgb(0X2A6255);

    protected EnderChestStorage() {
        // NO-OP
    }

    @Override
    public boolean hasContents(ItemStack itemStack) {
        return true;
    }

    @Override
    public SimpleContainer getItemContainer(ItemStack itemStack, Player player, boolean isMutable) {
        return player.getEnderChestInventory();
    }

    @Override
    public int getGridWidth(int itemCount) {
        return 9;
    }

    @Override
    public int getGridHeight(int itemCount) {
        int gridWidth = this.getGridWidth(itemCount);
        // Attempt to support mods that add more ender chest rows, like the Carpet mod.
        if (itemCount % gridWidth == 0) {
            return itemCount / gridWidth;
        } else {
            return 3;
        }
    }

    @Override
    public TooltipComponent createTooltipImageComponent(ItemStack itemStack, Player player, NonNullList<ItemStack> itemList) {
        return new ItemContentsTooltip(itemList,
                this.getSelectedItem(itemStack),
                this.getGridWidth(itemList.size()),
                this.getGridHeight(itemList.size()),
                DEFAULT_ENDER_CHEST_COLOR);
    }

    @Override
    public Optional<Boolean> isBarVisible(ItemStack itemStack, Player player) {
        return Optional.empty();
    }

    @Override
    public OptionalInt getBarWidth(ItemStack itemStack, Player player) {
        return OptionalInt.empty();
    }

    @Override
    public OptionalInt getBarColor(ItemStack itemStack, Player player) {
        return OptionalInt.empty();
    }

    @Override
    public void broadcastChangesOnContainerMenu(ItemStack itemStack, Player player) {
        if (player.level().isClientSide()) {
            // Will only actually broadcast when in the creative menu as that menu needs manual syncing.
            NonNullList<ItemStack> itemList = this.getItemContainer(itemStack, player, false).getItems();
            EnderChestSyncHandler.broadcastCreativeState(player, itemList);
        } else {
            // Sync the full state, the client ender chest will otherwise likely be messed up.
            // Useful for nested ender chests when paired with packet spam and latency.
            EnderChestSyncHandler.broadcastFullState((ServerPlayer) player);
        }
    }

    @Override
    public ItemStorageType<?> getType() {
        return ModRegistry.ENDER_CHEST_ITEM_CONTENTS_PROVIDER_TYPE.value();
    }
}
