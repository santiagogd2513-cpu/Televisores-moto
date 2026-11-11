package com.santiagogd.holobuilder.builder;

import com.santiagogd.holobuilder.HoloBuilder;
import com.santiagogd.holobuilder.compat.ForgematicaBridge;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;

/**
 * Build / undo manager.
 * Uses ForgematicaBridge to read the selected hologram placement and place blocks
 * with correct BlockStates, consuming inventory items in survival.
 */
public class BuildManager {

    private static final List<PlacedBlock> LAST_BUILD = new ArrayList<>();

    public static void buildSelectedPlacement(Minecraft mc) {
        if (mc == null || mc.player == null || mc.level == null) return;

        Player player = mc.player;
        Level level = mc.level;

        if (!ForgematicaBridge.isAvailable()) {
            player.displayClientMessage(Component.literal("[HoloBuilder] Forgematica / Litematica no está instalado."), false);
            return;
        }

        List<BlockPosState> toPlace = ForgematicaBridge.getSelectedPlacementBlocks();
        String placementName = ForgematicaBridge.getSelectedPlacementName();

        if (toPlace == null || toPlace.isEmpty()) {
            player.displayClientMessage(Component.literal("[HoloBuilder] No hay holograma / placement activo."), false);
            player.displayClientMessage(Component.literal("[HoloBuilder] Carga un schematic en Forgematica y selecciona el placement (M → Schematic Placements)."), false);
            return;
        }

        if (placementName != null) {
            player.displayClientMessage(Component.literal("[HoloBuilder] Construyendo: " + placementName + " (" + toPlace.size() + " bloques)"), false);
        }

        LAST_BUILD.clear();
        int placed = 0;
        int skippedNoItem = 0;
        int skippedAlready = 0;

        boolean creative = player.getAbilities().instabuild;

        for (BlockPosState entry : toPlace) {
            BlockPos pos = entry.pos();
            BlockState desired = entry.state();

            if (desired == null || desired.isAir()) continue;

            BlockState current = level.getBlockState(pos);
            if (current.equals(desired)) {
                skippedAlready++;
                continue;
            }

            if (!creative) {
                ItemStack needed = new ItemStack(desired.getBlock().asItem());
                if (needed.isEmpty()) {
                    skippedNoItem++;
                    continue;
                }
                if (!consumeOne(player.getInventory(), needed)) {
                    skippedNoItem++;
                    continue;
                }
            }

            // Client-side setBlock for singleplayer / integrated.
            // On dedicated multiplayer this only predicts; real placement needs server authority.
            level.setBlock(pos, desired, 3);
            LAST_BUILD.add(new PlacedBlock(pos, current, desired));
            placed++;
        }

        player.displayClientMessage(Component.literal(
                "[HoloBuilder] Colocados: " + placed +
                " | Ya correctos: " + skippedAlready +
                " | Sin ítem: " + skippedNoItem
        ), false);
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
            level.setBlock(pb.pos(), pb.previous(), 3);
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

    public record BlockPosState(BlockPos pos, BlockState state) {}
    public record PlacedBlock(BlockPos pos, BlockState previous, BlockState placed) {}
}
