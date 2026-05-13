package fuzs.iteminteractions.common.impl.world.item.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;

public final class SelectedItem {
    public static final int DEFAULT_SELECTED_ITEM = -1;
    public static final SelectedItem DEFAULT = new SelectedItem(DEFAULT_SELECTED_ITEM);
    public static final Codec<SelectedItem> CODEC = MapCodec.unitCodec(DEFAULT);
    public static final StreamCodec<ByteBuf, SelectedItem> STREAM_CODEC = StreamCodec.unit(DEFAULT);

    private final int selectedItemIndex;

    private SelectedItem(int selectedItemIndex) {
        this.selectedItemIndex = selectedItemIndex;
    }

    public static SelectedItem of(int selectedItemIndex) {
        if (selectedItemIndex == -1) {
            return DEFAULT;
        } else if (selectedItemIndex >= 0) {
            return new SelectedItem(selectedItemIndex);
        } else {
            throw new IllegalArgumentException("Invalid selectedItemIndex: " + selectedItemIndex);
        }
    }

    public int selectedItem() {
        return this.selectedItemIndex;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else {
            return obj instanceof SelectedItem;
        }
    }

    @Override
    public int hashCode() {
        return SelectedItem.class.hashCode();
    }

    @Override
    public String toString() {
        return "SelectedItem[" + this.selectedItemIndex + "]";
    }
}
