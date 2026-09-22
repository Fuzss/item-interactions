package fuzs.iteminteractions.common.impl.data.client;

import fuzs.iteminteractions.common.impl.ItemInteractions;
import fuzs.iteminteractions.common.impl.client.core.KeyType;
import fuzs.iteminteractions.common.impl.client.gui.screens.inventory.tooltip.CollapsibleClientTooltipComponent;
import fuzs.iteminteractions.common.impl.config.ItemHeldByCursorTooltip;
import fuzs.iteminteractions.common.impl.config.ItemStorageTooltip;
import fuzs.puzzleslib.common.api.client.data.v3.language.AbstractLanguageProvider;
import fuzs.puzzleslib.common.api.data.v3.core.DataProviderContext;

public class ModLanguageProvider extends AbstractLanguageProvider {

    public ModLanguageProvider(DataProviderContext context) {
        super(context);
    }

    @Override
    public void addTranslations() {
        this.add(CollapsibleClientTooltipComponent.REVEAL_CONTENTS_TRANSLATION_KEY, "%s %s to reveal contents");
        this.add(KeyType.HOLD_COMPONENT, "Hold");
        this.add(KeyType.TOGGLE_COMPONENT, "Toggle");
        this.add(KeyType.SHIFT_COMPONENT, "Shift");
        this.add(KeyType.CONTROL_COMPONENT, "Control");
        this.add(KeyType.COMMAND_COMPONENT, "Command");
        this.add(KeyType.ALT_COMPONENT, "Alt");
        this.add(ItemStorageTooltip.KEY_MAPPING, "Toggle Item Storage Tooltip");
        this.add(ItemHeldByCursorTooltip.KEY_MAPPING, "Toggle Item Held By Cursor Tooltip");
        this.addKeyCategory(ItemInteractions.MOD_ID, ItemInteractions.MOD_NAME);
    }
}
