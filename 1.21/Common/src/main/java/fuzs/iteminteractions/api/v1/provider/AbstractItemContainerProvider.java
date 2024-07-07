package fuzs.iteminteractions.api.v1.provider;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import fuzs.iteminteractions.api.v1.ContainerItemHelper;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public abstract class AbstractItemContainerProvider implements TooltipItemContainerProvider {
    @Nullable
    private final DyeColor dyeColor;
    private final float[] backgroundColor;
    private final List<Value> values = Lists.newArrayList();
    @Nullable
    private Set<Item> disallowedItems;

    public AbstractItemContainerProvider(@Nullable DyeColor dyeColor, String... nbtKey) {
        this.dyeColor = dyeColor;
        this.backgroundColor = ContainerItemHelper.getBackgroundColor(dyeColor);
    }

    protected float[] getBackgroundColor() {
        return this.backgroundColor;
    }

    public AbstractItemContainerProvider disallowValues(Collection<String> value) {
        for (String s : value) {
            this.disallowValue(s);
        }
        return this;
    }

    public AbstractItemContainerProvider disallowValue(String value) {
        boolean tag = value.startsWith("#");
        if (tag) value = value.substring(1);
        ResourceLocation id = ResourceLocation.tryParse(value);
        Objects.requireNonNull(id, "invalid resource location '%s'".formatted(value));
        Value valueObj = tag ? new TagValue(id) : new ItemValue(id);
        this.values.add(valueObj);
        return this;
    }

    @Override
    public boolean isItemAllowedInContainer(ItemStack containerStack, ItemStack stackToAdd) {
        if (this.disallowedItems == null) {
            this.disallowedItems = this.values.stream()
                    .flatMap(value -> value.getItems().stream())
                    .collect(ImmutableSet.toImmutableSet());
        }
        return !this.disallowedItems.contains(stackToAdd.getItem());
    }

    @Override
    public void toJson(JsonObject jsonObject) {
        if (this.dyeColor != null) {
            jsonObject.addProperty("background_color", this.dyeColor.getName());
        }
        RegistryCodecs.homogeneousList(Registries.ITEM);
        if (!this.values.isEmpty()) {
            JsonArray jsonArray = new JsonArray();
            for (Value value : this.values) {
                jsonArray.add(value.getValue());
            }
            jsonObject.add("disallowed_items", jsonArray);
        }
    }

    private interface Value {

        Collection<Item> getItems();

        String getValue();
    }

    private static class ItemValue implements Value {
        private final ResourceLocation value;
        @Nullable
        private final Item item;

        public ItemValue(ResourceLocation value) {
            this.value = value;
            this.item = BuiltInRegistries.ITEM.containsKey(this.value) ? BuiltInRegistries.ITEM.get(this.value) : null;
        }

        @Override
        public Collection<Item> getItems() {
            if (this.item == null) return Collections.emptySet();
            return Collections.singleton(this.item);
        }

        @Override
        public String getValue() {
            return this.value.toString();
        }
    }

    private static class TagValue implements Value {
        private final ResourceLocation value;
        private final TagKey<Item> tag;

        public TagValue(ResourceLocation value) {
            this.value = value;
            this.tag = TagKey.create(Registries.ITEM, this.value);
        }

        @Override
        public Collection<Item> getItems() {
            List<Item> list = Lists.newArrayList();
            for (Holder<Item> holder : BuiltInRegistries.ITEM.getTagOrEmpty(this.tag)) {
                list.add(holder.value());
            }
            return list;
        }

        @Override
        public String getValue() {
            return "#" + this.value.toString();
        }
    }
}
