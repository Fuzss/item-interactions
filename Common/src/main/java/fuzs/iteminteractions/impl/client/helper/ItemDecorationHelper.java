package fuzs.iteminteractions.impl.client.helper;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.Tesselator;
import fuzs.iteminteractions.impl.ItemInteractions;
import fuzs.iteminteractions.impl.config.ClientConfig;
import fuzs.iteminteractions.api.v1.provider.ItemContainerBehavior;
import fuzs.iteminteractions.impl.world.item.container.ItemContainerProviders;
import fuzs.puzzleslib.api.client.init.v1.DynamicItemDecorator;
import fuzs.puzzleslib.api.core.v1.ModLoaderEnvironment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.function.BooleanSupplier;

public class ItemDecorationHelper {
    private static final Map<ItemContainerBehavior, DynamicItemDecorator> DECORATORS_CACHE = Maps.newIdentityHashMap();

    @Nullable
    private static Slot activeSlot;

    private static DynamicItemDecorator getDynamicItemDecorator(ItemDecoratorType.Provider filter, BooleanSupplier allow) {
        return (GuiGraphics guiGraphics, Font font, ItemStack stack, int itemPosX, int itemPosY) -> {
            if (!allow.getAsBoolean()) return false;
            return tryRenderItemDecorations(guiGraphics, font, stack, itemPosX, itemPosY, filter);
        };
    }

    @SuppressWarnings("ConstantConditions")
    private static boolean tryRenderItemDecorations(GuiGraphics guiGraphics, Font font, ItemStack stack, int itemPosX, int itemPosY, ItemDecoratorType.Provider filter) {
        Minecraft minecraft = Minecraft.getInstance();
        // prevent rendering on items used as icons for creative mode tabs and for backpacks in locked slots (like Inmis)
        if (!(minecraft.screen instanceof AbstractContainerScreen<?> screen)) return false;
        AbstractContainerMenu menu = screen.getMenu();
        if (activeSlot != null && activeSlot.getItem() == stack && activeSlot.allowModification(minecraft.player) && !isCreativeInventorySlot(menu, activeSlot)) {
            ItemStack carriedStack = menu.getCarried();
            if (stack != carriedStack) {
                ItemDecoratorType type = filter.get(screen, stack, carriedStack);
                if (type != ItemDecoratorType.NONE) {
                    guiGraphics.pose().pushPose();
                    guiGraphics.pose().translate(0.0, 0.0, 200.0);
                    MultiBufferSource.BufferSource bufferSource = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());
                    font.drawInBatch(type.getText(), (float) (itemPosX + 19 - 2 - type.getWidth(font)), (float) (itemPosY + 6 + 3), type.getColor(), true, guiGraphics.pose().last().pose(), bufferSource, Font.DisplayMode.NORMAL, 0, 15728880);
                    bufferSource.endBatch();
                    guiGraphics.pose().popPose();
                    // font renderer modifies render states, so this tells the implementation to reset them
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean isCreativeInventorySlot(AbstractContainerMenu menu, Slot slot) {
        // filter out creative mode inventory slots on the client
        return ModLoaderEnvironment.INSTANCE.isClient() && slot instanceof CreativeModeInventoryScreen.CustomCreativeSlot;
    }

    public static void render(GuiGraphics guiGraphics, Font font, ItemStack stack, int itemPosX, int itemPosY) {
        ItemContainerBehavior behavior = ItemContainerProviders.INSTANCE.get(stack);
        if (!behavior.isEmpty()) {
            resetRenderState();
            DynamicItemDecorator itemDecorator = DECORATORS_CACHE.computeIfAbsent(behavior, $ -> ItemDecorationHelper.getDynamicItemDecorator((AbstractContainerScreen<?> screen, ItemStack containerStack, ItemStack carriedStack) -> {
                return ItemDecoratorType.getItemDecoratorType(behavior, containerStack, carriedStack, screen.minecraft.player);
            }, () -> ItemInteractions.CONFIG.get(ClientConfig.class).containerItemIndicator));
            if (itemDecorator.renderItemDecorations(guiGraphics, font, stack, itemPosX, itemPosY)) {
                resetRenderState();
            }
        }
    }

    private static void resetRenderState() {
        RenderSystem.enableDepthTest();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
    }

    public static void clearCache() {
        DECORATORS_CACHE.clear();
    }

    public static void setActiveSlot(@Nullable Slot activeSlot) {
        ItemDecorationHelper.activeSlot = activeSlot;
    }
}
