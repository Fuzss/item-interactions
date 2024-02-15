package fuzs.iteminteractions.impl.client.helper;

import fuzs.iteminteractions.api.v1.provider.ItemContainerProvider;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public enum ItemDecoratorType {
    NONE("", -1),
    SPACE("+", ChatFormatting.YELLOW),
    PRESENT_AND_SPACE("+", ChatFormatting.GREEN),
    PRESENT_NO_SPACE("+", ChatFormatting.RED);

    private final String text;
    private final int color;

    ItemDecoratorType(String text, ChatFormatting color) {
        this(text, color.getColor());
    }

    ItemDecoratorType(String text, int color) {
        this.text = text;
        this.color = color;
    }

    public String getText() {
        return this.text;
    }

    public int getWidth(Font font) {
        return font.width(this.text);
    }

    public int getColor() {
        return this.color;
    }

    public static ItemDecoratorType getItemDecoratorType(ItemContainerProvider provider, ItemStack containerStack, ItemStack carriedStack, Player player) {
        if (provider.canAddItem(containerStack, carriedStack, player)) {
            if (provider.hasAnyOf(containerStack, carriedStack, player)) {
                return ItemDecoratorType.PRESENT_AND_SPACE;
            }
            return ItemDecoratorType.SPACE;
        } else if (provider.hasAnyOf(containerStack, carriedStack, player)) {
            return ItemDecoratorType.PRESENT_NO_SPACE;
        }
        return ItemDecoratorType.NONE;
    }

    @FunctionalInterface
    public interface Provider {

        ItemDecoratorType get(AbstractContainerScreen<?> screen, ItemStack stack, ItemStack carriedStack);
    }
}
