package com.direwolf20.buildinggadgets.common.inv;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.DimensionType;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;
import org.apache.commons.lang3.tuple.Pair;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

/**
 * Responsible for gathering user inventories and providing them to the Inventory Manager
 */
public class UserInventory {
    private final PlayerEntity player;
    private final ItemStack gadget;

    public UserInventory(PlayerEntity player, ItemStack gadget) {
        this.player = player;
        this.gadget = gadget;
    }

    /**
     * Collects any inventories a player might have access to so basically (In this order):
     * 1. Linked Inventory
     * 2. Inventory Items inside
     * 3. Their own inventory
     *
     * @return list of inventories we can manage.
     */
    public Set<IItemHandler> collectAccessible() {
        Set<IItemHandler> collector = new HashSet<>();

        this.linkedInventory().ifPresent(collector::add);
        collector.addAll(this.inventoryItemInventories());
        collector.add(new InvWrapper(player.inventory));

        return collector;
    }

    /**
     * Collects a list of items inside the players inventory which inventories
     * attached to them via the capability provider.
     */
    public Set<IItemHandler> inventoryItemInventories() {
        Set<IItemHandler> collector = new HashSet<>();
        InvWrapper inventory = new InvWrapper(player.inventory);

        for (int i = 0; i < inventory.getSlots(); i++) {
            ItemStack stackInSlot = inventory.getStackInSlot(i);
            if (stackInSlot.isEmpty()) {
                continue;
            }

            stackInSlot.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
                    .ifPresent(collector::add);
        }

        return collector;
    }

    /**
     * Convert a linked Block Pos into a tile entity, then into a inventory :D
     * @return optional if the inventory is valid.
     */
    public Optional<IItemHandler> linkedInventory() {
        Optional<Pair<RegistryKey<DimensionType>, BlockPos>> inventory = LinkedInventory.get(this.gadget);
        if (!inventory.isPresent()) {
            return Optional.empty();
        }

        TileEntity tileEntity = this.player.world.getTileEntity(inventory.get().getRight());
        if (tileEntity == null) {
            return Optional.empty();
        }

        return tileEntity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
                .map(Optional::of)
                .orElse(Optional.empty());
    }
}
