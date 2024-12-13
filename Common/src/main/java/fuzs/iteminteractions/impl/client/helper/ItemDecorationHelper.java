package fuzs.iteminteractions.impl.client.helper;

import com.mojang.blaze3d.systems.RenderSystem;
import fuzs.iteminteractions.api.v1.provider.ItemContentsBehavior;
import fuzs.iteminteractions.impl.ItemInteractions;
import fuzs.iteminteractions.impl.config.ClientConfig;
import fuzs.iteminteractions.impl.world.item.container.ItemContentsProviders;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class ItemDecorationHelper {
    @Nullable
    private static Slot slotBeingRendered;

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
                    resetRenderState();
                    renderItemDecoratorType(type, guiGraphics, font, itemPosX, itemPosY);
                    resetRenderState();
                }
            }
        }
    }

    private static boolean isValidSlot(@Nullable Slot slot, ItemStack itemStack, Player player) {
        // filter out creative mode inventory slots on the client
        return slot != null && slot.getItem() == itemStack && slot.allowModification(player) &&
                !(slot instanceof CreativeModeInventoryScreen.CustomCreativeSlot);
    }

    @SuppressWarnings("ConstantConditions")
    private static void renderItemDecoratorType(ItemDecoratorType type, GuiGraphics guiGraphics, Font font, int itemPosX, int itemPosY) {
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(0.0, 0.0, 200.0);
        guiGraphics.drawSpecial((MultiBufferSource bufferSource) -> {
            font.drawInBatch(type.getString(),
                    (float) (itemPosX + 19 - 2 - type.getWidth(font)),
                    (float) (itemPosY + 6 + 3),
                    type.getColor(),
                    true,
                    guiGraphics.pose().last().pose(),
                    bufferSource,
                    Font.DisplayMode.NORMAL,
                    0,
                    0xF000F0);
        });
        guiGraphics.pose().popPose();
    }

    private static void resetRenderState() {
        RenderSystem.enableDepthTest();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
    }

    public static void setSlotBeingRendered(@Nullable Slot slotBeingRendered) {
        ItemDecorationHelper.slotBeingRendered = slotBeingRendered;
    }
}
