package fuzs.iteminteractions.impl.client.handler;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.datafixers.util.Pair;
import fuzs.iteminteractions.api.v1.provider.ItemContentsBehavior;
import fuzs.iteminteractions.impl.ItemInteractions;
import fuzs.iteminteractions.impl.config.ClientConfig;
import fuzs.iteminteractions.impl.config.ServerConfig;
import fuzs.iteminteractions.impl.network.client.C2SContainerClientInputMessage;
import fuzs.iteminteractions.impl.world.inventory.ContainerSlotHelper;
import fuzs.iteminteractions.impl.world.item.container.ItemContentsProviders;
import fuzs.puzzleslib.api.event.v1.core.EventResult;
import fuzs.puzzleslib.api.event.v1.data.DefaultedFloat;
import fuzs.puzzleslib.api.event.v1.data.MutableValue;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.Holder;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class ClientInputActionHandler {

    public static EventResult onBeforeMouseRelease(AbstractContainerScreen<?> screen, double mouseX, double mouseY, int button) {
        // prevent vanilla double click feature from interfering with our precision mode, adding an unnecessary delay when quickly inserting items via left-click
        // it wouldn't work anyway, and right-click is fine, leading to inconsistent behavior
        if (precisionModeAllowedAndActive() && !getContainerItemStack(screen, false).isEmpty()) {
            screen.doubleclick = false;
        }

        return EventResult.PASS;
    }

    public static void onAfterRender(AbstractContainerScreen<?> screen, GuiGraphics guiGraphics, int mouseX, int mouseY, float tickDelta) {
        // renders vanilla item tooltips when a stack is carried and the cursor hovers over a container item
        // intended to be used with single item extraction/insertion feature to be able to continuously see what's going on in the container item
        if (!ItemInteractions.CONFIG.get(ClientConfig.class).carriedItemTooltips.isActive()) return;
        if (!screen.getMenu().getCarried().isEmpty()) {
            ItemStack stack = getContainerItemStack(screen, false);
            if (!stack.isEmpty()) {
                guiGraphics.renderTooltip(screen.font, stack, mouseX, mouseY);
            }
        }
    }

    public static EventResult onBeforeMouseScroll(AbstractContainerScreen<?> screen, double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        // allows to scroll between filled slots on a container items tooltip to select the slot to be interacted with next
        if (verticalAmount == 0.0) return EventResult.PASS;
        if (!ItemInteractions.CONFIG.get(ClientConfig.class).revealContents.isActive()) return EventResult.PASS;
        if (precisionModeAllowedAndActive()) {
            Slot hoveredSlot = screen.hoveredSlot;
            if (hoveredSlot != null) {
                if (!ItemContentsProviders.get(screen.getMenu().getCarried()).isEmpty() ||
                        !ItemContentsProviders.get(hoveredSlot.getItem()).isEmpty()) {
                    int mouseButton = (ItemInteractions.CONFIG.get(ClientConfig.class).invertPrecisionModeScrolling ?
                            verticalAmount < 0.0 : verticalAmount > 0.0) ? InputConstants.MOUSE_BUTTON_RIGHT :
                            InputConstants.MOUSE_BUTTON_LEFT;
                    syncContainerClientInput(screen.minecraft.player);
                    screen.slotClicked(hoveredSlot, hoveredSlot.index, mouseButton, ClickType.PICKUP);
                    return EventResult.INTERRUPT;
                }
            }
        } else if (ItemInteractions.CONFIG.get(ServerConfig.class).allowSlotCycling) {
            ItemStack carriedStack = screen.getMenu().getCarried();
            if (!carriedStack.isEmpty() &&
                    !ItemInteractions.CONFIG.get(ClientConfig.class).carriedItemTooltips.isActive()) {
                return EventResult.PASS;
            }
            Pair<ItemStack, ItemContentsBehavior> pair = getContainerPair(screen, true);
            ItemStack itemStack = pair.getFirst();
            if (!itemStack.isEmpty()) {
                int oldContainerSlot = ContainerSlotHelper.getCurrentContainerSlot(screen.minecraft.player);
                SimpleContainer container = ItemContentsProviders.get(itemStack)
                        .getItemContainerView(itemStack, screen.minecraft.player);
                int newContainerSlot = ContainerSlotHelper.findClosestSlotWithContent(container,
                        oldContainerSlot,
                        verticalAmount < 0.0);
                ContainerSlotHelper.setCurrentContainerSlot(screen.minecraft.player, newContainerSlot);
                if (oldContainerSlot != -1) {
                    pair.getSecond().provider().onToggleSelectedItem(itemStack, oldContainerSlot, newContainerSlot);
                }
                syncContainerClientInput(screen.minecraft.player);
                return EventResult.INTERRUPT;
            }
        }

        return EventResult.PASS;
    }

    public static ItemStack getContainerItemStack(AbstractContainerScreen<?> screen, boolean requireItemContainerData) {
        return getContainerPair(screen, requireItemContainerData).getFirst();
    }

    public static Pair<ItemStack, ItemContentsBehavior> getContainerPair(AbstractContainerScreen<?> screen, boolean requireItemContainerData) {
        ItemStack itemStack = screen.getMenu().getCarried();
        ItemContentsBehavior behavior = ItemContentsProviders.get(itemStack);
        if (!behavior.isEmpty() && (!requireItemContainerData || behavior.provider().hasContents(itemStack))) {
            return Pair.of(itemStack, behavior);
        } else if (screen.hoveredSlot != null) {
            itemStack = screen.hoveredSlot.getItem();
            behavior = ItemContentsProviders.get(itemStack);
            if (!behavior.isEmpty() && (!requireItemContainerData || behavior.provider().hasContents(itemStack))) {
                return Pair.of(itemStack, behavior);
            }
        }

        return Pair.of(ItemStack.EMPTY, ItemContentsBehavior.empty());
    }

    public static EventResult onPlaySoundAtPosition(Level level, Entity entity, MutableValue<Holder<SoundEvent>> sound, MutableValue<SoundSource> source, DefaultedFloat volume, DefaultedFloat pitch) {
        if (!ItemInteractions.CONFIG.get(ClientConfig.class).disableInteractionSounds) return EventResult.PASS;
        if (source.get() == SoundSource.PLAYERS && (sound.get().value() == SoundEvents.BUNDLE_INSERT ||
                sound.get().value() == SoundEvents.BUNDLE_REMOVE_ONE)) {
            return EventResult.INTERRUPT;
        }

        return EventResult.PASS;
    }

    public static boolean precisionModeAllowedAndActive() {
        return ItemInteractions.CONFIG.get(ServerConfig.class).allowPrecisionMode &&
                ItemInteractions.CONFIG.get(ClientConfig.class).precisionMode.isActive();
    }

    private static void syncContainerClientInput(Player player) {
        int currentContainerSlot = ContainerSlotHelper.getCurrentContainerSlot(player);
        boolean extractSingleItem = precisionModeAllowedAndActive();
        // this is where the client sets this value, so it's important to call before click actions even when syncing isn't so important (applies mostly to creative menu)
        ContainerSlotHelper.extractSingleItem(player, extractSingleItem);
        ItemInteractions.NETWORK.sendMessage(new C2SContainerClientInputMessage(currentContainerSlot,
                extractSingleItem).toServerboundMessage());
    }
}
