package fuzs.iteminteractions.api.v1.provider;

import com.google.gson.JsonObject;
import fuzs.iteminteractions.api.v1.tooltip.ContainerItemTooltip;
import fuzs.iteminteractions.impl.init.ModRegistry;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

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
    public boolean hasItemContainerData(ItemStack containerStack) {
        return true;
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
    public boolean canProvideTooltipImage(ItemStack containerStack, Player player) {
        return !this.getItemContainer(containerStack, player, false).isEmpty();
    }

    @Override
    public TooltipComponent createTooltipImageComponent(ItemStack containerStack, Player player, NonNullList<ItemStack> items) {
        return new ContainerItemTooltip(items, GRID_SIZE_X, this.getGridSizeY(player), DEFAULT_ENDER_CHEST_COLOR);
    }

    private int getGridSizeY(Player player) {
        if (player.getEnderChestInventory().getContainerSize() % GRID_SIZE_X == 0) {
            // try support mods that add more ender chest rows
            return player.getEnderChestInventory().size / GRID_SIZE_X;
        } else {
            return 3;
        }
    }

    @Override
    public void broadcastContainerChanges(Player player) {
        if (player.level().isClientSide) {
            // will only actually broadcast when in creative menu as that menu needs manual syncing
            ModRegistry.ENDER_CHEST_MENU_CAPABILITY.get(player).getEnderChestMenu().broadcastChanges();
        } else if (player.containerMenu instanceof ChestMenu menu && menu.getContainer() == player.getEnderChestInventory()) {
            // sync full state, client ender chest will otherwise likely be messed up when using item interactions
            // for the ender chest inside the ender chest menu due to packet spam and corresponding delays
            ModRegistry.ENDER_CHEST_MENU_CAPABILITY.get(player).getEnderChestMenu().broadcastFullState();
        }
    }

    @Override
    public void toJson(JsonObject jsonObject) {
        // NO-OP
    }
}
