package com.direwolf20.buildinggadgets.common.inv;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * This class is designed to manage the Insert and Extraction of Items from a players inventories.
 */
public class InventoryManagement {
    public static List<ItemStack> extract(PlayerEntity player, ItemStack gadget, List<ItemStack> items) {
        Set<IItemHandler> handlers = new UserInventory(player, gadget).collectAccessible();

        List<ItemStack> failedStacks = new ArrayList<>();
        for (IItemHandler handler : handlers) {
            for (int i = 0; i < handler.getSlots(); i++) {
                ItemStack providerStack = handler.getStackInSlot(i);
                for (ItemStack requiredStack : items) {
                    if (!requiredStack.isItemEqualIgnoreDurability(providerStack)) {
                        continue;
                    }

                    ItemStack stack = handler.extractItem(i, requiredStack.getCount(), false);
                    if (!stack.isEmpty()) {
                        failedStacks.add(stack.copy()); // copy to modify original
                    }
                }
            }
        }

        // Only return items which didn
        return failedStacks;
    }

    public static boolean insert(PlayerEntity player) {
//        UserInventory inventory = new UserInventory(player);
        return true;
    }

    /**
     *
     */
    public static int count(ItemStack stack) {
        return 0;
    }
}
