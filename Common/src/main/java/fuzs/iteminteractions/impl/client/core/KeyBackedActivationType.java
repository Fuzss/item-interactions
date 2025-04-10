package fuzs.iteminteractions.impl.client.core;

import com.mojang.blaze3d.platform.InputConstants;
import fuzs.iteminteractions.impl.ItemInteractions;
import fuzs.puzzleslib.api.client.key.v1.KeyMappingHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.network.chat.Component;

public class KeyBackedActivationType extends HeldActivationType implements KeyMappingProvider {
    public static final String KEY_CATEGORY = "key.categories." + ItemInteractions.MOD_ID;
    public static final String KEY_TOOLTIP_PRESS_TRANSLATION = "item.container.tooltip.press";

    private final KeyMapping keyMapping;
    private boolean active;

    public KeyBackedActivationType(String id) {
        super(id);
        this.keyMapping = new KeyMapping("key." + id, InputConstants.UNKNOWN.getValue(), KEY_CATEGORY);
    }

    @Override
    public String getIdentifier() {
        return "KEY";
    }

    @Override
    public Component getComponent(String translationId) {
        Component keyName = this.keyMapping.getTranslatedKeyMessage();
        return Component.translatable(translationId,
                Component.translatable(KEY_TOOLTIP_PRESS_TRANSLATION),
                Component.empty().append(keyName).withStyle(ChatFormatting.YELLOW)).withStyle(ChatFormatting.GRAY);
    }

    @Override
    public boolean isActive() {
        return this.active;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode) {
        if (KeyMappingHelper.isKeyActiveAndMatches(this.keyMapping, keyCode, scanCode)) {
            this.active = !this.active;
            return true;
        }
        return false;
    }

    @Override
    public KeyMapping getKeyMapping() {
        return this.keyMapping;
    }
}
