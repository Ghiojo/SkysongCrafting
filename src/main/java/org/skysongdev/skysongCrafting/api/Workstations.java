package org.skysongdev.skysongCrafting.api;

import org.bukkit.block.Block;

import java.util.List;
import java.util.Optional;

/**
 * Reads SkysongCrafting's placed workstations. Get it with
 * {@code Bukkit.getServicesManager().load(Workstations.class)} and use it on the main thread.
 * Everything it returns is a copy taken when it was called. Read-only: workstations are created and
 * removed by {@code /sc workbench} and by breaking their block.
 */
public interface Workstations {
    /** @return the workstation on the block at those coordinates; empty when there is none */
    Optional<WorkstationInfo> at(String world, int x, int y, int z);

    /** @return the workstation on that block; empty when there is none */
    default Optional<WorkstationInfo> at(Block block) {
        return at(block.getWorld().getName(), block.getX(), block.getY(), block.getZ());
    }

    /** @return every placed workstation, in no particular order */
    List<WorkstationInfo> all();
}
