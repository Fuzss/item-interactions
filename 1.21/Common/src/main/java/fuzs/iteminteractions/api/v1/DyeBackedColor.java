package fuzs.iteminteractions.api.v1;

import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Lifecycle;
import net.minecraft.world.item.DyeColor;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * Adapted from {@link net.minecraft.network.chat.TextColor} to use {@link DyeColor} instead of {@link net.minecraft.ChatFormatting}.
 */
public final class DyeBackedColor {
    private static final String CUSTOM_COLOR_PREFIX = "#";
    public static final Codec<DyeBackedColor> CODEC = Codec.STRING.comapFlatMap(DyeBackedColor::parseColor, DyeBackedColor::serialize);
    private static final Map<DyeColor, DyeBackedColor> LEGACY_FORMAT_TO_COLOR = Stream.of(DyeColor.values())
            .collect(ImmutableMap.toImmutableMap(Function.identity(), formatting -> new DyeBackedColor(formatting.getTextureDiffuseColor(), formatting.getName())));
    private static final Map<String, DyeBackedColor> NAMED_COLORS = LEGACY_FORMAT_TO_COLOR.values()
            .stream()
            .collect(ImmutableMap.toImmutableMap(textColor -> textColor.name, Function.identity()));

    private final int value;
    @Nullable
    private final String name;

    private DyeBackedColor(int value, String name) {
        this.value = value & 16777215;
        this.name = name;
    }

    private DyeBackedColor(int value) {
        this.value = value & 16777215;
        this.name = null;
    }

    public int getValue() {
        return this.value;
    }

    public String serialize() {
        return this.name != null ? this.name : this.formatValue();
    }

    public String formatValue() {
        return String.format(Locale.ROOT, "%s%06X", CUSTOM_COLOR_PREFIX, this.value);
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        } else if (object != null && this.getClass() == object.getClass()) {
            DyeBackedColor textColor = (DyeBackedColor)object;
            return this.value == textColor.value;
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.value, this.name);
    }

    @Override
    public String toString() {
        return this.serialize();
    }

    public static DyeBackedColor fromLegacyFormat(DyeColor formatting) {
        return LEGACY_FORMAT_TO_COLOR.get(formatting);
    }

    public static DyeBackedColor fromRgb(int color) {
        return new DyeBackedColor(color);
    }

    public static DataResult<DyeBackedColor> parseColor(String color) {
        if (color.startsWith(CUSTOM_COLOR_PREFIX)) {
            try {
                int i = Integer.parseInt(color.substring(1), 16);
                return i >= 0 && i <= 16777215 ? DataResult.success(fromRgb(i), Lifecycle.stable()) : DataResult.error(() -> "Color value out of range: " + color);
            } catch (NumberFormatException var2) {
                return DataResult.error(() -> "Invalid color value: " + color);
            }
        } else {
            DyeBackedColor textColor = NAMED_COLORS.get(color);
            return textColor == null ? DataResult.error(() -> "Invalid color name: " + color) : DataResult.success(textColor, Lifecycle.stable());
        }
    }
}

