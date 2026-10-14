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
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public interface SelectedItemPreviewProvider {
    String ITEM_COUNT_KEY = ItemInteractions.id("container")
            .toLanguageKey(Registries.elementsDirPath(Registries.ITEM), "item_count");
    SelectedItemPreviewProvider INSTANCE = new SelectedItemPreviewProvider() {
        // NO-OP
    };

    static SelectedItemPreviewProvider getSelectedItemTooltipProvider(ItemStorageHolder holder, ItemStack itemStack) {
        TooltipComponent tooltipComponent = getTooltipComponent(holder, itemStack);
        if (tooltipComponent != null) {
            ClientTooltipComponent clientComponent = TooltipRenderHelper.createImageComponent(tooltipComponent);
            if (clientComponent instanceof SelectedItemPreviewProvider tooltipProvider) {
                return tooltipProvider.unwrap();
            }
        }

        return SelectedItemPreviewProvider.INSTANCE;
    }

    private static @Nullable TooltipComponent getTooltipComponent(ItemStorageHolder holder, ItemStack itemStack) {
        return holder.getTooltipImage(itemStack, CommonHelper.getClientPlayer())
                .flatMap(Function.identity())
                .orElse(null);
    }

    static Component getItemNameWithCount(Component itemName, int contentsCount) {
        if (contentsCount > 0) {
            return Component.translatable(ITEM_COUNT_KEY, itemName, contentsCount).withStyle(ChatFormatting.GRAY);
        } else {
            return itemName;
        }
    }

    default SelectedItemPreviewProvider unwrap() {
        return this;
    }

    default List<ClientTooltipComponent> createSelectedItemComponents(ItemStorageHolder holder, ItemStack selectedItem) {
        List<ClientTooltipComponent> tooltipComponents = new ArrayList<>();
        Component itemName = this.createItemName(holder, selectedItem);
        tooltipComponents.add(ClientTooltipComponent.create(itemName.getVisualOrderText()));
        return tooltipComponents;
    }

    default Component createItemName(ItemStorageHolder holder, ItemStack selectedItem) {
        Component itemName = selectedItem.getStyledHoverName();
        if (ItemInteractions.CONFIG.get(ClientConfig.class).itemStorageContentsCount) {
            int contentsCount = holder.getContentsCount(selectedItem, CommonHelper.getClientPlayer());
            return getItemNameWithCount(itemName, contentsCount);
        } else {
            return itemName;
        }
    }
}
