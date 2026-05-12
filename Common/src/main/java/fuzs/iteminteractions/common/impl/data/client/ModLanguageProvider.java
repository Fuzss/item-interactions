package fuzs.iteminteractions.common.impl.data.client;

import fuzs.iteminteractions.common.impl.ItemInteractions;
import fuzs.iteminteractions.common.impl.client.core.ActivationTypeProvider;
import fuzs.iteminteractions.common.impl.client.gui.screens.inventory.tooltip.CollapsibleClientTooltipComponent;
import fuzs.iteminteractions.common.impl.config.CarriedItemTooltips;
import fuzs.iteminteractions.common.impl.config.VisualItemContents;
import fuzs.puzzleslib.common.api.client.data.v2.AbstractLanguageProvider;
import fuzs.puzzleslib.common.api.data.v2.core.DataProviderContext;

public class ModLanguageProvider extends AbstractLanguageProvider {

    public ModLanguageProvider(DataProviderContext context) {
        super(context);
    }

    @Override
    public void addTranslations(TranslationBuilder builder) {
        builder.add(CollapsibleClientTooltipComponent.REVEAL_CONTENTS_TRANSLATION_KEY, "%s %s to reveal contents");
        builder.add(ActivationTypeProvider.HOLD_COMPONENT, "Hold");
        builder.add(ActivationTypeProvider.TOGGLE_COMPONENT, "Toggle");
        builder.add(ActivationTypeProvider.SHIFT_COMPONENT, "Shift");
        builder.add(ActivationTypeProvider.CONTROL_COMPONENT, "Control");
        builder.add(ActivationTypeProvider.COMMAND_COMPONENT, "Command");
        builder.add(ActivationTypeProvider.ALT_COMPONENT, "Alt");
        builder.add(VisualItemContents.KEY_MAPPING, "Toggle Visual Item Contents");
        builder.add(CarriedItemTooltips.KEY_MAPPING, "Toggle Carried Item Tooltips");
        builder.addKeyCategory(ItemInteractions.MOD_ID, ItemInteractions.MOD_NAME);
    }
}
