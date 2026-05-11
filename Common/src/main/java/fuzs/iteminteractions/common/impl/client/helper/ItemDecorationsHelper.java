package fuzs.iteminteractions.common.impl.client.helper;

import fuzs.iteminteractions.common.api.v1.world.item.storage.ItemStorageHolder;
import fuzs.iteminteractions.common.impl.ItemInteractions;
import fuzs.iteminteractions.common.impl.config.ClientConfig;
import fuzs.iteminteractions.common.impl.world.item.container.ItemContentsProviders;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

public class ItemDecorationsHelper {
    @Nullable
    private static Slot slotBeingRendered;

    public static void renderItemDecorations(GuiGraphicsExtractor guiGraphics, Font font, ItemStack itemStack, int itemPosX, int itemPosY) {
        if (!ItemInteractions.CONFIG.get(ClientConfig.class).containerItemIndicator) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        // prevent rendering on items used as icons for creative mode tabs and for backpacks in locked slots (like Inmis)
        if (!(minecraft.screen instanceof AbstractContainerScreen<?> screen)) {
            return;
        }

        ItemStorageHolder holder = ItemContentsProviders.get(itemStack);
        if (!holder.isEmpty() && isValidSlot(slotBeingRendered, itemStack, minecraft.player)) {
            ItemStack carriedStack = screen.getMenu().getCarried();
            if (itemStack != carriedStack) {
                ItemDecorationsType type = ItemDecorationsType.pickType(holder, itemStack, carriedStack, minecraft.player);
                if (type != null) {
                    renderItemDecoratorType(type, guiGraphics, font, itemPosX, itemPosY);
                }
            }
        }
    }

    private static boolean isValidSlot(@Nullable Slot slot, ItemStack itemStack, Player player) {
        if (slot == null || slot.getItem() != itemStack) {
            return false;
        } else if (!slot.allowModification(player)) {
            return false;
        } else if (slot instanceof CreativeModeInventoryScreen.CustomCreativeSlot) {
            // filter out creative mode inventory slots on the client
            return false;
        } else if (slot.container instanceof CraftingContainer) {
            // do not allow interactions in the crafting grid, the crafting result will not update,
            // so players can remove items and get them back from the crafted item
            return false;
        } else {
            return true;
        }
    }

    @SuppressWarnings("ConstantConditions")
    private static void renderItemDecoratorType(ItemDecorationsType type, GuiGraphicsExtractor guiGraphics, Font font, int itemPosX, int itemPosY) {
        guiGraphics.pose().pushMatrix();
        guiGraphics.text(font,
                type.getString(),
                itemPosX + 19 - 2 - type.getWidth(font),
                itemPosY + 6 + 3,
                type.getColor(),
                true);
        guiGraphics.pose().popMatrix();
    }

    public static void setSlotBeingRendered(@Nullable Slot slotBeingRendered) {
        ItemDecorationsHelper.slotBeingRendered = slotBeingRendered;
    }
}
