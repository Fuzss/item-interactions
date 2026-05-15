package fuzs.iteminteractions.common.api.v1.world.item.storage;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fuzs.iteminteractions.common.api.v1.world.inventory.tooltip.ItemContentsTooltip;
import fuzs.iteminteractions.common.api.v1.world.item.DyeBackedColor;
import fuzs.iteminteractions.common.impl.init.ModRegistry;
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

import java.util.Locale;
import java.util.Optional;

public class ContainerStorage extends ComponentBackedStorage {
    public static final MapCodec<ContainerStorage> CODEC = RecordCodecBuilder.mapCodec(instance -> {
        return instance.group(inventoryWidthCodec(),
                        inventoryHeightCodec(),
                        backgroundColorCodec(),
                        itemContentsCodec(),
                        interactionPermissionsCodec(),
                        equipmentSlotsCodec())
                .apply(instance,
                        (Integer inventoryWidth, Integer inventoryHeight, Optional<DyeBackedColor> dyeColor, StorageOptions storageOptions, InteractionPermissions interactionPermissions, EquipmentSlotGroup equipmentSlots) -> {
                            return new ContainerStorage(inventoryWidth,
                                    inventoryHeight,
                                    dyeColor.orElse(null),
                                    storageOptions).interactionPermissions(interactionPermissions)
                                    .equipmentSlots(equipmentSlots);
                        });
    });
    private static final EquipmentSlot[] EQUIPMENT_SLOTS = EquipmentSlot.values();

    final int inventoryWidth;
    final int inventoryHeight;
    @Nullable
    final DyeBackedColor dyeColor;
    InteractionPermissions interactionPermissions = InteractionPermissions.ALWAYS;
    EquipmentSlotGroup equipmentSlots = EquipmentSlotGroup.ANY;

    public ContainerStorage(int inventoryWidth, int inventoryHeight) {
        this(inventoryWidth, inventoryHeight, null);
    }

    public ContainerStorage(int inventoryWidth, int inventoryHeight, @Nullable DyeBackedColor dyeColor) {
        this(inventoryWidth, inventoryHeight, dyeColor, StorageOptions.DEFAULT);
    }

    public ContainerStorage(int inventoryWidth, int inventoryHeight, @Nullable DyeBackedColor dyeColor, StorageOptions storageOptions) {
        super(storageOptions);
        this.inventoryWidth = inventoryWidth;
        this.inventoryHeight = inventoryHeight;
        this.dyeColor = dyeColor;
    }

    protected static <T extends ContainerStorage> RecordCodecBuilder<T, Integer> inventoryWidthCodec() {
        return ExtraCodecs.POSITIVE_INT.fieldOf("inventory_width").forGetter(ContainerStorage::getInventoryWidth);
    }

    protected static <T extends ContainerStorage> RecordCodecBuilder<T, Integer> inventoryHeightCodec() {
        return ExtraCodecs.POSITIVE_INT.fieldOf("inventory_height").forGetter(ContainerStorage::getInventoryHeight);
    }

    protected static <T extends ContainerStorage> RecordCodecBuilder<T, Optional<DyeBackedColor>> backgroundColorCodec() {
        return DyeBackedColor.CODEC.optionalFieldOf("background_color")
                .forGetter((T storage) -> Optional.ofNullable(storage.dyeColor));
    }

    protected static <T extends ContainerStorage> RecordCodecBuilder<T, InteractionPermissions> interactionPermissionsCodec() {
        return InteractionPermissions.CODEC.fieldOf("interaction_permissions")
                .orElse(InteractionPermissions.ALWAYS)
                .forGetter(ContainerStorage::getInteractionPermissions);
    }

    protected static <T extends ContainerStorage> RecordCodecBuilder<T, EquipmentSlotGroup> equipmentSlotsCodec() {
        return EquipmentSlotGroup.CODEC.fieldOf("equipment_slots")
                .orElse(EquipmentSlotGroup.ANY)
                .forGetter(ContainerStorage::getEquipmentSlots);
    }

    public ContainerStorage interactionPermissions(InteractionPermissions interactionPermissions) {
        this.interactionPermissions = interactionPermissions;
        return this;
    }

    public ContainerStorage equipmentSlots(EquipmentSlotGroup equipmentSlots) {
        this.equipmentSlots = equipmentSlots;
        return this;
    }

    @Override
    public int getGridWidth(int itemCount) {
        return this.getInventoryWidth();
    }

    @Override
    public int getGridHeight(int itemCount) {
        return this.getInventoryHeight();
    }

    public int getInventoryWidth() {
        return this.inventoryWidth;
    }

    public int getInventoryHeight() {
        return this.inventoryHeight;
    }

    public int getInventorySize() {
        return this.getInventoryWidth() * this.getInventoryHeight();
    }

    public InteractionPermissions getInteractionPermissions() {
        return this.interactionPermissions;
    }

    public EquipmentSlotGroup getEquipmentSlots() {
        return this.equipmentSlots;
    }

    @Override
    public boolean hasContents(ItemStack itemStack) {
        return itemStack.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY)
                != ItemContainerContents.EMPTY;
    }

    @Override
    public boolean canPlayerInteractWith(ItemStack itemStack, Player player) {
        if (!itemStack.has(DataComponents.CONTAINER)) {
            return false;
        } else if (!super.canPlayerInteractWith(itemStack, player)) {
            return false;
        } else if (!this.interactionPermissions.allowsPlayerInteractions(player)) {
            return false;
        } else if (!this.matchesRequiredEquipmentSlots(itemStack, player)) {
            return false;
        } else {
            return true;
        }
    }

    private boolean matchesRequiredEquipmentSlots(ItemStack itemStack, Player player) {
        if (player.getAbilities().instabuild || this.equipmentSlots == EquipmentSlotGroup.ANY) {
            return true;
        } else {
            for (EquipmentSlot slot : EQUIPMENT_SLOTS) {
                if (this.equipmentSlots.test(slot)) {
                    ItemStack itemInSlot = player.getItemBySlot(slot);
                    if (itemStack == itemInSlot) {
                        return true;
                    }
                }
            }

            return false;
        }
    }

    @Override
    public SimpleContainer getItemContainer(ItemStack itemStack, boolean isMutable) {
        NonNullList<ItemStack> itemList = NonNullList.withSize(this.getInventorySize(), ItemStack.EMPTY);
        itemStack.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY).copyInto(itemList);
        return ContainerMenuHelper.createListBackedContainer(itemList, (Container container) -> {
            if (isMutable) {
                itemStack.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(itemList));
            } else {
                throw new UnsupportedOperationException();
            }
        });
    }

    @Override
    public TooltipComponent createTooltipImageComponent(ItemStack itemStack, Player player, NonNullList<ItemStack> itemList) {
        return new ItemContentsTooltip(itemList,
                this.getSelectedItem(itemStack),
                this.getGridWidth(itemList.size()),
                this.getGridHeight(itemList.size()),
                this.dyeColor);
    }

    @Override
    public ItemStorageType<?> getType() {
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
