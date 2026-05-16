package fuzs.iteminteractions.common.impl.client.handler;

import fuzs.iteminteractions.common.api.v1.world.item.storage.ItemStorageHolder;
import fuzs.iteminteractions.common.impl.ItemInteractions;
import fuzs.iteminteractions.common.impl.client.gui.CustomItemSlotMouseAction;
import fuzs.iteminteractions.common.impl.config.ClientConfig;
import fuzs.iteminteractions.common.impl.init.ModRegistry;
import fuzs.iteminteractions.common.impl.network.client.ServerboundContainerClientInputMessage;
import fuzs.puzzleslib.common.api.event.v1.core.EventResult;
import fuzs.puzzleslib.common.api.event.v1.data.MutableFloat;
import fuzs.puzzleslib.common.api.event.v1.data.MutableValue;
import fuzs.puzzleslib.common.api.network.v4.MessageSender;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.ItemSlotMouseAction;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.Set;

public class ClientEventHandler {
    private static final Set<SoundEvent> BUNDLE_SOUNDS = Set.of(SoundEvents.BUNDLE_INSERT,
            SoundEvents.BUNDLE_INSERT_FAIL,
            SoundEvents.BUNDLE_REMOVE_ONE);

    private static boolean lastSentSingleItemOnly;

    /**
     * Shows the item tooltip for the item held by the cursor; to be used with the single item moving feature to be able
     * to continuously see what's going on.
     *
     * @see AbstractContainerScreen#extractTooltip(GuiGraphicsExtractor, int, int)
     */
    public static void onAfterBackground(AbstractContainerScreen<?> screen, GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        if (!ItemInteractions.CONFIG.get(ClientConfig.class).itemHeldByCursorTooltip.isUsed()) {
            return;
        }

        ItemStack itemHeldByCursor = screen.getMenu().getCarried();
        ItemStorageHolder holder = ItemStorageHolder.ofItem(itemHeldByCursor);
        if (holder.isPresentFor(itemHeldByCursor, screen.minecraft.player) && holder.storage()
                .hasContents(itemHeldByCursor)) {
            guiGraphics.setTooltipForNextFrame(screen.getFont(),
                    screen.getTooltipFromContainerItem(itemHeldByCursor),
                    itemHeldByCursor.getTooltipImage(),
                    mouseX,
                    mouseY,
                    itemHeldByCursor.get(DataComponents.TOOLTIP_STYLE));
        }
    }

    public static EventResult onPlaySoundAtEntity(Level level, Entity entity, MutableValue<Holder<SoundEvent>> soundEvent, MutableValue<SoundSource> soundSource, MutableFloat soundVolume, MutableFloat soundPitch) {
        // Prevent the bundle sounds from being spammed when dragging.
        // Not a nice solution, but it works.
        if (isDragging() && soundSource.get() == SoundSource.PLAYERS && BUNDLE_SOUNDS.contains(soundEvent.get()
                .value())) {
            return EventResult.INTERRUPT;
        } else {
            return EventResult.PASS;
        }
    }

    private static boolean isDragging() {
        if (Minecraft.getInstance().screen instanceof AbstractContainerScreen<?> screen) {
            for (ItemSlotMouseAction itemMouseAction : screen.itemSlotMouseActions) {
                if (itemMouseAction instanceof CustomItemSlotMouseAction customMouseAction
                        && customMouseAction.isDragging()) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * This must be sent before any slot click action is performed server side. For vanilla this can be caused by either
     * mouse clicks (normal menu interactions) or key presses (hotbar keys for swapping items to those slots).
     * <p>
     * All screens sending the normal click packet are handled via
     * {@link MultiPlayerGameMode#handleInventoryButtonClick(int, int)}, only the creative screen needs additional
     * handling which happens in {@link AbstractContainerScreen#onMouseClickAction(Slot, ContainerInput)}.
     * <p>
     * While the latter option works for all screens, we keep both to be extra safe, especially with other mods.
     *
     * @see MultiPlayerGameMode#ensureHasSentCarriedItem()
     */
    public static void ensureHasSentContainerClientInput(Player player) {
        boolean singleItemOnly = ItemInteractions.CONFIG.get(ClientConfig.class).extractSingleItemOnly();
        if (singleItemOnly != lastSentSingleItemOnly) {
            lastSentSingleItemOnly = singleItemOnly;
            ModRegistry.MOVE_SINGLE_ITEM_ATTACHMENT_TYPE.set(player, singleItemOnly ? Unit.INSTANCE : null);
            MessageSender.broadcast(new ServerboundContainerClientInputMessage(singleItemOnly));
        }
    }
}
