package org.skysongdev.skysongCrafting.api;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * A workstation was created with {@code /sc workbench create}. Fired on the main thread after it was
 * saved. It can't be cancelled.
 * <p>
 * When the block already held a workstation, that one is replaced: a {@link WorkstationRemoveEvent}
 * with {@link WorkstationRemoveEvent.Cause#REPLACED} comes first.
 */
public final class WorkstationCreateEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();

    private final WorkstationInfo workstation;

    public WorkstationCreateEvent(@NotNull WorkstationInfo workstation) {
        this.workstation = Objects.requireNonNull(workstation, "workstation");
    }

    /** @return the workstation that was created */
    public @NotNull WorkstationInfo getWorkstation() {
        return workstation;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    public static @NotNull HandlerList getHandlerList() {
        return HANDLERS;
    }
}
