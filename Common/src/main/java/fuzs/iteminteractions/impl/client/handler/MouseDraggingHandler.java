package fuzs.iteminteractions.impl.client.handler;

import com.google.common.collect.Sets;
import com.mojang.blaze3d.platform.InputConstants;
import fuzs.iteminteractions.api.v1.provider.ItemContentsBehavior;
import fuzs.iteminteractions.impl.ItemInteractions;
import fuzs.iteminteractions.impl.config.ClientConfig;
import fuzs.iteminteractions.impl.config.ServerConfig;
import fuzs.iteminteractions.impl.world.item.container.ItemContentsProviders;
import fuzs.puzzleslib.api.event.v1.core.EventResult;
import fuzs.puzzleslib.api.event.v1.data.MutableFloat;
import fuzs.puzzleslib.api.event.v1.data.MutableValue;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

import java.util.Set;

public class MouseDraggingHandler {
    /**
     * @see AbstractContainerScreen#SLOT_HIGHLIGHT_BACK_SPRITE
     */
    private static final Identifier SLOT_HIGHLIGHT_BACK_SPRITE = Identifier.withDefaultNamespace(
            "container/slot_highlight_back");
    /**
     * @see AbstractContainerScreen#SLOT_HIGHLIGHT_FRONT_SPRITE
     */
    private static final Identifier SLOT_HIGHLIGHT_FRONT_SPRITE = Identifier.withDefaultNamespace(
            "container/slot_highlight_front");
    private static final Set<Slot> CONTAINER_DRAG_SLOTS = Sets.newHashSet();
    @Nullable private static ContainerDragType containerDragType;

    public static EventResult onBeforeMousePressed(AbstractContainerScreen<?> screen, MouseButtonEvent mouseButtonEvent) {
        if (!ItemInteractions.CONFIG.get(ServerConfig.class).allowMouseDragging) return EventResult.PASS;
        ItemStack carriedStack = screen.getMenu().getCarried();
        if (validMouseButton(mouseButtonEvent)) {
            if (ItemContentsProviders.get(carriedStack)
                    .allowsPlayerInteractions(carriedStack, screen.minecraft.player)) {
                Slot slot = screen.getHoveredSlot(mouseButtonEvent.x(), mouseButtonEvent.y());
                if (slot != null) {
                    if (slot.hasItem() && !ClientInputActionHandler.precisionModeAllowedAndActive()) {
                        containerDragType = ContainerDragType.INSERT;
                    } else {
                        containerDragType = ContainerDragType.REMOVE;
                    }
                    CONTAINER_DRAG_SLOTS.clear();
                    return EventResult.INTERRUPT;
                }
            }
        }
        containerDragType = null;
        return EventResult.PASS;
    }

    public static EventResult onBeforeMouseDragged(AbstractContainerScreen<?> screen, MouseButtonEvent mouseButtonEvent, double dragX, double dragY) {
        if (!ItemInteractions.CONFIG.get(ServerConfig.class).allowMouseDragging) return EventResult.PASS;
        if (containerDragType != null) {
            AbstractContainerMenu menu = screen.getMenu();
            ItemStack carriedStack = menu.getCarried();
            ItemContentsBehavior behavior = ItemContentsProviders.get(carriedStack);
            if (!validMouseButton(mouseButtonEvent) || !behavior.allowsPlayerInteractions(carriedStack,
                    screen.minecraft.player)) {
                containerDragType = null;
                CONTAINER_DRAG_SLOTS.clear();
                return EventResult.PASS;
            }
            Slot slot = screen.getHoveredSlot(mouseButtonEvent.x(), mouseButtonEvent.y());
            if (slot != null && menu.canDragTo(slot) && !CONTAINER_DRAG_SLOTS.contains(slot)) {
                boolean interact = false;
                if (containerDragType == ContainerDragType.INSERT && slot.hasItem() && behavior.canAddItem(carriedStack,
                        slot.getItem(),
                        screen.minecraft.player)) {
                    interact = true;
                } else if (containerDragType == ContainerDragType.REMOVE) {
                    boolean normalInteraction =
                            mouseButtonEvent.button() == InputConstants.MOUSE_BUTTON_RIGHT && !slot.hasItem()
                                    && !behavior.getItemContainerView(carriedStack, screen.minecraft.player).isEmpty();
                    if (normalInteraction
                            || slot.hasItem() && ClientInputActionHandler.precisionModeAllowedAndActive()) {
                        interact = true;
                    }
                }
                if (interact) {
                    screen.slotClicked(slot, slot.index, mouseButtonEvent.button(), ClickType.PICKUP);
                    CONTAINER_DRAG_SLOTS.add(slot);
                    return EventResult.INTERRUPT;
                }
            }
        }
        return EventResult.PASS;
    }

