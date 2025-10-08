package fuzs.iteminteractions.impl.client.helper;

import fuzs.iteminteractions.api.v1.provider.ItemContentsBehavior;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.util.ARGB;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public enum ItemDecoratorType {
    NONE("", -1),
    SPACE("+", ChatFormatting.YELLOW),
    PRESENT_AND_SPACE("+", ChatFormatting.GREEN),
    PRESENT_NO_SPACE("+", ChatFormatting.RED);

    private final String string;
    private final int color;

    ItemDecoratorType(String string, ChatFormatting chatFormatting) {
        this(string, ARGB.opaque(chatFormatting.getColor()));
    }

    ItemDecoratorType(String string, int color) {
        this.string = string;
        this.color = color;
    }

    public String getString() {
        return this.string;
    }

    public int getWidth(Font font) {
        return font.width(this.string);
    }

    public int getColor() {
        return this.color;
    }

    public boolean mayRender() {
        return this != NONE;
    }

    public static ItemDecoratorType getItemDecoratorType(ItemContentsBehavior behavior, ItemStack containerStack, ItemStack carriedStack, Player player) {
        if (behavior.canAddItem(containerStack, carriedStack, player)) {
            if (behavior.hasAnyOf(containerStack, carriedStack, player)) {
                return ItemDecoratorType.PRESENT_AND_SPACE;
            } else {
                return ItemDecoratorType.SPACE;
            }
        } else if (behavior.hasAnyOf(containerStack, carriedStack, player)) {
            return ItemDecoratorType.PRESENT_NO_SPACE;
        } else {
            return ItemDecoratorType.NONE;
        }
    }
}
