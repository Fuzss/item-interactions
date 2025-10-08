package fuzs.iteminteractions.impl.config;

import fuzs.puzzleslib.api.config.v3.Config;
import fuzs.puzzleslib.api.config.v3.ConfigCore;

public class ClientConfig implements ConfigCore {
    private static final String ACTIVATION_TYPE_MESSAGE = "Select a modifier key required to be held, otherwise selecting \"KEY\" serves as a toggle. The key is defined in vanilla's controls menu.";

    @Config(description = "Color item inventories on tooltips according to the container item's color.")
    public boolean colorfulTooltips = true;
    @Config(name = "reveal_contents",
            description = {"Expand container item tooltips to reveal their contents.", ACTIVATION_TYPE_MESSAGE})
    public VisualItemContents visualItemContents = VisualItemContents.ALWAYS;
    @Config(description = "Render a white overlay or the hotbar selected item frame over the slot the next item will be taken out of when right-clicking the container item.")
    public SlotOverlay slotOverlay = SlotOverlay.HOVER;
    @Config(description = "Show an indicator on container items when the stack carried by the cursor can be added in your inventory.")
    public boolean containerItemIndicator = true;
    @Config(description = {
            "Show a tooltip for the item currently selected in a container item's tooltip next to the main tooltip.",
            ACTIVATION_TYPE_MESSAGE
    })
    public SelectedItemTooltips selectedItemTooltips = SelectedItemTooltips.ALWAYS;
    @Config(name = "precision_mode", description = {
            "Select a modifier key required to be held to use precision mode.", ServerConfig.PRECISION_MODE_MESSAGE
    })
    public ExtractSingleItem extractSingleItem = ExtractSingleItem.CONTROL;
    @Config(description = "Disable sounds from inserting and extracting items from playing, as they trigger quite often with all the new interactions.")
    public boolean disableInteractionSounds = true;
    @Config(description = {
            "Always show item tooltips while interacting with container items, even when the cursor is currently carrying an item.",
            ACTIVATION_TYPE_MESSAGE
    })
    public CarriedItemTooltips carriedItemTooltips = CarriedItemTooltips.ALT;
    @Config(description = "Invert scroll wheel direction for extracting / inserting items from a container item in precision mode.")
    public boolean invertPrecisionModeScrolling = false;

    public enum SlotOverlay {
        HOTBAR,
        HOVER
    }
}
