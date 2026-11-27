package org.skysongdev.skysongCrafting.api;

import org.bukkit.block.Block;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * A placed workstation as other plugins see it, at the moment it was read. It is a copy: it doesn't
 * change when the workstation does.
 * <p>
 * A workstation is identified by its block: the world name and block coordinates. There is at most
 * one workstation per block.
 *
 * @param world        the world's name
 * @param x            block x
 * @param y            block y
 * @param z            block z
 * @param typeId       its type, the key in {@code workstations.yml}, such as {@code test_station}
 * @param typeName     the type's display name ({@code name} in {@code workstations.yml})
 * @param owner        the player who owns it
 * @param members      the players the owner let use it, besides the owner
 * @param publicAccess whether anyone may use it
 */
public record WorkstationInfo(String world, int x, int y, int z, String typeId, String typeName, UUID owner,
                              List<UUID> members, boolean publicAccess) {
    public WorkstationInfo {
        Objects.requireNonNull(world, "world");
        Objects.requireNonNull(typeId, "typeId");
        Objects.requireNonNull(typeName, "typeName");
        Objects.requireNonNull(owner, "owner");
        members = List.copyOf(members);
    }

    /** @return whether this workstation is on that block */
    public boolean isAt(Block block) {
        return isAt(block.getWorld().getName(), block.getX(), block.getY(), block.getZ());
    }

    /** @return whether this workstation is on the block at those coordinates */
    public boolean isAt(String world, int x, int y, int z) {
        return this.world.equals(world) && this.x == x && this.y == y && this.z == z;
    }

    /**
     * @return whether the player may use it by SkysongCrafting's own rules: it is public, or they own
     *         it or are a member (bypass permissions and {@link WorkstationAccessEvent} not counted)
     */
    public boolean canUse(UUID player) {
        return publicAccess || owner.equals(player) || members.contains(player);
    }
}
