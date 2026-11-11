package com.santiagogd.holobuilder.compat;

import com.santiagogd.holobuilder.HoloBuilder;
import com.santiagogd.holobuilder.builder.BuildManager.BlockPosState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Reflection bridge to Forgematica / Litematica (package fi.dy.masa.litematica).
 *
 * Reads the currently selected SchematicPlacement and extracts BlockPos + BlockState
 * pairs so HoloBuilder can place them (with correct states).
 *
 * Soft dependency: the mod still loads if Forgematica is missing.
 */
public final class ForgematicaBridge {

    private static Boolean available = null;

    private ForgematicaBridge() {}

    public static boolean isAvailable() {
        if (available == null) {
            try {
                Class.forName("fi.dy.masa.litematica.data.DataManager");
                available = true;
            } catch (Throwable t) {
                available = false;
            }
        }
        return available;
    }

    /**
     * Returns the blocks of the currently selected placement, or empty list.
     * Each entry has world BlockPos and the desired BlockState from the schematic.
     */
    public static List<BlockPosState> getSelectedPlacementBlocks() {
        List<BlockPosState> result = new ArrayList<>();
        if (!isAvailable()) {
            return result;
        }

        try {
            Object placement = getSelectedPlacement();
            if (placement == null) {
                HoloBuilder.LOGGER.info("[HoloBuilder] No selected placement in Forgematica");
                return result;
            }

            // Prefer walking schematic regions if possible
            List<BlockPosState> fromRegions = tryReadFromSchematicRegions(placement);
            if (!fromRegions.isEmpty()) {
                return fromRegions;
            }

            // Fallback: try a simpler path if region walking failed
            HoloBuilder.LOGGER.warn("[HoloBuilder] Could not walk schematic regions; placement found but no blocks extracted");
            return result;

        } catch (Throwable t) {
            HoloBuilder.LOGGER.error("[HoloBuilder] Error reading Forgematica placement", t);
            return result;
        }
    }

    public static String getSelectedPlacementName() {
        try {
            Object placement = getSelectedPlacement();
            if (placement == null) return null;
            Method getName = placement.getClass().getMethod("getName");
            Object name = getName.invoke(placement);
            return name != null ? name.toString() : null;
        } catch (Throwable t) {
            return null;
        }
    }

    private static Object getSelectedPlacement() throws Exception {
        Class<?> dataManager = Class.forName("fi.dy.masa.litematica.data.DataManager");
        Method getManager = dataManager.getMethod("getSchematicPlacementManager");
        Object manager = getManager.invoke(null);
        if (manager == null) return null;

        Method getSelected = manager.getClass().getMethod("getSelectedSchematicPlacement");
        return getSelected.invoke(manager);
    }

    /**
     * Walks enabled sub-regions of the placement and reads block states from the schematic container.
     * This is the most accurate path for correct BlockStates (orientation, half, etc.).
     */
    @SuppressWarnings("unchecked")
    private static List<BlockPosState> tryReadFromSchematicRegions(Object placement) {
        List<BlockPosState> out = new ArrayList<>();
        try {
            // placement.getSchematic()
            Method getSchematic = placement.getClass().getMethod("getSchematic");
            Object schematic = getSchematic.invoke(placement);
            if (schematic == null) return out;

            // placement.getOrigin()
            Method getOrigin = placement.getClass().getMethod("getOrigin");
            Object originObj = getOrigin.invoke(placement);
            if (!(originObj instanceof BlockPos origin)) return out;

            // placement.getEnabledSubRegions() or getAllSubRegionsUnsorted() / getSubRegionBoxes
            Collection<?> subRegions = null;
            for (String methodName : new String[]{"getEnabledSubRegions", "getAllSubRegionsUnsorted", "getSubRegionPlacements"}) {
                try {
                    Method m = placement.getClass().getMethod(methodName);
                    Object res = m.invoke(placement);
                    if (res instanceof Collection<?> col) {
                        subRegions = col;
                        break;
                    }
                } catch (NoSuchMethodException ignored) {}
            }

            if (subRegions == null || subRegions.isEmpty()) {
                // Try reading full schematic size as one region
                return tryReadFullSchematic(schematic, placement, origin);
            }

            for (Object sub : subRegions) {
                out.addAll(readSubRegion(schematic, placement, origin, sub));
            }

            return out;
        } catch (Throwable t) {
            HoloBuilder.LOGGER.debug("[HoloBuilder] Region walk failed", t);
            return out;
        }
    }

