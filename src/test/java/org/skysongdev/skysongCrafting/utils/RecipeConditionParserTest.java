package org.skysongdev.skysongCrafting.utils;

import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;
import org.skysongdev.skysongCrafting.data.recipe.RecipeCondition;

import java.io.File;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RecipeConditionParserTest {

    private final List<String> warnings = new ArrayList<>();

    private List<RecipeCondition> parse(String yaml) {
        return RecipeConditionParser.parse(new File("test_recipe.yaml"), YamlConfiguration.loadConfiguration(new StringReader(yaml)), warnings::add);
    }

    @Test
    void absentKeyMeansNoConditions() {
        List<RecipeCondition> conditions = parse("name: x");
        assertNotNull(conditions);
        assertTrue(conditions.isEmpty());
        assertTrue(warnings.isEmpty());
    }

    @Test
    void validConditionsLoadInOrder() {
        List<RecipeCondition> conditions = parse("""
                conditions:
                  - item: "marked:0"
                    check: "tag:layer_count"
                    operator: "<"
                    value: 8
                  - item: "marked:1"
                    check: material
                    operator: "=="
                    value: INK_SAC
                """);
        assertNotNull(conditions, warnings.toString());
        assertEquals(2, conditions.size());
        assertEquals(RecipeCondition.Operator.LESS, conditions.get(0).getOperator());
        assertEquals("8", conditions.get(0).getValue());
        assertEquals("marked:0 tag:layer_count < 8", conditions.get(0).describe());
        assertEquals("material", conditions.get(1).getCheck());
    }

    @Test
    void missingKeyNamesTheEntry() {
        assertNull(parse("""
                conditions:
                  - item: "marked:0"
                    operator: "<"
                    value: 8
                """));
        assertEquals(1, warnings.size());
        assertTrue(warnings.get(0).contains("condition 1"), warnings.get(0));
        assertTrue(warnings.get(0).contains("missing 'check'"), warnings.get(0));
        assertTrue(warnings.get(0).contains("test_recipe.yaml"), warnings.get(0));
    }

    @Test
    void badOperatorListsTheOptions() {
        assertNull(parse("""
                conditions:
                  - item: "marked:0"
                    check: "tag:layer_count"
                    operator: "=>"
                    value: 8
                """));
        assertTrue(warnings.get(0).contains("expected one of"), warnings.get(0));
    }

    @Test
    void pivotIsNotAllowedInRecipeConditions() {
        assertNull(parse("""
                conditions:
                  - item: pivot
                    check: amount
                    operator: ">"
                    value: 1
                """));
        assertTrue(warnings.get(0).contains("pivot"), warnings.get(0));
    }

    @Test
    void oldSemicolonFormatIsRefused() {
        assertNull(parse("""
                conditions:
                  - "0;tag!layer_count;<;8"
                """));
        assertEquals(1, warnings.size());
    }
}
