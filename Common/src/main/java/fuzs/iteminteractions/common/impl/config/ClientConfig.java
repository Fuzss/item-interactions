package fuzs.iteminteractions.common.impl.config;

import fuzs.iteminteractions.common.impl.ItemInteractions;
import fuzs.iteminteractions.common.impl.client.core.SimpleKeyType;
import fuzs.puzzleslib.common.api.config.v3.Config;
import fuzs.puzzleslib.common.api.config.v3.ConfigCore;

public class ClientConfig implements ConfigCore {
    private static final String KEY_MESSAGE_TYPE_DESCRIPTION = "Select a modifier key required to be held, otherwise selecting \"KEY\" serves as a toggle. The key mapping is defined in vanilla's controls menu.";

    @Config(description = {"Expand item tooltips to show their contents.", KEY_MESSAGE_TYPE_DESCRIPTION})
    public ItemContentsTooltip itemStorageTooltip = ItemContentsTooltip.ALWAYS;
    @Config(description = "Color item inventories on tooltips according to the container item's color.")
    public boolean itemStorageColors = true;
    @Config(description = "Pick the sprite used for highlighting the currently selected item in item content tooltips.")
    public SlotHighlight itemStorageHighlightSprite = SlotHighlight.HIGHLIGHT;
    @Config(description = {
            "Scroll vertically through contents shown on an item's tooltip. Otherwise, scrolling only works in horizontal directions.",
            "Select a modifier key required to be held to use vertical scrolling."
    })
    public SimpleKeyType verticalTooltipScrolling = SimpleKeyType.SHIFT;
    @Config(description = "Show an plus sign indicator on container items when the item stack held by the cursor can be added.")
    public boolean itemStorageIndicator = true;
    @Config(description = "Show a bar on container items indicating the current fill level.")
    public boolean itemStorageBar = true;
    @Config(description = "Select a modifier key required to be held to use single item movement when enabled in the server config.")
    public SimpleKeyType singleItemMovement = SimpleKeyType.CONTROL_OR_COMMAND;
    @Config(description = "Invert scroll wheel direction for moving items with a container item for single item movement.")
    public boolean reverseSingleItemScrolling = false;
    @Config(description = {
            "Show the item tooltip for the item currently held by the cursor to allow for scrolling through it.",
            KEY_MESSAGE_TYPE_DESCRIPTION
    })
    public ItemHeldByCursorTooltip itemHeldByCursorTooltip = ItemHeldByCursorTooltip.ALT;

    public boolean extractSingleItemOnly() {
        return this.singleItemMovement.isUsed()
                && ItemInteractions.CONFIG.get(ServerConfig.class).enableSingleItemMovement;
    }
}
