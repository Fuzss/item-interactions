package fuzs.iteminteractions.impl.client.handler;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.datafixers.util.Pair;
import fuzs.iteminteractions.api.v1.provider.ItemContentsBehavior;
import fuzs.iteminteractions.impl.ItemInteractions;
import fuzs.iteminteractions.impl.config.ClientConfig;
import fuzs.iteminteractions.impl.config.ServerConfig;
import fuzs.iteminteractions.impl.network.client.ServerboundContainerClientInputMessage;
import fuzs.iteminteractions.impl.world.inventory.ContainerSlotHelper;
import fuzs.iteminteractions.impl.world.item.container.ItemContentsProviders;
import fuzs.puzzleslib.api.client.gui.v2.tooltip.TooltipRenderHelper;
import fuzs.puzzleslib.api.event.v1.core.EventResult;
import fuzs.puzzleslib.api.event.v1.data.MutableFloat;
import fuzs.puzzleslib.api.event.v1.data.MutableValue;
import fuzs.puzzleslib.api.network.v4.MessageSender;
import net.minecraft.client.Minecraft;
import net.minecraft.client.ScrollWheelHandler;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.DefaultTooltipPositioner;
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
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2i;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;

public class ClientInputActionHandler {
    private static ScrollWheelHandler scrollWheelHandler = new ScrollWheelHandler();
    private static int lastSentContainerSlot = -1;
    private static boolean lastSentExtractSingleItem;

    public static EventResult onBeforeKeyPressed(AbstractContainerScreen<?> screen, int keyCode, int scanCode, int modifiers) {
        // this must be sent before any slot click action is performed server side, by vanilla this can be caused by either mouse clicks (normal menu interactions)
        // or key presses (hotbar keys for swapping items to those slots)
        // this is already added via mixin to where vanilla sends the click packet, but creative screen doesn't use it, and you never know with other mods...
        ensureHasSentContainerClientInput(screen, screen.minecraft.player, false);
        return EventResult.PASS;
    }

    public static EventResult onBeforeMousePressed(AbstractContainerScreen<?> screen, double mouseX, double mouseY, int button) {
        // this must be sent before any slot click action is performed server side, by vanilla this can be caused by either mouse clicks (normal menu interactions)
        // or key presses (hotbar keys for swapping items to those slots)
        // this is already added via mixin to where vanilla sends the click packet, but creative screen doesn't use it, and you never know with other mods...
        ensureHasSentContainerClientInput(screen, screen.minecraft.player, false);
        return EventResult.PASS;
    }

    public static EventResult onBeforeMouseRelease(AbstractContainerScreen<?> screen, double mouseX, double mouseY, int button) {
        // prevent vanilla double click feature from interfering with our precision mode, adding an unnecessary delay when quickly inserting items via left-click
        // it wouldn't work anyway, and right-click is fine, leading to inconsistent behavior
        if (precisionModeAllowedAndActive() && !getContainerItemStack(screen, false).isEmpty()) {
            screen.doubleclick = false;
        }

        return EventResult.PASS;
    }

    public static void onAfterRender(AbstractContainerScreen<?> screen, GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        // renders vanilla item tooltips when a stack is carried and the cursor hovers over a container item
        // intended to be used with single item extraction/insertion feature to be able to continuously see what's going on in the container item
        if (!ItemInteractions.CONFIG.get(ClientConfig.class).carriedItemTooltips.isActive()) return;
        if (!screen.getMenu().getCarried().isEmpty()) {
            ItemStack itemStack = getContainerItemStack(screen, false);
            if (!itemStack.isEmpty()) {
                List<ClientTooltipComponent> tooltipComponents = TooltipRenderHelper.getTooltip(itemStack);
                guiGraphics.renderTooltip(screen.getFont(),
                        tooltipComponents,
                        mouseX,
                        mouseY,
                        DefaultTooltipPositioner.INSTANCE,
                        null);
            }
        }
    }

    public static void onAfterInit(Minecraft minecraft, AbstractContainerScreen<?> screen, int screenWidth, int screenHeight, List<AbstractWidget> widgets, UnaryOperator<AbstractWidget> addWidget, Consumer<AbstractWidget> removeWidget) {
        // no way to reset internal values other than to create a new instance
        scrollWheelHandler = new ScrollWheelHandler();
    }

