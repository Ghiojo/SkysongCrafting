package org.skysongdev.skysongCrafting.api;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/** The public workstation copy: what other plugins rely on when they read a workstation. */
class WorkstationInfoTest {
    private static final UUID OWNER = UUID.randomUUID();
    private static final UUID MEMBER = UUID.randomUUID();
    private static final UUID STRANGER = UUID.randomUUID();

    private static WorkstationInfo bench(boolean publicAccess, List<UUID> members) {
        return new WorkstationInfo("world", 10, 64, -3, "anvil", "Anvil", OWNER, members, publicAccess);
    }

    @Test
    void membersAreACopy() {
        List<UUID> members = new ArrayList<>(List.of(MEMBER));
        WorkstationInfo info = bench(false, members);

        members.add(STRANGER);

        assertEquals(List.of(MEMBER), info.members());
        assertThrows(UnsupportedOperationException.class, () -> info.members().add(STRANGER));
    }

    @Test
    void ownerAndMembersCanUseAPrivateBench() {
        WorkstationInfo info = bench(false, List.of(MEMBER));

        assertTrue(info.canUse(OWNER));
        assertTrue(info.canUse(MEMBER));
        assertFalse(info.canUse(STRANGER));
    }

    @Test
    void anyoneCanUseAPublicBench() {
        assertTrue(bench(true, List.of()).canUse(STRANGER));
    }

    @Test
    void isAtMatchesWorldAndAllCoordinates() {
        WorkstationInfo info = bench(false, List.of());

        assertTrue(info.isAt("world", 10, 64, -3));
        assertFalse(info.isAt("world_nether", 10, 64, -3));
        assertFalse(info.isAt("world", 10, 65, -3));
    }

    @Test
    void requiresIdentity() {
        assertThrows(NullPointerException.class,
                () -> new WorkstationInfo(null, 0, 0, 0, "anvil", "Anvil", OWNER, List.of(), false));
        assertThrows(NullPointerException.class,
                () -> new WorkstationInfo("world", 0, 0, 0, "anvil", "Anvil", null, List.of(), false));
    }
}
