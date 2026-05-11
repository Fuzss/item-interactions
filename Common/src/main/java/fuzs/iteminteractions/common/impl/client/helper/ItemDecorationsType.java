package fuzs.iteminteractions.common.impl.client.helper;

import fuzs.iteminteractions.common.api.v1.world.item.storage.ItemStorageHolder;
import net.minecraft.client.gui.Font;
import net.minecraft.util.ARGB;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

public enum ItemDecorationsType {
    NOT_FULL(DyeColor.YELLOW),
    HAS_ITEM(DyeColor.GREEN),
    HAS_ITEM_BUT_FULL(DyeColor.RED);

    private final String string;
    private final int color;

    ItemDecorationsType(DyeColor dyeColor) {
        this("+", dyeColor);
    }

    ItemDecorationsType(String string, DyeColor dyeColor) {
        this(string, ARGB.opaque(dyeColor.getTextColor()));
    }

    ItemDecorationsType(String string, int color) {
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

    public static @Nullable ItemDecorationsType pickType(ItemStorageHolder behavior, ItemStack containerStack, ItemStack carriedStack, Player player) {
        if (behavior.canAddItem(containerStack, carriedStack, player)) {
            if (behavior.hasAnyOf(containerStack, carriedStack, player)) {
                return ItemDecorationsType.HAS_ITEM;
            } else {
                return ItemDecorationsType.NOT_FULL;
            }
        } else if (behavior.hasAnyOf(containerStack, carriedStack, player)) {
            return ItemDecorationsType.HAS_ITEM_BUT_FULL;
        } else {
            return null;
        }
    }
}