    private static List<BlockPosState> tryReadFullSchematic(Object schematic, Object placement, BlockPos origin) {
        List<BlockPosState> out = new ArrayList<>();
        try {
            // LitematicaSchematic has getAreas() / getSubRegionCount / getBlockContainer etc.
            // Try getAreas() map
            Method getAreas = null;
            for (String name : new String[]{"getAreas", "getRegionContainers", "getSubRegionContainers"}) {
                try {
                    getAreas = schematic.getClass().getMethod(name);
                    break;
                } catch (NoSuchMethodException ignored) {}
            }
            if (getAreas == null) return out;

            Object areas = getAreas.invoke(schematic);
            if (!(areas instanceof java.util.Map<?, ?> map)) return out;

            for (Object regionObj : map.values()) {
                out.addAll(readContainerBlocks(regionObj, origin, placement));
            }
        } catch (Throwable t) {
            HoloBuilder.LOGGER.debug("[HoloBuilder] Full schematic read failed", t);
        }
        return out;
    }

    private static List<BlockPosState> readSubRegion(Object schematic, Object placement, BlockPos origin, Object subRegion) {
        List<BlockPosState> out = new ArrayList<>();
        try {
            // SubRegionPlacement has getName(), getPos(), getSize(), etc.
            String regionName = null;
            try {
                Method getName = subRegion.getClass().getMethod("getName");
                Object n = getName.invoke(subRegion);
                if (n != null) regionName = n.toString();
            } catch (NoSuchMethodException ignored) {}

            // Get container from schematic by region name
            Object container = null;
            if (regionName != null) {
                for (String methodName : new String[]{"getBlockContainer", "getContainer", "getSubRegionContainer"}) {
                    try {
                        Method m = schematic.getClass().getMethod(methodName, String.class);
                        container = m.invoke(schematic, regionName);
                        if (container != null) break;
                    } catch (NoSuchMethodException ignored) {}
                }
            }

            if (container == null) {
                // Try getAreas map entry
                try {
                    Method getAreas = schematic.getClass().getMethod("getAreas");
                    Object areas = getAreas.invoke(schematic);
                    if (areas instanceof java.util.Map<?, ?> map && regionName != null) {
                        container = map.get(regionName);
                    }
                } catch (Throwable ignored) {}
            }

            if (container != null) {
                out.addAll(readContainerBlocks(container, origin, placement));
            }
        } catch (Throwable t) {
            HoloBuilder.LOGGER.debug("[HoloBuilder] SubRegion read failed", t);
        }
        return out;
    }

