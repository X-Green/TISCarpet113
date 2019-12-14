package carpet.utils;

import net.minecraft.inventory.IInventory;

import javax.annotation.Nullable;

public interface OptimizedInventory extends IInventory
{
    @Nullable
    InventoryOptimizer getOptimizer();
}
