package fuzs.iteminteractions.common.impl.client.handler;

import fuzs.iteminteractions.common.impl.ItemInteractions;
import fuzs.iteminteractions.common.impl.config.ClientConfig;
import fuzs.puzzleslib.common.api.event.v1.core.EventResult;
import fuzs.puzzleslib.common.api.event.v1.data.MutableFloat;
import fuzs.puzzleslib.common.api.event.v1.data.MutableValue;
import net.minecraft.core.Holder;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

public class InteractionSoundsHandler {

    public static EventResult onPlaySoundAtEntity(Level level, Entity entity, MutableValue<Holder<SoundEvent>> soundEvent, MutableValue<SoundSource> soundSource, MutableFloat soundVolume, MutableFloat soundPitch) {
        if (!ItemInteractions.CONFIG.get(ClientConfig.class).disableInteractionSounds) {
            return EventResult.PASS;
        }

        if (soundSource.get() == SoundSource.PLAYERS && (soundEvent.get().value() == SoundEvents.BUNDLE_INSERT
                || soundEvent.get().value() == SoundEvents.BUNDLE_REMOVE_ONE)) {
            return EventResult.INTERRUPT;
        } else {
            return EventResult.PASS;
        }
    }
}
