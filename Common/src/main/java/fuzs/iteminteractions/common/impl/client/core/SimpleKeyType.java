package fuzs.iteminteractions.common.impl.client.core;

import fuzs.puzzleslib.common.api.util.v1.CommonHelper;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.Nullable;

public enum SimpleKeyType implements KeyType {
    ALWAYS {
        @Override
        public boolean isUsed() {
            return true;
        }

        @Override
        public Component getNameComponent() {
            throw new UnsupportedOperationException();
        }
    },
    NEVER {
        @Override
        public boolean isUsed() {
            return false;
        }

        @Override
        public @Nullable Component getComponent(String translationId) {
            return null;
        }

        @Override
        public Component getNameComponent() {
            throw new UnsupportedOperationException();
        }
    },
    SHIFT {
        @Override
        public boolean isUsed() {
            return CommonHelper.hasShiftDown();
        }

        @Override
        public Component getNameComponent() {
            return SHIFT_COMPONENT;
        }
    },
    CONTROL {
        @Override
        public boolean isUsed() {
            return CommonHelper.hasControlDown();
        }

        @Override
        public Component getNameComponent() {
            return CONTROL_COMPONENT;
        }
    },
    ALT {
        @Override
        public boolean isUsed() {
            return CommonHelper.hasAltDown();
        }

        @Override
        public Component getNameComponent() {
            return ALT_COMPONENT;
        }
    }
}
