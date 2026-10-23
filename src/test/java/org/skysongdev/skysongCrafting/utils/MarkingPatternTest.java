package org.skysongdev.skysongCrafting.utils;

import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;

import java.io.StringReader;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MarkingPatternTest {

    private static YamlConfiguration yaml(String text) {
        return YamlConfiguration.loadConfiguration(new StringReader(text));
    }

    @Test
    void rowsAreFlattenedInOrder() {
        List<Boolean> marks = YamlParsingUtils.readMarkingPattern(yaml("""
                marking_pattern:
                  - [ false, true ]
                  - [ true, false ]
                """));
        assertEquals(List.of(false, true, true, false), marks);
    }

    @Test
    void flatListStillWorks() {
        List<Boolean> marks = YamlParsingUtils.readMarkingPattern(yaml("""
                marking_pattern: [ true, false, true ]
                """));
        assertEquals(List.of(true, false, true), marks);
    }

    @Test
    void missingKeyGivesEmptyList() {
        assertTrue(YamlParsingUtils.readMarkingPattern(yaml("name: x")).isEmpty());
    }
}
