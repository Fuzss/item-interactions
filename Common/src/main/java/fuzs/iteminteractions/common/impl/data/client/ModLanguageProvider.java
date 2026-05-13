package fuzs.iteminteractions.common.impl.data.client;

import fuzs.iteminteractions.common.impl.ItemInteractions;
import fuzs.iteminteractions.common.impl.client.core.KeyType;
import fuzs.iteminteractions.common.impl.client.gui.screens.inventory.tooltip.CollapsibleClientTooltipComponent;
import fuzs.iteminteractions.common.impl.config.ItemContentsTooltip;
import fuzs.iteminteractions.common.impl.config.ItemHeldByCursorTooltip;
import fuzs.puzzleslib.common.api.client.data.v2.AbstractLanguageProvider;
import fuzs.puzzleslib.common.api.data.v2.core.DataProviderContext;

public class ModLanguageProvider extends AbstractLanguageProvider {

    public ModLanguageProvider(DataProviderContext context) {
        super(context);
    }

    @Override
    public void addTranslations(TranslationBuilder builder) {
        builder.add(CollapsibleClientTooltipComponent.REVEAL_CONTENTS_TRANSLATION_KEY, "%s %s to reveal contents");
        builder.add(KeyType.HOLD_COMPONENT, "Hold");
        builder.add(KeyType.TOGGLE_COMPONENT, "Toggle");
        builder.add(KeyType.SHIFT_COMPONENT, "Shift");
        builder.add(KeyType.CONTROL_COMPONENT, "Control");
        builder.add(KeyType.ALT_COMPONENT, "Alt");
        builder.add(ItemContentsTooltip.KEY_MAPPING, "Toggle Item Contents Tooltip");
        builder.add(ItemHeldByCursorTooltip.KEY_MAPPING, "Toggle Item Held By Cursor Tooltip");
        builder.addKeyCategory(ItemInteractions.MOD_ID, ItemInteractions.MOD_NAME);
    }
}
