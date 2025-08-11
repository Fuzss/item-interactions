package fuzs.iteminteractions.impl.config;

import fuzs.iteminteractions.impl.ItemInteractions;
import fuzs.iteminteractions.impl.client.core.ActivationTypeProvider;
import fuzs.iteminteractions.impl.client.core.BackedActivationTypeProvider;
import fuzs.iteminteractions.impl.client.core.KeyBackedActivationType;
import fuzs.puzzleslib.api.client.key.v1.KeyMappingHelper;
import fuzs.puzzleslib.api.event.v1.core.EventResult;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;

public enum VisualItemContents implements BackedActivationTypeProvider {
    KEY {
        private boolean isActive;

        @Override
        public boolean isActive() {
            return this.isActive;
        }

        @Override
        public void toggleActive() {
            this.isActive = !this.isActive;
        }

        @Override
        public KeyMapping getKeyMapping() {
            return KEY_MAPPING;
        }
    },
    ALWAYS {
        @Override
        public ActivationTypeProvider getBackingProvider() {
            return KeyBackedActivationType.ALWAYS;
        }
    },
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

    private static final VisualItemContents[] VALUES = values();
    public static final KeyMapping KEY_MAPPING = KeyMappingHelper.registerUnboundKeyMapping(ItemInteractions.id(
            "toggle_visual_item_contents"));

    public static EventResult onBeforeKeyPressed(AbstractContainerScreen<?> screen, int keyCode, int scanCode, int modifiers) {
        return BackedActivationTypeProvider.onKeyPressed(VALUES, keyCode, scanCode);
    }
}
