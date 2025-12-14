package fuzs.iteminteractions.impl.client.helper;

import fuzs.iteminteractions.api.v1.provider.ItemContentsBehavior;
import fuzs.iteminteractions.impl.ItemInteractions;
import fuzs.iteminteractions.impl.config.ClientConfig;
import fuzs.iteminteractions.impl.world.item.container.ItemContentsProviders;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

public class ItemDecorationHelper {
    @Nullable private static Slot slotBeingRendered;

    public static void renderItemDecorations(GuiGraphics guiGraphics, Font font, ItemStack itemStack, int itemPosX, int itemPosY) {
        if (!ItemInteractions.CONFIG.get(ClientConfig.class).containerItemIndicator) return;
        Minecraft minecraft = Minecraft.getInstance();
        // prevent rendering on items used as icons for creative mode tabs and for backpacks in locked slots (like Inmis)
        if (!(minecraft.screen instanceof AbstractContainerScreen<?> screen)) return;
        ItemContentsBehavior behavior = ItemContentsProviders.get(itemStack);
        if (!behavior.isEmpty() && isValidSlot(slotBeingRendered, itemStack, minecraft.player)) {
            ItemStack carriedStack = screen.getMenu().getCarried();
            if (itemStack != carriedStack) {
                ItemDecoratorType type = ItemDecoratorType.getItemDecoratorType(behavior,
                        itemStack,
                        carriedStack,
                        minecraft.player);
                if (type.mayRender()) {
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
    private static void renderItemDecoratorType(ItemDecoratorType type, GuiGraphics guiGraphics, Font font, int itemPosX, int itemPosY) {
        guiGraphics.pose().pushMatrix();
        guiGraphics.drawString(font,
                type.getString(),
                itemPosX + 19 - 2 - type.getWidth(font),
                itemPosY + 6 + 3,
                type.getColor(),
                true);
        guiGraphics.pose().popMatrix();
    }

    public static void setSlotBeingRendered(@Nullable Slot slotBeingRendered) {
        ItemDecorationHelper.slotBeingRendered = slotBeingRendered;
    }
}
