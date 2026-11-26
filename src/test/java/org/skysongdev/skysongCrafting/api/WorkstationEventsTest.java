package org.skysongdev.skysongCrafting.api;

import org.bukkit.entity.Player;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/** The workstation events carry what they were given, and access starts refused. */
class WorkstationEventsTest {
    private static final WorkstationInfo BENCH =
            new WorkstationInfo("world", 1, 2, 3, "anvil", "Anvil", UUID.randomUUID(), List.of(), false);

    /** A Player that does nothing: the event only holds on to it. */
    private static Player player() {
        return (Player) Proxy.newProxyInstance(Player.class.getClassLoader(), new Class<?>[]{Player.class},
                (proxy, method, args) -> method.getName().equals("hashCode") ? 0 : null);
    }

    @Test
    void accessIsRefusedUntilAListenerAllowsIt() {
        Player player = player();
        WorkstationAccessEvent event = new WorkstationAccessEvent(player, BENCH);

        assertFalse(event.isAllowed());
        event.setAllowed(true);
        assertTrue(event.isAllowed());
        assertSame(player, event.getPlayer());
        assertEquals(BENCH, event.getWorkstation());
    }

    @Test
    void removeEventKeepsTheCause() {
        WorkstationRemoveEvent event = new WorkstationRemoveEvent(BENCH, WorkstationRemoveEvent.Cause.BROKEN);

        assertEquals(BENCH, event.getWorkstation());
        assertEquals(WorkstationRemoveEvent.Cause.BROKEN, event.getCause());
        assertThrows(NullPointerException.class, () -> new WorkstationRemoveEvent(BENCH, null));
    }

    @Test
    void createEventKeepsTheWorkstation() {
        assertEquals(BENCH, new WorkstationCreateEvent(BENCH).getWorkstation());
    }
}
