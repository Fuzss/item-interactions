package fuzs.iteminteractions.common.api.v2.client.gui.screens.inventory.tooltip;

import fuzs.iteminteractions.common.api.v2.world.item.storage.ItemStorageHolder;
import fuzs.iteminteractions.common.impl.ItemInteractions;
import fuzs.iteminteractions.common.impl.config.ClientConfig;
import fuzs.puzzleslib.common.api.client.gui.v2.tooltip.TooltipRenderHelper;
import fuzs.puzzleslib.common.api.util.v1.CommonHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public interface SelectedItemPreviewProvider {
    String ITEM_COUNT_KEY = ItemInteractions.id("container")
            .toLanguageKey(Registries.elementsDirPath(Registries.ITEM), "item_count");
    SelectedItemPreviewProvider INSTANCE = new SelectedItemPreviewProvider() {
        // NO-OP
    };

    static SelectedItemPreviewProvider getSelectedItemTooltipProvider(ItemStack itemStack) {
        return ItemStorageHolder.ofItem(itemStack)
                .getTooltipImage(itemStack, CommonHelper.getClientPlayer())
                .flatMap(Function.identity())
                .map(TooltipRenderHelper::createImageComponent)
                .orElse(null) instanceof SelectedItemPreviewProvider tooltipProvider ? tooltipProvider.unwrap() :
                SelectedItemPreviewProvider.INSTANCE;
    }

    default SelectedItemPreviewProvider unwrap() {
        return this;
    }

    default List<ClientTooltipComponent> createSelectedItemComponents(ItemStack selectedItem) {
        List<ClientTooltipComponent> tooltipComponents = new ArrayList<>();
        Component itemName = this.createItemName(selectedItem);
        tooltipComponents.add(ClientTooltipComponent.create(itemName.getVisualOrderText()));
        return tooltipComponents;
    }

    private Component createItemName(ItemStack selectedItem) {
        Component itemName = selectedItem.getStyledHoverName();
        if (ItemInteractions.CONFIG.get(ClientConfig.class).itemStorageContentsCount) {
            return this.getItemNameWithCount(itemName);
        } else {
            return itemName;
        }
    }

    default Component getItemNameWithCount(Component itemName) {
        int contentsCount = this.getContentsCount();
        if (contentsCount > 0) {
            return Component.translatable(ITEM_COUNT_KEY, itemName, contentsCount).withStyle(ChatFormatting.GRAY);
        } else {
            return itemName;
        }
    }

    default int getContentsCount() {
        return 0;
    }
}
