package org.skysongdev.skysongCrafting.api;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * A player tried to open a private workstation they may not use by SkysongCrafting's own rules: they
 * aren't its owner or a member and have no bypass permission. Fired on the main thread before they
 * are refused. A listener that calls {@link #setAllowed(boolean) setAllowed(true)} lets them open it.
 * <p>
 * It isn't fired for players SkysongCrafting already lets in, so listeners can only add access, never
 * take it away.
 */
public final class WorkstationAccessEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();

    private final Player player;
    private final WorkstationInfo workstation;
    private boolean allowed;

    public WorkstationAccessEvent(@NotNull Player player, @NotNull WorkstationInfo workstation) {
        this.player = Objects.requireNonNull(player, "player");
        this.workstation = Objects.requireNonNull(workstation, "workstation");
    }

    /** @return the player trying to open it */
    public @NotNull Player getPlayer() {
        return player;
    }

    /** @return the workstation they are trying to open */
    public @NotNull WorkstationInfo getWorkstation() {
        return workstation;
    }

    /** @return whether a listener let them in; false until one does */
    public boolean isAllowed() {
        return allowed;
    }

    /** Lets the player open the workstation, or takes back another listener's permission. */
    public void setAllowed(boolean allowed) {
        this.allowed = allowed;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    public static @NotNull HandlerList getHandlerList() {
        return HANDLERS;
    }
}
