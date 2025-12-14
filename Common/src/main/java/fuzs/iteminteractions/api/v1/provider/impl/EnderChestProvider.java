package fuzs.iteminteractions.api.v1.provider.impl;

import com.mojang.serialization.MapCodec;
import fuzs.iteminteractions.api.v1.DyeBackedColor;
import fuzs.iteminteractions.api.v1.provider.TooltipProvider;
import fuzs.iteminteractions.api.v1.tooltip.ItemContentsTooltip;
import fuzs.iteminteractions.impl.client.handler.ClientEnderChestHandler;
import fuzs.iteminteractions.impl.handler.EnderChestSyncHandler;
import fuzs.iteminteractions.impl.init.ModRegistry;
import net.minecraft.core.NonNullList;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;

public class EnderChestProvider implements TooltipProvider {
    /**
     * Pretty ender color from <a href="https://www.curseforge.com/minecraft/mc-mods/tinted">Tinted mod</a>.
     */
    private static final DyeBackedColor DEFAULT_ENDER_CHEST_COLOR = DyeBackedColor.fromRgb(0X2A6255);
    private static final int GRID_SIZE_X = 9;
    public static final MapCodec<EnderChestProvider> CODEC = MapCodec.unit(EnderChestProvider::new);

    @Override
    public SimpleContainer getItemContainer(ItemStack containerStack, Player player, boolean allowSaving) {
        return player.getEnderChestInventory();
    }

    @Override
    public boolean hasContents(ItemStack containerStack) {
        return true;
    }

    @Override
    public TooltipComponent createTooltipImageComponent(ItemStack containerStack, Player player, NonNullList<ItemStack> items) {
        return new ItemContentsTooltip(items, GRID_SIZE_X, this.getGridSizeY(items), DEFAULT_ENDER_CHEST_COLOR);
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
        SimpleContainer container = this.getItemContainer(containerStack, player, false);
        if (player.level().isClientSide()) {
            // will only actually broadcast when in the creative menu as that menu needs manual syncing
            ClientEnderChestHandler.broadcastFullState(container.getItems());
        } else {
            // sync full state, the client ender chest will otherwise likely be messed up when using item interactions
            // for the ender chest inside the ender chest menu due to packet spam and corresponding delays
            EnderChestSyncHandler.broadcastFullState((ServerPlayer) player);
        }
    }

    @Override
    public Type getType() {
        return ModRegistry.ENDER_CHEST_ITEM_CONTENTS_PROVIDER_TYPE.value();
    }
}
