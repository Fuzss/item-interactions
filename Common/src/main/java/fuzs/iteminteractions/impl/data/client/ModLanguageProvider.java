package fuzs.iteminteractions.impl.data.client;

import fuzs.iteminteractions.api.v1.client.tooltip.ExpandableClientContentsTooltip;
import fuzs.iteminteractions.impl.ItemInteractions;
import fuzs.iteminteractions.impl.client.core.ActivationTypeProvider;
import fuzs.iteminteractions.impl.config.CarriedItemTooltips;
import fuzs.iteminteractions.impl.config.SelectedItemTooltips;
import fuzs.iteminteractions.impl.config.VisualItemContents;
import fuzs.puzzleslib.api.client.data.v2.AbstractLanguageProvider;
import fuzs.puzzleslib.api.data.v2.core.DataProviderContext;

public class ModLanguageProvider extends AbstractLanguageProvider {

    public ModLanguageProvider(DataProviderContext context) {
        super(context);
    }

    @Override
    public void addTranslations(TranslationBuilder builder) {
        builder.add(ExpandableClientContentsTooltip.REVEAL_CONTENTS_TRANSLATION_KEY, "%s %s to reveal contents");
        builder.add(ActivationTypeProvider.HOLD_COMPONENT, "Hold");
        builder.add(ActivationTypeProvider.TOGGLE_COMPONENT, "Toggle");
        builder.add(ActivationTypeProvider.SHIFT_COMPONENT, "Shift");
        builder.add(ActivationTypeProvider.CONTROL_COMPONENT, "Control");
        builder.add(ActivationTypeProvider.COMMAND_COMPONENT, "Command");
        builder.add(ActivationTypeProvider.ALT_COMPONENT, "Alt");
        builder.add(VisualItemContents.KEY_MAPPING, "Toggle Visual Item Contents");
        builder.add(SelectedItemTooltips.KEY_MAPPING, "Toggle Selected Item Tooltips");
        builder.add(CarriedItemTooltips.KEY_MAPPING, "Toggle Carried Item Tooltips");
        builder.addKeyCategory(ItemInteractions.MOD_ID, ItemInteractions.MOD_NAME);
    }
}
