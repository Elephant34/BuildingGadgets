package com.direwolf20.buildinggadgets.common.inv;

import com.direwolf20.buildinggadgets.common.items.AbstractGadget;
import com.direwolf20.buildinggadgets.common.util.helpers.VectorHelper;
import com.direwolf20.buildinggadgets.common.util.lang.MessageTranslation;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.DimensionType;
import net.minecraftforge.items.CapabilityItemHandler;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Optional;

/**
 * Stores and serves the remote inventory based on an item and a player.
 */
public class LinkedInventory {
    public static final String LINKED_INVENTORY_KEY = "linked_inventory";

    public static void link(ItemStack stack, PlayerEntity player) {
        BlockRayTraceResult lookingAt = VectorHelper.getLookingAt(player, RayTraceContext.FluidMode.NONE);
        TileEntity tileEntity = player.world.getTileEntity(lookingAt.getPos());

        if (tileEntity == null) {
            return;
        }

        // Be sure we're not about to write it over it's original one
        Optional<Pair<RegistryKey<DimensionType>, BlockPos>> linkedInventory = LinkedInventory.get(stack);
        if (linkedInventory.isPresent()) {
            // If we're looking at the same block pos in the same dim then remove the link
            if (linkedInventory.get().getKey().equals(player.world.getDimensionRegistryKey()) && lookingAt.getPos().equals(linkedInventory.get().getValue())) {
                stack.getOrCreateTag().remove(LINKED_INVENTORY_KEY);
                player.sendStatusMessage(MessageTranslation.UNBOUND_TO_TILE.componentTranslation().formatted(TextFormatting.RED), true);
                return;
            }
        }

        // Write the data to the gadget
        tileEntity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).ifPresent(e -> {
            CompoundNBT compound = stack.getOrCreateTag();

            CompoundNBT linkCompound = new CompoundNBT();
            linkCompound.putString("dimension", player.world.getDimensionRegistryKey().getRegistryName().toString());
            linkCompound.put("pos", NBTUtil.writeBlockPos(lookingAt.getPos()));

            compound.put(LINKED_INVENTORY_KEY, linkCompound);

            player.sendStatusMessage(MessageTranslation.BOUND_TO_TILE.componentTranslation().formatted(TextFormatting.AQUA), true);
        });
    }

    public static Optional<Pair<RegistryKey<DimensionType>, BlockPos>> get(ItemStack stack) {
        if (!stack.getOrCreateTag().contains(LINKED_INVENTORY_KEY)) {
            return Optional.empty();
        }

        CompoundNBT compound = stack.getOrCreateTag().getCompound(LINKED_INVENTORY_KEY);

        // The RegistryKey.of can fail and if it does it'll throw a null.
        try {
            RegistryKey<DimensionType> dimension = RegistryKey.of(Registry.DIMENSION_TYPE_KEY, new ResourceLocation(compound.getString("dimension")));
            return Optional.of(Pair.of(dimension, NBTUtil.readBlockPos(compound.getCompound("pos"))));
        } catch (NullPointerException ignored) {
            return Optional.empty();
        }
    }
}