    public static EventResult onBeforeMouseRelease(AbstractContainerScreen<?> screen, MouseButtonEvent mouseButtonEvent) {
        if (!ItemInteractions.CONFIG.get(ServerConfig.class).allowMouseDragging) return EventResult.PASS;
        if (containerDragType != null) {
            if (validMouseButton(mouseButtonEvent) && !CONTAINER_DRAG_SLOTS.isEmpty()) {
                if (!ItemInteractions.CONFIG.get(ClientConfig.class).disableInteractionSounds) {
                    // play this manually at the end; we suppress all interaction sounds played while dragging
                    SimpleSoundInstance sound = SimpleSoundInstance.forUI(containerDragType.sound,
                            0.8F,
                            0.8F + SoundInstance.createUnseededRandom().nextFloat() * 0.4F);
                    screen.minecraft.getSoundManager().play(sound);
                }
                containerDragType = null;
                CONTAINER_DRAG_SLOTS.clear();
                return EventResult.INTERRUPT;
            }
            containerDragType = null;
        }
        CONTAINER_DRAG_SLOTS.clear();
        return EventResult.PASS;
    }

    private static boolean validMouseButton(MouseButtonEvent mouseButtonEvent) {
        if (mouseButtonEvent.button() == InputConstants.MOUSE_BUTTON_LEFT) {
            return ClientInputActionHandler.precisionModeAllowedAndActive();
        } else {
            return mouseButtonEvent.button() == InputConstants.MOUSE_BUTTON_RIGHT;
        }
    }

    public static void onAfterBackground(AbstractContainerScreen<?> screen, GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderDragSlotsHighlight(screen, guiGraphics, mouseX, mouseY, SLOT_HIGHLIGHT_BACK_SPRITE, true);
    }

    public static void onRenderContainerScreenContents(AbstractContainerScreen<?> screen, GuiGraphics guiGraphics, int mouseX, int mouseY) {
        renderDragSlotsHighlight(screen, guiGraphics, mouseX, mouseY, SLOT_HIGHLIGHT_FRONT_SPRITE, false);
    }

    private static void renderDragSlotsHighlight(AbstractContainerScreen<?> screen, GuiGraphics guiGraphics, int mouseX, int mouseY, Identifier identifier, boolean applyTranslation) {
        if (CONTAINER_DRAG_SLOTS.isEmpty()) {
            return;
        }

        if (applyTranslation) {
            guiGraphics.pose().pushMatrix();
            guiGraphics.pose().translate(screen.leftPos, screen.topPos);
        }

        for (Slot slot : screen.getMenu().slots) {
            if (slot.isHighlightable() && CONTAINER_DRAG_SLOTS.contains(slot)) {
                // slots will sometimes be added to dragged slots when simply clicking on a slot, so don't render our overlay then
                if (CONTAINER_DRAG_SLOTS.size() > 1 || !screen.isHovering(slot, mouseX, mouseY)) {
                    guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED,
                            identifier,
                            slot.x - 4,
                            slot.y - 4,
                            24,
                            24);
                }
            }
        }

        if (applyTranslation) {
            guiGraphics.pose().popMatrix();
        }
    }

    public static EventResult onPlaySoundAtEntity(Level level, Entity entity, MutableValue<Holder<SoundEvent>> soundEvent, MutableValue<SoundSource> soundSource, MutableFloat soundVolume, MutableFloat soundPitch) {
        // prevent the bundle sounds from being spammed when dragging, not a nice solution, but it works
        if (containerDragType != null && soundSource.get() == SoundSource.PLAYERS
                && soundEvent.get().value() == containerDragType.sound) {
            return EventResult.INTERRUPT;
        } else {
            return EventResult.PASS;
        }
    }

    private enum ContainerDragType {
        INSERT(SoundEvents.BUNDLE_INSERT),
        REMOVE(SoundEvents.BUNDLE_REMOVE_ONE);

        public final SoundEvent sound;

        ContainerDragType(SoundEvent sound) {
            this.sound = sound;
        }
    }
}