    public static EventResult onBeforeMouseScroll(AbstractContainerScreen<?> screen, double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        // allows scrolling between filled slots on a container items tooltip to select the slot to be interacted with next
        if (horizontalAmount == 0.0 && verticalAmount == 0.0) return EventResult.PASS;
        if (!ItemInteractions.CONFIG.get(ClientConfig.class).visualItemContents.isActive()) return EventResult.PASS;
        if (precisionModeAllowedAndActive()) {
            Slot hoveredSlot = screen.hoveredSlot;
            if (hoveredSlot != null) {
                if (!ItemContentsProviders.get(screen.getMenu().getCarried()).isEmpty() || !ItemContentsProviders.get(
                        hoveredSlot.getItem()).isEmpty()) {
                    Vector2i vector2i = scrollWheelHandler.onMouseScroll(horizontalAmount, verticalAmount);
                    int scrollAmount = vector2i.y == 0 ? -vector2i.x : vector2i.y;
                    if (scrollAmount != 0) {
                        int mouseButton =
                                (ItemInteractions.CONFIG.get(ClientConfig.class).invertPrecisionModeScrolling ?
                                        scrollAmount < 0 : scrollAmount > 0) ? InputConstants.MOUSE_BUTTON_RIGHT :
                                        InputConstants.MOUSE_BUTTON_LEFT;
                        ensureHasSentContainerClientInput(screen, screen.minecraft.player, true);
                        screen.slotClicked(hoveredSlot, hoveredSlot.index, mouseButton, ClickType.PICKUP);
                    }
                    return EventResult.INTERRUPT;
                }
            }
        } else if (ItemInteractions.CONFIG.get(ServerConfig.class).allowSlotCycling) {
            ItemStack carriedStack = screen.getMenu().getCarried();
            if (!carriedStack.isEmpty()
                    && !ItemInteractions.CONFIG.get(ClientConfig.class).carriedItemTooltips.isActive()) {
                return EventResult.PASS;
            }
            Pair<ItemStack, ItemContentsBehavior> pair = getContainerPair(screen, true);
            ItemStack itemStack = pair.getFirst();
            if (!itemStack.isEmpty()) {
                Vector2i vector2i = scrollWheelHandler.onMouseScroll(horizontalAmount, verticalAmount);
                int scrollAmount = vector2i.y == 0 ? -vector2i.x : vector2i.y;
                if (scrollAmount != 0) {
                    int oldContainerSlot = ContainerSlotHelper.getCurrentContainerSlot(screen.minecraft.player);
                    SimpleContainer container = ItemContentsProviders.get(itemStack)
                            .getItemContainerView(itemStack, screen.minecraft.player);
                    int newContainerSlot = ContainerSlotHelper.findClosestSlotWithContent(container,
                            oldContainerSlot,
                            scrollAmount < 0.0);
                    ContainerSlotHelper.setCurrentContainerSlot(screen.minecraft.player, newContainerSlot);
                    if (oldContainerSlot != -1) {
                        pair.getSecond().provider().onToggleSelectedItem(itemStack, oldContainerSlot, newContainerSlot);
                    }
                    ensureHasSentContainerClientInput(screen, screen.minecraft.player, true);
                }
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

    public static EventResult onPlaySoundAtEntity(Level level, Entity entity, MutableValue<Holder<SoundEvent>> soundEvent, MutableValue<SoundSource> soundSource, MutableFloat soundVolume, MutableFloat soundPitch) {
        if (!ItemInteractions.CONFIG.get(ClientConfig.class).disableInteractionSounds) return EventResult.PASS;
        if (soundSource.get() == SoundSource.PLAYERS && (soundEvent.get().value() == SoundEvents.BUNDLE_INSERT
                || soundEvent.get().value() == SoundEvents.BUNDLE_REMOVE_ONE)) {
            return EventResult.INTERRUPT;
        } else {
            return EventResult.PASS;
        }
    }

    public static boolean precisionModeAllowedAndActive() {
        return ItemInteractions.CONFIG.get(ServerConfig.class).allowPrecisionMode && ItemInteractions.CONFIG.get(
                ClientConfig.class).extractSingleItem.isActive();
    }

    public static void ensureHasSentContainerClientInput(Screen screen, Player player) {
        ensureHasSentContainerClientInput(screen, player, false);
    }

    private static void ensureHasSentContainerClientInput(@Nullable Screen screen, Player player, boolean alwaysSendMessage) {
        if (!(screen instanceof AbstractContainerScreen<?>)) return;
        int currentContainerSlot = ContainerSlotHelper.getCurrentContainerSlot(player);
        boolean extractSingleItem = precisionModeAllowedAndActive();
        if (alwaysSendMessage || currentContainerSlot != lastSentContainerSlot
                || extractSingleItem != lastSentExtractSingleItem) {
            lastSentContainerSlot = currentContainerSlot;
            lastSentExtractSingleItem = extractSingleItem;
            // this is where the client sets this value,
            // so it's important to call before click actions even when syncing isn't so important
            // (applies mostly to creative menu)
            ContainerSlotHelper.extractSingleItem(player, extractSingleItem);
            MessageSender.broadcast(new ServerboundContainerClientInputMessage(currentContainerSlot,
                    extractSingleItem));
        }
    }
}
