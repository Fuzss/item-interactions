package fuzs.iteminteractions.api.v1.provider;

import net.minecraft.core.HolderSet;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import org.jetbrains.annotations.Nullable;

public class BlockEntityViewProvider extends BlockEntityProvider {

    public BlockEntityViewProvider(BlockEntityType<?> blockEntityType, int inventoryWidth, int inventoryHeight) {
        super(blockEntityType, inventoryWidth, inventoryHeight);
    }

    public BlockEntityViewProvider(ResourceLocation blockEntityTypeId, int inventoryWidth, int inventoryHeight) {
        super(blockEntityTypeId, inventoryWidth, inventoryHeight);
    }

    public BlockEntityViewProvider(BlockEntityType<?> blockEntityType, int inventoryWidth, int inventoryHeight, @Nullable DyeColor dyeColor) {
        super(blockEntityType, inventoryWidth, inventoryHeight, dyeColor);
    }

    public BlockEntityViewProvider(ResourceLocation blockEntityTypeId, int inventoryWidth, int inventoryHeight, @Nullable DyeColor dyeColor) {
        super(blockEntityTypeId, inventoryWidth, inventoryHeight, dyeColor);
    }

    @Override
    public AbstractItemContainerProvider disallowedItems(HolderSet<Item> disallowedItems) {
        return this;
    }

    @Override
    public SimpleItemContainerProvider filterContainerItems() {
        return this;
    }

    @Override
    public BlockEntityProvider anyGameMode() {
        return this;
    }

    @Override
    public SimpleItemContainerProvider equipmentSlot(@Nullable EquipmentSlot equipmentSlot) {
        return this;
    }

    @Override
    public boolean allowsPlayerInteractions(ItemStack containerStack, Player player) {
        return false;
    }
}
