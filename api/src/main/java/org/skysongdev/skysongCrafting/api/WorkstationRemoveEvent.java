package org.skysongdev.skysongCrafting.api;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * A workstation is gone. Fired on the main thread after it was removed from the database. It can't
 * be cancelled.
 */
public final class WorkstationRemoveEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();

    /** How a workstation was removed. */
    public enum Cause {
        /** {@code /sc workbench delete}. The block stays. */
        DELETED,
        /** Its block was broken. */
        BROKEN,
        /** {@code /sc workbench create} put a new workstation on its block. */
        REPLACED
    }

    private final WorkstationInfo workstation;
    private final Cause cause;

    public WorkstationRemoveEvent(@NotNull WorkstationInfo workstation, @NotNull Cause cause) {
        this.workstation = Objects.requireNonNull(workstation, "workstation");
        this.cause = Objects.requireNonNull(cause, "cause");
    }

    /** @return the workstation as it was just before it was removed */
    public @NotNull WorkstationInfo getWorkstation() {
        return workstation;
    }

    /** @return how it was removed */
    public @NotNull Cause getCause() {
        return cause;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    public static @NotNull HandlerList getHandlerList() {
        return HANDLERS;
    }
}
