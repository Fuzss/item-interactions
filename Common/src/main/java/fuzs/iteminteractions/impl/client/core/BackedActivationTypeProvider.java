package fuzs.iteminteractions.impl.client.core;

import fuzs.puzzleslib.api.client.key.v1.KeyMappingHelper;
import fuzs.puzzleslib.api.event.v1.core.EventResult;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.Nullable;

import java.util.Objects;

public interface BackedActivationTypeProvider extends ActivationTypeProvider {

    default @Nullable ActivationTypeProvider getBackingProvider() {
        return null;
    }

    default @Nullable KeyMapping getKeyMapping() {
        return null;
    }

    default boolean isActive() {
        ActivationTypeProvider backingProvider = this.getBackingProvider();
        Objects.requireNonNull(backingProvider, "backing provider is null");
        return backingProvider.isActive();
    }

    @Override
    default Component getNameComponent() {
        if (this.getKeyMapping() != null) {
            return this.getKeyMapping().getTranslatedKeyMessage();
        } else {
            ActivationTypeProvider backingProvider = this.getBackingProvider();
            Objects.requireNonNull(backingProvider, "backing provider is null");
            return backingProvider.getNameComponent();
        }
    }

    @Override
    default Component getActivationModeComponent() {
        return this.getKeyMapping() != null ? TOGGLE_COMPONENT : HOLD_COMPONENT;
    }

    default void toggleActive() {
        // NO-OP
    }

    default boolean keyPressed(KeyEvent keyEvent) {
        if (this.getKeyMapping() != null && KeyMappingHelper.isKeyActiveAndMatches(this.getKeyMapping(), keyEvent)) {
            this.toggleActive();
            return true;
        } else {
            return false;
        }
    }

    static EventResult onKeyPressed(BackedActivationTypeProvider[] activationTypeProviders, KeyEvent keyEvent) {
        for (BackedActivationTypeProvider activationTypeProvider : activationTypeProviders) {
            if (activationTypeProvider.keyPressed(keyEvent)) {
                return EventResult.INTERRUPT;
            }
        }

        return EventResult.PASS;
    }
}
