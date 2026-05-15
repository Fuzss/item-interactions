package fuzs.iteminteractions.common.impl.client.handler;

import fuzs.iteminteractions.common.api.v1.world.item.storage.ItemStorageHolder;
import fuzs.iteminteractions.common.impl.ItemInteractions;
import fuzs.iteminteractions.common.impl.config.ClientConfig;
import fuzs.iteminteractions.common.impl.config.ServerConfig;
import fuzs.puzzleslib.common.api.event.v1.core.EventResult;
import fuzs.puzzleslib.common.api.event.v1.data.MutableFloat;
import fuzs.puzzleslib.common.api.event.v1.data.MutableValue;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;

public class MouseDraggingHandler {
    private static final Set<Slot> CLICKED_CONTAINER_DRAG_SLOTS = new HashSet<>();
    private static final Set<Slot> ALL_CONTAINER_DRAG_SLOTS = new HashSet<>();
    @Nullable
    private static ContainerDragType containerDragType;

    public static EventResult onBeforeMousePressed(AbstractContainerScreen<?> screen, MouseButtonEvent event) {
        if (!ItemInteractions.CONFIG.get(ServerConfig.class).enableMouseDragging) {
            return EventResult.PASS;
        }

        ItemStack itemHeldByCursor = screen.getMenu().getCarried();
        if (ItemStorageHolder.ofItem(itemHeldByCursor).isPresentFor(itemHeldByCursor, screen.minecraft.player)) {
            Slot slot = screen.getHoveredSlot(event.x(), event.y());
            if (slot != null) {
                if (event.button() == 0) {
                    containerDragType = ContainerDragType.INSERT;
                } else {
                    containerDragType = ContainerDragType.REMOVE;
                }

                CLICKED_CONTAINER_DRAG_SLOTS.clear();
                ALL_CONTAINER_DRAG_SLOTS.clear();
                return EventResult.INTERRUPT;
            }
        }

        containerDragType = null;
        return EventResult.PASS;
    }

    public static EventResult onBeforeMouseDragged(AbstractContainerScreen<?> screen, MouseButtonEvent event, double dragX, double dragY) {
        if (!ItemInteractions.CONFIG.get(ServerConfig.class).enableMouseDragging) {
            return EventResult.PASS;
        }

        if (containerDragType != null) {
            ItemStack itemHeldByCursor = screen.getMenu().getCarried();
            ItemStorageHolder holder = ItemStorageHolder.ofItem(itemHeldByCursor);
            if (!holder.isPresentFor(itemHeldByCursor, screen.minecraft.player)) {
                containerDragType = null;
                CLICKED_CONTAINER_DRAG_SLOTS.clear();
                ALL_CONTAINER_DRAG_SLOTS.clear();
                return EventResult.PASS;
            }

            Slot slot = screen.getHoveredSlot(event.x(), event.y());
            if (slot != null && screen.getMenu().canDragTo(slot) && !ALL_CONTAINER_DRAG_SLOTS.contains(slot)) {
                boolean interact = false;
                if (containerDragType == ContainerDragType.INSERT) {
                    if (slot.hasItem() && holder.canAddItem(itemHeldByCursor,
                            slot.getItem(),
                            screen.minecraft.player)) {
                        interact = true;
                    }
                } else if (containerDragType == ContainerDragType.REMOVE) {
                    if ((!slot.hasItem() || ItemInteractions.CONFIG.get(ClientConfig.class).extractSingleItemOnly())
                            && !holder.getItemContainer(itemHeldByCursor, screen.minecraft.player).isEmpty()) {
                        interact = true;
                    }
                }

                if (interact) {
                    screen.slotClicked(slot, slot.index, event.button(), ContainerInput.PICKUP);
                    CLICKED_CONTAINER_DRAG_SLOTS.add(slot);
                }

                ALL_CONTAINER_DRAG_SLOTS.add(slot);
                return EventResult.INTERRUPT;
            }
        }

        return EventResult.PASS;
    }

    public static EventResult onBeforeMouseRelease(AbstractContainerScreen<?> screen, MouseButtonEvent event) {
        if (!ItemInteractions.CONFIG.get(ServerConfig.class).enableMouseDragging) {
            return EventResult.PASS;
        }

        if (containerDragType != null) {
            if (!CLICKED_CONTAINER_DRAG_SLOTS.isEmpty()) {
                // Play this manually at the end as we suppress all interaction sounds played while dragging.
                containerDragType.playSound(screen.minecraft);
            }

            containerDragType = null;
            boolean interrupt = ALL_CONTAINER_DRAG_SLOTS.size() > 1 || !CLICKED_CONTAINER_DRAG_SLOTS.isEmpty();
            CLICKED_CONTAINER_DRAG_SLOTS.clear();
            ALL_CONTAINER_DRAG_SLOTS.clear();
            if (interrupt) {
                return EventResult.INTERRUPT;
            }
        }

        containerDragType = null;
        CLICKED_CONTAINER_DRAG_SLOTS.clear();
        ALL_CONTAINER_DRAG_SLOTS.clear();
        return EventResult.PASS;
    }

    public static void onAfterBackground(AbstractContainerScreen<?> screen, GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        if (!CLICKED_CONTAINER_DRAG_SLOTS.isEmpty()) {
            guiGraphics.pose().pushMatrix();
            guiGraphics.pose().translate(screen.leftPos, screen.topPos);
            extractSlotHighlights(screen,
                    guiGraphics,
                    mouseX,
                    mouseY,
                    AbstractContainerScreen.SLOT_HIGHLIGHT_BACK_SPRITE);
            guiGraphics.pose().popMatrix();
        }
    }

    public static void onRenderContainerScreenContents(AbstractContainerScreen<?> screen, GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY) {
        if (!CLICKED_CONTAINER_DRAG_SLOTS.isEmpty()) {
            extractSlotHighlights(screen,
                    guiGraphics,
                    mouseX,
                    mouseY,
                    AbstractContainerScreen.SLOT_HIGHLIGHT_FRONT_SPRITE);
        }
    }

    private static void extractSlotHighlights(AbstractContainerScreen<?> screen, GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, Identifier slotHighlightSprite) {
        for (Slot slot : screen.getMenu().slots) {
            if (slot.isHighlightable() && CLICKED_CONTAINER_DRAG_SLOTS.contains(slot)) {
                // slots will sometimes be added to dragged slots when simply clicking on a slot, so don't render our overlay then
                if (CLICKED_CONTAINER_DRAG_SLOTS.size() > 1 || !screen.isHovering(slot, mouseX, mouseY)) {
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
        // Prevent the bundle sounds from being spammed when dragging.
        // Not a nice solution, but it works.
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

        /**
         * @see net.minecraft.world.item.BundleItem#playInsertSound(Entity)
         * @see net.minecraft.world.item.BundleItem#playRemoveOneSound(Entity)
         */
        public void playSound(Minecraft minecraft) {
            if (minecraft.level != null) {
                minecraft.getSoundManager()
                        .play(SimpleSoundInstance.forUI(this.sound,
                                0.8F,
                                0.8F + minecraft.level.getRandom().nextFloat() * 0.4F));
            }
        }
    }
}
