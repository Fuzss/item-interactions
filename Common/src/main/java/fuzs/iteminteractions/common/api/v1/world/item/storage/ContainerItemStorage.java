package fuzs.iteminteractions.common.api.v1.world.item.storage;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fuzs.iteminteractions.common.api.v1.world.item.DyeBackedColor;
import fuzs.iteminteractions.common.api.v1.world.inventory.tooltip.ItemContentsTooltip;
import fuzs.iteminteractions.common.impl.init.ModRegistry;
import fuzs.iteminteractions.common.impl.world.inventory.ContainerSlotHelper;
import fuzs.puzzleslib.common.api.container.v1.ContainerMenuHelper;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import org.jspecify.annotations.Nullable;

import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;

public class ContainerItemStorage extends ContentsBackedItemStorage {
    public static final MapCodec<ContainerItemStorage> CODEC = RecordCodecBuilder.mapCodec(instance -> {
        return instance.group(inventoryWidthCodec(),
                        inventoryHeightCodec(),
                        backgroundColorCodec(),
                        itemContentsCodec(),
                        interactionPermissionsCodec(),
                        equipmentSlotsCodec())
                .apply(instance,
                        (Integer inventoryWidth, Integer inventoryHeight, Optional<DyeBackedColor> dyeColor, ItemContents itemContents, InteractionPermissions interactionPermissions, EquipmentSlotGroup equipmentSlots) -> {
                            return new ContainerItemStorage(inventoryWidth,
                                    inventoryHeight,
                                    dyeColor.orElse(null)).itemContents(itemContents)
                                    .interactionPermissions(interactionPermissions)
                                    .equipmentSlots(equipmentSlots);
                        });
    });
    private static final EquipmentSlot[] EQUIPMENT_SLOTS = EquipmentSlot.values();

    final int inventoryWidth;
    final int inventoryHeight;
    InteractionPermissions interactionPermissions = InteractionPermissions.ALWAYS;
    EquipmentSlotGroup equipmentSlots = EquipmentSlotGroup.ANY;

    public ContainerItemStorage(int inventoryWidth, int inventoryHeight) {
        this(inventoryWidth, inventoryHeight, null);
    }

    public ContainerItemStorage(int inventoryWidth, int inventoryHeight, @Nullable DyeBackedColor dyeColor) {
        super(dyeColor);
        this.inventoryWidth = inventoryWidth;
        this.inventoryHeight = inventoryHeight;
    }

    protected static <T extends ContainerItemStorage> RecordCodecBuilder<T, Integer> inventoryWidthCodec() {
        return ExtraCodecs.POSITIVE_INT.fieldOf("inventory_width").forGetter(ContainerItemStorage::getInventoryWidth);
    }

    protected static <T extends ContainerItemStorage> RecordCodecBuilder<T, Integer> inventoryHeightCodec() {
        return ExtraCodecs.POSITIVE_INT.fieldOf("inventory_height").forGetter(ContainerItemStorage::getInventoryHeight);
    }

    protected static <T extends ContainerItemStorage> RecordCodecBuilder<T, InteractionPermissions> interactionPermissionsCodec() {
        return InteractionPermissions.CODEC.fieldOf("interaction_permissions")
                .orElse(InteractionPermissions.ALWAYS)
                .forGetter(provider -> provider.interactionPermissions);
    }

    protected static <T extends ContainerItemStorage> RecordCodecBuilder<T, EquipmentSlotGroup> equipmentSlotsCodec() {
        return EquipmentSlotGroup.CODEC.fieldOf("equipment_slots")
                .orElse(EquipmentSlotGroup.ANY)
                .forGetter(provider -> provider.equipmentSlots);
    }

    @Override
    protected ContainerItemStorage itemContents(ItemContents itemContents) {
        return (ContainerItemStorage) super.itemContents(itemContents);
    }

    @Override
    public ContainerItemStorage filterContainerItems(boolean filterContainerItems) {
        return (ContainerItemStorage) super.filterContainerItems(filterContainerItems);
    }

    public ContainerItemStorage interactionPermissions(InteractionPermissions interactionPermissions) {
        this.interactionPermissions = interactionPermissions;
        return this;
    }

    public ContainerItemStorage equipmentSlots(EquipmentSlotGroup equipmentSlots) {
        this.equipmentSlots = equipmentSlots;
        return this;
    }

    protected int getInventoryWidth() {
        return this.inventoryWidth;
    }

    protected int getInventoryHeight() {
        return this.inventoryHeight;
    }

    public int getInventorySize() {
        return this.getInventoryWidth() * this.getInventoryHeight();
    }

    @Override
    public boolean hasContents(ItemStack containerStack) {
        return containerStack.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY)
                != ItemContainerContents.EMPTY;
    }

    @Override
    public boolean allowsPlayerInteractions(ItemStack containerStack, Player player) {
        return super.allowsPlayerInteractions(containerStack, player)
                && this.interactionPermissions.allowsPlayerInteractions(player) && (player.getAbilities().instabuild
                || this.equipmentSlots == EquipmentSlotGroup.ANY || Arrays.stream(EQUIPMENT_SLOTS)
                .filter(this.equipmentSlots::test)
                .map(player::getItemBySlot)
                .anyMatch((ItemStack itemStack) -> itemStack == containerStack));
    }

    @Override
    public SimpleContainer getItemContainer(ItemStack containerStack, Player player, boolean allowSaving) {
        NonNullList<ItemStack> items = NonNullList.withSize(this.getInventorySize(), ItemStack.EMPTY);
        containerStack.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY).copyInto(items);
        return ContainerMenuHelper.createListBackedContainer(items, allowSaving ? (Container container) -> {
            containerStack.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(items));
        } : null);
    }

    @Override
    public TooltipComponent createTooltipImageComponent(ItemStack itemStack, Player player, NonNullList<ItemStack> items) {
        int selectedItem = ContainerSlotHelper.getSelectedItem(itemStack);
        return new ItemContentsTooltip(items,
                this.getInventoryWidth(),
                this.getInventoryHeight(),
                this.dyeColor,
                selectedItem);
    }

    @Override
    public Type<?> getType() {
        return ModRegistry.CONTAINER_ITEM_CONTENTS_PROVIDER_TYPE.value();
    }

    public enum InteractionPermissions implements StringRepresentable {
        ALWAYS,
        CREATIVE_ONLY,
        NEVER;

        public static final Codec<InteractionPermissions> CODEC = StringRepresentable.fromValues(InteractionPermissions::values);

        public boolean allowsPlayerInteractions(Player player) {
            return this == ALWAYS || this != NEVER && player.getAbilities().instabuild;
        }

        @Override
        public String getSerializedName() {
            return this.name().toLowerCase(Locale.ROOT);
        }
    }
}
