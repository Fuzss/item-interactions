package fuzs.iteminteractions.impl.client.core;

import fuzs.iteminteractions.impl.ItemInteractions;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

public interface ActivationTypeProvider {
    Component HOLD_COMPONENT = makeDescriptionComponent("hold");
    Component TOGGLE_COMPONENT = makeDescriptionComponent("toggle");
    Component SHIFT_COMPONENT = makeDescriptionComponent("shift");
    Component CONTROL_COMPONENT = makeDescriptionComponent("control");
    Component COMMAND_COMPONENT = makeDescriptionComponent("command");
    Component ALT_COMPONENT = makeDescriptionComponent("alt");

    boolean isActive();

    default Component getNameComponent() {
        return CommonComponents.EMPTY;
    }

    default Component getComponent(String translationId) {
        return Component.translatable(translationId,
                this.getActivationModeComponent(),
                this.getNameComponent().copy().withStyle(ChatFormatting.LIGHT_PURPLE)).withStyle(ChatFormatting.GRAY);
    }

    default Component getActivationModeComponent() {
        return HOLD_COMPONENT;
    }

    static Component makeDescriptionComponent(String serializedName) {
        return Component.translatable(ItemInteractions.id("container")
                .toLanguageKey(Registries.elementsDirPath(Registries.ITEM), "tooltip." + serializedName));
    }
}
