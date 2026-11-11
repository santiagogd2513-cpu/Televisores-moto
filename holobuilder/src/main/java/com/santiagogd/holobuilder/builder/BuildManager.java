package com.santiagogd.holobuilder.builder;

import com.santiagogd.holobuilder.HoloBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.Blocks;

import java.util.ArrayList;
import java.util.List;

/**
 * Simple build / undo manager.
 *
 * Full integration with Forgematica placement data requires reflection
 * against Forgematica/Litematica internal classes. This class provides
 * the structure + inventory consumption + undo history.
 *
 * When Forgematica is present, the real placement blocks can be read
 * via reflection (see tryGetPlacementBlocks).
 */
public class BuildManager {

    private static final List<PlacedBlock> LAST_BUILD = new ArrayList<>();

    public static void buildSelectedPlacement(Minecraft mc) {
        if (mc == null || mc.player == null || mc.level == null) return;

        Player player = mc.player;
        Level level = mc.level;

        List<BlockPosState> toPlace = tryGetPlacementBlocks();

        if (toPlace == null || toPlace.isEmpty()) {
            player.displayClientMessage(Component.literal("[HoloBuilder] No hay holograma / placement activo de Forgematica."), false);
            player.displayClientMessage(Component.literal("[HoloBuilder] Carga un schematic en Forgematica y selecciona el placement."), false);
            return;
        }

        LAST_BUILD.clear();
        int placed = 0;
        int skipped = 0;

        boolean creative = player.getAbilities().instabuild;

        for (BlockPosState entry : toPlace) {
            BlockPos pos = entry.pos;
            BlockState desired = entry.state;

            if (desired == null || desired.isAir()) continue;

            BlockState current = level.getBlockState(pos);
            if (current.equals(desired)) {
                skipped++;
                continue;
            }

            if (!creative) {
                ItemStack needed = new ItemStack(desired.getBlock().asItem());
                if (needed.isEmpty()) {
                    skipped++;
                    continue;
                }
                if (!consumeOne(player.getInventory(), needed)) {
                    skipped++;
                    continue;
                }
            }

            // Client-side prediction only for now.
            // Real placement in survival multiplayer needs server-side handling or packets.
            level.setBlock(pos, desired, 3);
            LAST_BUILD.add(new PlacedBlock(pos, current, desired));
            placed++;
        }

        player.displayClientMessage(Component.literal("[HoloBuilder] Colocados: " + placed + " | Saltados: " + skipped), false);
    }

    public static void undoLastBuild(Minecraft mc) {
        if (mc == null || mc.player == null || mc.level == null) return;

        if (LAST_BUILD.isEmpty()) {
            mc.player.displayClientMessage(Component.literal("[HoloBuilder] No hay nada que deshacer."), false);
            return;
        }

        Level level = mc.level;
        int restored = 0;

        for (int i = LAST_BUILD.size() - 1; i >= 0; i--) {
            PlacedBlock pb = LAST_BUILD.get(i);
            level.setBlock(pb.pos, pb.previous, 3);
            restored++;
        }

        LAST_BUILD.clear();
        mc.player.displayClientMessage(Component.literal("[HoloBuilder] Deshechos: " + restored + " bloques."), false);
    }

    private static boolean consumeOne(Inventory inv, ItemStack needed) {
        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack stack = inv.getItem(i);
            if (!stack.isEmpty() && ItemStack.isSameItemSameTags(stack, needed)) {
                stack.shrink(1);
                if (stack.isEmpty()) {
                    inv.setItem(i, ItemStack.EMPTY);
                }
                return true;
            }
        }
        return false;
    }

    /**
     * Attempts to read the currently selected Forgematica / Litematica placement.
     * Returns null if Forgematica is not present or no placement is selected.
     *
     * This is intentionally defensive (reflection) so the mod still loads without Forgematica.
     */
    private static List<BlockPosState> tryGetPlacementBlocks() {
        try {
            // Placeholder: real implementation would reflect into
            // fi.dy.masa.litematica.data.DataManager / SchematicPlacementManager
            // and walk the selected SchematicPlacement regions + block states.
            //
            // For now we return an empty list so the menu works and messages are clear.
            // Next step: implement the reflection bridge once we confirm the exact
            // Forgematica package names on 1.20.1.
            return List.of();
        } catch (Throwable t) {
            HoloBuilder.LOGGER.warn("Could not read Forgematica placement", t);
            return List.of();
        }
    }

    public record BlockPosState(BlockPos pos, BlockState state) {}
    public record PlacedBlock(BlockPos pos, BlockState previous, BlockState placed) {}
}
