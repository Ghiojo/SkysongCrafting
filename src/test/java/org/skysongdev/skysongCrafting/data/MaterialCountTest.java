package org.skysongdev.skysongCrafting.data;

import org.junit.jupiter.api.Test;
import org.skysongdev.skysongCrafting.utils.Utils;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Shapeless recipes are matched by looking up a list of MaterialCounts in a HashMap, so these
 * invariants (case-insensitive equality, stable alphabetical order, merged duplicates) are what
 * make a grid's contents find the recipe that was registered for them.
 */
class MaterialCountTest {

    @Test
    void equalityIgnoresIdCase() {
        assertEquals(new MaterialCount("Iron_Ingot", 2), new MaterialCount("iron_ingot", 2));
        assertEquals(new MaterialCount("Iron_Ingot", 2).hashCode(), new MaterialCount("iron_ingot", 2).hashCode());
        assertNotEquals(new MaterialCount("iron_ingot", 2), new MaterialCount("iron_ingot", 3));
    }

    @Test
    void inputListMergesDuplicatesAndSortsAlphabetically() {
        List<MaterialCount> result = Utils.Instance().inputToMaterialCountList(new ArrayList<>(List.of(
                "oak_log", "", "iron_ingot*2", "Iron_Ingot", "   ", "oak_log")));

        assertEquals(List.of(new MaterialCount("iron_ingot", 3), new MaterialCount("oak_log", 2)), result);
    }

    @Test
    void invalidMultiplierFallsBackToOne() {
        List<MaterialCount> result = Utils.Instance().inputToMaterialCountList(new ArrayList<>(List.of("dye*lots", "dye*0")));
        assertEquals(List.of(new MaterialCount("dye", 2)), result);
    }

    @Test
    void gridOrderDoesNotMatter() {
        List<MaterialCount> a = Utils.Instance().inputToMaterialCountList(new ArrayList<>(List.of("a", "b", "", "c")));
        List<MaterialCount> b = Utils.Instance().inputToMaterialCountList(new ArrayList<>(List.of("", "c", "b", "a")));
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }
}
