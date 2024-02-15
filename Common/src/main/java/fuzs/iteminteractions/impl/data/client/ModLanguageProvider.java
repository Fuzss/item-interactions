package fuzs.iteminteractions.impl.data.client;

import fuzs.iteminteractions.api.v1.client.tooltip.ExpandableClientTooltipComponent;
import fuzs.iteminteractions.impl.ItemInteractions;
import fuzs.iteminteractions.impl.client.core.HeldActivationType;
import fuzs.iteminteractions.impl.client.core.KeyBackedActivationType;
import fuzs.iteminteractions.impl.client.handler.KeyBindingTogglesHandler;
import fuzs.puzzleslib.api.client.data.v2.AbstractLanguageProvider;
import fuzs.puzzleslib.api.data.v2.core.DataProviderContext;

public class ModLanguageProvider extends AbstractLanguageProvider {

    public ModLanguageProvider(DataProviderContext context) {
        super(context);
    }

    @Override
    public void addTranslations(TranslationBuilder builder) {
        builder.add(ExpandableClientTooltipComponent.REVEAL_CONTENTS_TRANSLATION_KEY, "%s %s to reveal contents");
        builder.add(HeldActivationType.TOOLTIP_HOLD_TRANSLATION_KEY, "Hold");
        builder.add(KeyBackedActivationType.KEY_TOOLTIP_PRESS_TRANSLATION, "Press");
        builder.add(KeyBindingTogglesHandler.VISUAL_ITEM_CONTENTS_KEY.getKeyMapping(), "Toggle Visual Item Contents");
        builder.add(KeyBindingTogglesHandler.SELECTED_ITEM_TOOLTIPS_KEY.getKeyMapping(), "Toggle Selected Item Tooltips");
        builder.add(KeyBindingTogglesHandler.CARRIED_ITEM_TOOLTIPS_KEY.getKeyMapping(), "Toggle Carried Item Tooltips");
        builder.add(KeyBackedActivationType.KEY_CATEGORY, ItemInteractions.MOD_NAME);
    }
}
