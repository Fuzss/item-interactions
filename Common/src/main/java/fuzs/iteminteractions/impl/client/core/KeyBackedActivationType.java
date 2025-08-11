package fuzs.iteminteractions.impl.client.core;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public enum KeyBackedActivationType implements ActivationTypeProvider {
    SHIFT {
        @Override
        public Component getNameComponent() {
            return SHIFT_COMPONENT;
        }

        @Override
        public boolean isActive() {
            return Screen.hasShiftDown();
        }
    },
    CONTROL {
        @Override
        public Component getNameComponent() {
            return Minecraft.ON_OSX ? COMMAND_COMPONENT : CONTROL_COMPONENT;
        }

        @Override
        public boolean isActive() {
            return Screen.hasControlDown();
        }
    },
    ALT {
        @Override
        public Component getNameComponent() {
            return ALT_COMPONENT;
        }

        @Override
        public boolean isActive() {
            return Screen.hasAltDown();
        }
    },
    ALWAYS {
        @Override
        public boolean isActive() {
            return true;
        }
    }
}
