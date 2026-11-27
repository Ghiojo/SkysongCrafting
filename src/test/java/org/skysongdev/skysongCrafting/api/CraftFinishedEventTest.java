package org.skysongdev.skysongCrafting.api;

import org.bukkit.entity.Player;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/** The craft finished event carries what it was given and can't be changed by listeners. */
class CraftFinishedEventTest {
    private static final WorkstationInfo BENCH =
            new WorkstationInfo("world", 1, 2, 3, "anvil", "Anvil", UUID.randomUUID(), List.of(), false);
    private static final RecipeInfo RECIPE = new RecipeInfo("shaped/iron_sword", "<gold>Iron Sword", "anvil");

    /** A Player that does nothing: the event only holds on to it. */
    private static Player player() {
        return (Player) Proxy.newProxyInstance(Player.class.getClassLoader(), new Class<?>[]{Player.class},
                (proxy, method, args) -> method.getName().equals("hashCode") ? 0 : null);
    }

    @Test
    void keepsPlayerWorkstationAndRecipe() {
        Player player = player();
        CraftFinishedEvent event = new CraftFinishedEvent(player, BENCH, RECIPE, List.of(), List.of());

        assertSame(player, event.getPlayer());
        assertEquals(BENCH, event.getWorkstation());
        assertEquals(RECIPE, event.getRecipe());
        assertEquals("shaped/iron_sword", event.getRecipe().id());
        assertTrue(event.getResults().isEmpty());
        assertTrue(event.getIngredients().isEmpty());
    }

    @Test
    void itemListsAreUnmodifiableCopies() {
        List<org.bukkit.inventory.ItemStack> results = new ArrayList<>();
        CraftFinishedEvent event = new CraftFinishedEvent(player(), BENCH, RECIPE, results, new ArrayList<>());

        results.add(null);

        assertTrue(event.getResults().isEmpty());
        assertThrows(UnsupportedOperationException.class, () -> event.getResults().clear());
        assertThrows(UnsupportedOperationException.class, () -> event.getIngredients().clear());
    }

    @Test
    void refusesMissingValues() {
        assertThrows(NullPointerException.class, () -> new CraftFinishedEvent(null, BENCH, RECIPE, List.of(), List.of()));
        assertThrows(NullPointerException.class, () -> new CraftFinishedEvent(player(), null, RECIPE, List.of(), List.of()));
        assertThrows(NullPointerException.class, () -> new CraftFinishedEvent(player(), BENCH, null, List.of(), List.of()));
        assertThrows(NullPointerException.class, () -> new CraftFinishedEvent(player(), BENCH, RECIPE, null, List.of()));
        assertThrows(NullPointerException.class, () -> new RecipeInfo(null, "name", "ANY"));
    }
}
