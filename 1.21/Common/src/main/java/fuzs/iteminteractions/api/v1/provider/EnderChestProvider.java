package fuzs.iteminteractions.api.v1.provider;

import com.google.gson.JsonObject;
import fuzs.iteminteractions.api.v1.tooltip.ContainerItemTooltip;
import fuzs.iteminteractions.impl.client.handler.ClientEnderChestHandler;
import fuzs.iteminteractions.impl.handler.EnderChestSyncHandler;
import net.minecraft.core.NonNullList;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;

public class EnderChestProvider implements TooltipItemContainerProvider {
    /**
     * Pretty ender color from <a href="https://www.curseforge.com/minecraft/mc-mods/tinted">Tinted mod</a>.
     */
    private static final float[] DEFAULT_ENDER_CHEST_COLOR = {0.16470589F, 0.38431373F, 0.33333334F};
    private static final int GRID_SIZE_X = 9;

    @Override
    public SimpleContainer getItemContainer(ItemStack containerStack, Player player, boolean allowSaving) {
        return player.getEnderChestInventory();
    }

    @Override
    public boolean hasContents(ItemStack containerStack) {
        return true;
    }

    @Override
    public boolean canProvideTooltipImage(ItemStack containerStack, Player player) {
        return !this.getItemContainer(containerStack, player, false).isEmpty();
    }

    @Override
    public TooltipComponent createTooltipImageComponent(ItemStack containerStack, Player player, NonNullList<ItemStack> items) {
        return new ContainerItemTooltip(items, GRID_SIZE_X, this.getGridSizeY(items), DEFAULT_ENDER_CHEST_COLOR);
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
        if (player.level().isClientSide) {
            // will only actually broadcast when in creative menu as that menu needs manual syncing
            ClientEnderChestHandler.broadcastFullState(container.items);
        } else if (player.containerMenu instanceof ChestMenu menu && menu.getContainer() == container) {
            // sync full state, client ender chest will otherwise likely be messed up when using item interactions
            // for the ender chest inside the ender chest menu due to packet spam and corresponding delays
            EnderChestSyncHandler.broadcastFullState((ServerPlayer) player);
        }
    }

    @Override
    public void toJson(JsonObject jsonObject) {
        // NO-OP
    }
}
