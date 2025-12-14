package fuzs.iteminteractions.mixin.client;

import fuzs.iteminteractions.impl.client.helper.ItemDecorationHelper;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractContainerScreen.class)
abstract class AbstractContainerScreenMixin extends Screen {

    protected AbstractContainerScreenMixin(Component component) {
        super(component);
    }

    @Inject(method = "renderSlot", at = @At("HEAD"))
    private void renderSlot$1(GuiGraphics guiGraphics, Slot slot, int mouseX, int mouseY, CallbackInfo callback) {
        ItemDecorationHelper.setSlotBeingRendered(slot);
    }

    @Inject(method = "renderSlot", at = @At("RETURN"))
    private void renderSlot$2(GuiGraphics guiGraphics, Slot slot, int mouseX, int mouseY, CallbackInfo callback) {
        ItemDecorationHelper.setSlotBeingRendered(null);
    }
}