    /**
     * Reads blocks from an ILitematicaBlockStateContainer-like object.
     * Expects getSize() -> Vec3i and get(x,y,z) -> BlockState (or IBlockState).
     */
    private static List<BlockPosState> readContainerBlocks(Object container, BlockPos origin, Object placement) {
        List<BlockPosState> out = new ArrayList<>();
        try {
            // size
            Object sizeObj = null;
            for (String name : new String[]{"getSize", "getDimensions"}) {
                try {
                    Method m = container.getClass().getMethod(name);
                    sizeObj = m.invoke(container);
                    if (sizeObj != null) break;
                } catch (NoSuchMethodException ignored) {}
            }
            if (sizeObj == null) return out;

            int sizeX, sizeY, sizeZ;
            if (sizeObj instanceof BlockPos bp) {
                sizeX = bp.getX();
                sizeY = bp.getY();
                sizeZ = bp.getZ();
            } else {
                // Vec3i-like
                Method getX = sizeObj.getClass().getMethod("getX");
                Method getY = sizeObj.getClass().getMethod("getY");
                Method getZ = sizeObj.getClass().getMethod("getZ");
                sizeX = ((Number) getX.invoke(sizeObj)).intValue();
                sizeY = ((Number) getY.invoke(sizeObj)).intValue();
                sizeZ = ((Number) getZ.invoke(sizeObj)).intValue();
            }

            // Clamp to avoid huge hangs on massive schematics in first version
            final int MAX_BLOCKS = 200_000;
            long volume = (long) Math.abs(sizeX) * Math.abs(sizeY) * Math.abs(sizeZ);
            if (volume > MAX_BLOCKS) {
                HoloBuilder.LOGGER.warn("[HoloBuilder] Schematic region too large ({} blocks). Skipping for safety.", volume);
                return out;
            }

            Method getBlock = null;
            for (String name : new String[]{"get", "getBlockState", "getAt"}) {
                try {
                    getBlock = container.getClass().getMethod(name, int.class, int.class, int.class);
                    break;
                } catch (NoSuchMethodException ignored) {}
            }
            if (getBlock == null) return out;

            // Rotation / mirror from placement (optional, best-effort)
            Object rotation = null;
            Object mirror = null;
            try {
                Method getRot = placement.getClass().getMethod("getRotation");
                rotation = getRot.invoke(placement);
            } catch (Throwable ignored) {}
            try {
                Method getMir = placement.getClass().getMethod("getMirror");
                mirror = getMir.invoke(placement);
            } catch (Throwable ignored) {}

            int sx = Math.abs(sizeX);
            int sy = Math.abs(sizeY);
            int sz = Math.abs(sizeZ);

            for (int y = 0; y < sy; y++) {
                for (int z = 0; z < sz; z++) {
                    for (int x = 0; x < sx; x++) {
                        Object stateObj = getBlock.invoke(container, x, y, z);
                        if (stateObj == null) continue;

                        BlockState state = null;
                        if (stateObj instanceof BlockState bs) {
                            state = bs;
                        } else {
                            // Older mappings might wrap it
                            try {
                                Method getBlockState = stateObj.getClass().getMethod("getBlockState");
                                Object inner = getBlockState.invoke(stateObj);
                                if (inner instanceof BlockState bs2) state = bs2;
                            } catch (Throwable ignored) {}
                        }

                        if (state == null || state.isAir() || state.getBlock() == Blocks.AIR) continue;

                        // Apply rotation/mirror to state if possible
                        state = applyTransform(state, rotation, mirror);

                        // World position (basic, without full region offset for first version)
                        BlockPos worldPos = origin.offset(x, y, z);
                        // Better: apply placement rotation to offset
                        worldPos = transformOffset(origin, x, y, z, rotation, mirror);

                        out.add(new BlockPosState(worldPos, state));
                    }
                }
            }
        } catch (Throwable t) {
            HoloBuilder.LOGGER.debug("[HoloBuilder] Container read failed", t);
        }
        return out;
    }

    private static BlockState applyTransform(BlockState state, Object rotation, Object mirror) {
        try {
            if (rotation != null) {
                // net.minecraft.world.level.block.Rotation
                Method rotate = state.getClass().getMethod("rotate", rotation.getClass());
                Object rotated = rotate.invoke(state, rotation);
                if (rotated instanceof BlockState rs) state = rs;
            }
            if (mirror != null) {
                Method mirrorM = state.getClass().getMethod("mirror", mirror.getClass());
                Object mirrored = mirrorM.invoke(state, mirror);
                if (mirrored instanceof BlockState ms) state = ms;
            }
        } catch (Throwable ignored) {}
        return state;
    }

    private static BlockPos transformOffset(BlockPos origin, int x, int y, int z, Object rotation, Object mirror) {
        // Simple path: no transform of coordinates beyond origin offset.
        // Full correct transform needs the same math Litematica uses (mirror then rotate).
        // This is good enough for unrotated placements; rotated ones may need adjustment later.
        return origin.offset(x, y, z);
    }
}
