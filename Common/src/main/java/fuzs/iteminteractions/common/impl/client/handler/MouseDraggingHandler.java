package fuzs.iteminteractions.common.impl.client.handler;

import com.mojang.blaze3d.platform.InputConstants;
import fuzs.iteminteractions.common.api.v1.world.item.storage.ItemStorageHolder;
import fuzs.iteminteractions.common.impl.ItemInteractions;
import fuzs.iteminteractions.common.impl.config.ClientConfig;
import fuzs.iteminteractions.common.impl.config.ServerConfig;
import fuzs.puzzleslib.common.api.event.v1.core.EventResult;
import fuzs.puzzleslib.common.api.event.v1.data.MutableFloat;
import fuzs.puzzleslib.common.api.event.v1.data.MutableValue;
import net.minecraft.client.gui.GuiGraphicsExtractor;
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
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;

public class MouseDraggingHandler {
    private static final Set<Slot> CONTAINER_DRAG_SLOTS = new HashSet<>();
    @Nullable
    private static ContainerDragType containerDragType;

    public static EventResult onBeforeMousePressed(AbstractContainerScreen<?> screen, MouseButtonEvent mouseButtonEvent) {
        if (!ItemInteractions.CONFIG.get(ServerConfig.class).enableMouseDragging) {
            return EventResult.PASS;
        }

        ItemStack carriedStack = screen.getMenu().getCarried();
        if (validMouseButton(mouseButtonEvent)) {
            if (ItemStorageHolder.ofItem(carriedStack).isPresentFor(carriedStack, screen.minecraft.player)) {
                Slot slot = screen.getHoveredSlot(mouseButtonEvent.x(), mouseButtonEvent.y());
                if (slot != null) {
                    if (slot.hasItem() && !ItemInteractions.CONFIG.get(ClientConfig.class).extractSingleItemOnly()) {
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
        if (!ItemInteractions.CONFIG.get(ServerConfig.class).enableMouseDragging) {
            return EventResult.PASS;
        }

        if (containerDragType != null) {
            AbstractContainerMenu menu = screen.getMenu();
            ItemStack carriedStack = menu.getCarried();
            ItemStorageHolder behavior = ItemStorageHolder.ofItem(carriedStack);
            if (!validMouseButton(mouseButtonEvent) || !behavior.isPresentFor(carriedStack,
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
                                    && !behavior.getItemContainer(carriedStack, screen.minecraft.player).isEmpty();
                    if (normalInteraction || slot.hasItem() && ItemInteractions.CONFIG.get(ClientConfig.class)
                            .extractSingleItemOnly()) {
                        interact = true;
                    }
                }

                if (interact) {
                    screen.slotClicked(slot, slot.index, mouseButtonEvent.button(), ContainerInput.PICKUP);
                    CONTAINER_DRAG_SLOTS.add(slot);
                    return EventResult.INTERRUPT;
                }
            }
        }

        return EventResult.PASS;
    }

    public static EventResult onBeforeMouseRelease(AbstractContainerScreen<?> screen, MouseButtonEvent mouseButtonEvent) {
        if (!ItemInteractions.CONFIG.get(ServerConfig.class).enableMouseDragging) {
            return EventResult.PASS;
        }

        if (containerDragType != null) {
            if (validMouseButton(mouseButtonEvent) && !CONTAINER_DRAG_SLOTS.isEmpty()) {
                // play this manually at the end; we suppress all interaction sounds played while dragging
                SimpleSoundInstance sound = SimpleSoundInstance.forUI(containerDragType.sound,
                        0.8F,
                        0.8F + SoundInstance.createUnseededRandom().nextFloat() * 0.4F);
                screen.minecraft.getSoundManager().play(sound);
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
            return ItemInteractions.CONFIG.get(ClientConfig.class).extractSingleItemOnly();
        } else {
            return mouseButtonEvent.button() == InputConstants.MOUSE_BUTTON_RIGHT;
        }
    }

    public static void onAfterBackground(AbstractContainerScreen<?> screen, GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        if (CONTAINER_DRAG_SLOTS.isEmpty()) {
            return;
        }

        guiGraphics.pose().pushMatrix();
        guiGraphics.pose().translate(screen.leftPos, screen.topPos);
        renderDragSlotsHighlight(screen,
                guiGraphics,
                mouseX,
                mouseY,
                AbstractContainerScreen.SLOT_HIGHLIGHT_BACK_SPRITE);
        guiGraphics.pose().popMatrix();
    }

    public static void onRenderContainerScreenContents(AbstractContainerScreen<?> screen, GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY) {
        if (CONTAINER_DRAG_SLOTS.isEmpty()) {
            return;
        }

        renderDragSlotsHighlight(screen,
                guiGraphics,
                mouseX,
                mouseY,
                AbstractContainerScreen.SLOT_HIGHLIGHT_FRONT_SPRITE);
    }

    private static void renderDragSlotsHighlight(AbstractContainerScreen<?> screen, GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, Identifier slotHighlightSprite) {
        for (Slot slot : screen.getMenu().slots) {
            if (slot.isHighlightable() && CONTAINER_DRAG_SLOTS.contains(slot)) {
                // slots will sometimes be added to dragged slots when simply clicking on a slot, so don't render our overlay then
                if (CONTAINER_DRAG_SLOTS.size() > 1 || !screen.isHovering(slot, mouseX, mouseY)) {
                    guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED,
                            slotHighlightSprite,
                            slot.x - 4,
                            slot.y - 4,
                            24,
                            24);
                }
            }
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
        INSERT(InputConstants.MOUSE_BUTTON_LEFT, SoundEvents.BUNDLE_INSERT),
        REMOVE(InputConstants.MOUSE_BUTTON_RIGHT, SoundEvents.BUNDLE_REMOVE_ONE);

        public final int buttonNum;
        public final SoundEvent sound;

        ContainerDragType(int buttonNum, SoundEvent sound) {
            this.buttonNum = buttonNum;
            this.sound = sound;
        }
    }
}
