package fuzs.iteminteractions.impl.client.core;

import fuzs.puzzleslib.api.util.v1.CommonHelper;
import net.minecraft.client.input.InputQuirks;
import net.minecraft.network.chat.Component;

public enum KeyBackedActivationType implements ActivationTypeProvider {
    SHIFT {
        @Override
        public Component getNameComponent() {
            return SHIFT_COMPONENT;
        }

        @Override
        public boolean isActive() {
            return CommonHelper.hasShiftDown();
        }
    },
    CONTROL {
        @Override
        public Component getNameComponent() {
            return InputQuirks.REPLACE_CTRL_KEY_WITH_CMD_KEY ? COMMAND_COMPONENT : CONTROL_COMPONENT;
        }

        @Override
        public boolean isActive() {
            return CommonHelper.hasControlDown();
        }
    },
    ALT {
        @Override
        public Component getNameComponent() {
            return ALT_COMPONENT;
        }

        @Override
        public boolean isActive() {
            return CommonHelper.hasAltDown();
        }
    },
    ALWAYS {
        @Override
        public boolean isActive() {
            return true;
        }
    }
}
