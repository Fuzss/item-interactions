package fuzs.iteminteractions.impl.config;

import fuzs.iteminteractions.impl.client.core.ActivationTypeProvider;
import fuzs.iteminteractions.impl.client.core.BackedActivationTypeProvider;
import fuzs.iteminteractions.impl.client.core.KeyBackedActivationType;
import fuzs.puzzleslib.api.event.v1.core.EventResult;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;

public enum ExtractSingleItem implements BackedActivationTypeProvider {
    SHIFT {
        @Override
        public ActivationTypeProvider getBackingProvider() {
            return KeyBackedActivationType.SHIFT;
        }
    },
    CONTROL {
        @Override
        public ActivationTypeProvider getBackingProvider() {
            return KeyBackedActivationType.CONTROL;
        }
    },
    ALT {
        @Override
        public ActivationTypeProvider getBackingProvider() {
            return KeyBackedActivationType.ALT;
        }
    };

    private static final ExtractSingleItem[] VALUES = values();

    public static EventResult onBeforeKeyPressed(AbstractContainerScreen<?> screen, int keyCode, int scanCode, int modifiers) {
        return BackedActivationTypeProvider.onKeyPressed(VALUES, keyCode, scanCode);
    }
}
