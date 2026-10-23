package org.skysongdev.skysongCrafting.utils;

import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;
import org.skysongdev.skysongCrafting.data.output.CompositeOutput;
import org.skysongdev.skysongCrafting.data.output.steps.*;

import java.io.File;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Covers the template format described in docs/COMPOSITE_FORMAT.md: a correct template loads
 * with its steps in file order, and every kind of mistake refuses the whole output with a
 * warning that names the step.
 */
class CompositeOutputParserTest {

    private final List<String> warnings = new ArrayList<>();

    private CompositeOutput parse(String yaml) {
        YamlConfiguration config = YamlConfiguration.loadConfiguration(new StringReader(yaml));
        return CompositeOutputParser.parse(new File("test_output.yml"), config, new ArrayList<>(), new ArrayList<>(), warnings::add);
    }

    private String onlyWarning() {
        assertEquals(1, warnings.size(), "expected exactly one warning, got: " + warnings);
        return warnings.get(0);
    }

    @Test
    void fullTemplateLoadsStepsInFileOrder() {
        CompositeOutput output = parse("""
                output_strings: ["<gray>Adds a layer"]
                steps:
                  - type: fetch
                    fetch_type: marked
                    fetch_id: "0"
                  - type: cmd
                    cmd_type: color
                    operation: add
                    index: "marked:0;tag:layer_count"
                    values: ["marked:1;tag:dye_color"]
                  - type: tag
                    operation: add
                    tag_id: layer_count
                    values: ["1"]
                  - type: property
                    operation: set
                    property_id: painted
                  - type: blend
                    tag_id: dye_color
                    sources: ["marked:1", "marked:2"]
                  - type: save
                    amount: 2
                """);

        assertNotNull(output, "template should load; warnings: " + warnings);
        assertTrue(warnings.isEmpty(), "no warnings expected, got: " + warnings);
        List<OutputStep> steps = output.getSteps();
        assertEquals(6, steps.size());
        assertInstanceOf(FetchStep.class, steps.get(0));
        assertInstanceOf(CMDStep.class, steps.get(1));
        assertInstanceOf(TagStep.class, steps.get(2));
        assertInstanceOf(PropertyStep.class, steps.get(3));
        assertInstanceOf(BlendStep.class, steps.get(4));
        assertInstanceOf(SaveStep.class, steps.get(5));
        assertEquals(2, ((SaveStep) steps.get(5)).getAmount());
    }

    @Test
    void stepTypesAndEnumsAreCaseInsensitive() {
        CompositeOutput output = parse("""
                steps:
                  - type: FETCH
                    fetch_type: Generated
                    fetch_id: pavise_shield
                  - type: Save
                """);
        assertNotNull(output, warnings.toString());
        assertEquals(1, ((SaveStep) output.getSteps().get(1)).getAmount(), "amount defaults to 1");
    }

    @Test
    void scalarValueIsAcceptedInPlaceOfList() {
        CompositeOutput output = parse("""
                steps:
                  - type: fetch
                    fetch_type: marked
                    fetch_id: "0"
                  - type: tag
                    operation: set
                    tag_id: layer_count
                    values: "3"
                  - type: save
                """);
        assertNotNull(output, warnings.toString());
    }

    @Test
    void oldKeyedFormatIsRefusedWithHint() {
        assertNull(parse("""
                output_steps:
                  step1:
                    order: 0
                    step_type: fetch
                """));
        assertTrue(onlyWarning().contains("output_steps"), warnings.toString());
    }

    @Test
    void unknownStepTypeNamesTheStep() {
        assertNull(parse("""
                steps:
                  - type: fetch
                    fetch_type: marked
                    fetch_id: "0"
                  - type: explode
                  - type: save
                """));
        String warning = onlyWarning();
        assertTrue(warning.contains("step 2"), warning);
        assertTrue(warning.contains("explode"), warning);
        assertTrue(warning.contains("test_output.yml"), warning);
    }

    @Test
    void invalidEnumListsTheOptions() {
        assertNull(parse("""
                steps:
                  - type: cmd
                    cmd_type: colour
                    operation: set
                    index: 0
                    values: ["1"]
                  - type: save
                """));
        String warning = onlyWarning();
        assertTrue(warning.contains("step 1"), warning);
        assertTrue(warning.contains("float, string, boolean, color"), warning);
    }

    @Test
    void missingRequiredKeyIsRefused() {
        assertNull(parse("""
                steps:
                  - type: tag
                    operation: set
                    values: ["1"]
                  - type: save
                """));
        assertTrue(onlyWarning().contains("missing 'tag_id'"), warnings.toString());
    }

    @Test
    void cmdSetWithoutIndexIsRefused() {
        assertNull(parse("""
                steps:
                  - type: cmd
                    cmd_type: float
                    operation: set
                    values: ["1"]
                  - type: save
                """));
        assertTrue(onlyWarning().contains("'set' needs an 'index'"), warnings.toString());
    }

    @Test
    void cmdAddWithoutIndexAppends() {
        CompositeOutput output = parse("""
                steps:
                  - type: cmd
                    cmd_type: string
                    operation: add
                    values: ["pattern_a"]
                  - type: save
                """);
        assertNotNull(output, warnings.toString());
    }

    @Test
    void removeOperationsNeedNoValues() {
        CompositeOutput output = parse("""
                steps:
                  - type: tag
                    operation: remove
                    tag_id: layer_count
                  - type: cmd
                    cmd_type: color
                    operation: remove
                    index: 0
                  - type: save
                """);
        assertNotNull(output, warnings.toString());
    }

    @Test
    void setWithoutValuesIsRefused() {
        assertNull(parse("""
                steps:
                  - type: tag
                    operation: set
                    tag_id: layer_count
                  - type: save
                """));
        assertTrue(onlyWarning().contains("'set' needs a 'values' list"), warnings.toString());
    }

    @Test
    void markedFetchNeedsNumericId() {
        assertNull(parse("""
                steps:
                  - type: fetch
                    fetch_type: marked
                    fetch_id: shield
                  - type: save
                """));
        assertTrue(onlyWarning().contains("marked index"), warnings.toString());
    }

    @Test
    void blendNeedsTwoSources() {
        assertNull(parse("""
                steps:
                  - type: blend
                    tag_id: dye_color
                    sources: ["marked:1"]
                  - type: save
                """));
        assertTrue(onlyWarning().contains("at least two"), warnings.toString());
    }

    @Test
    void templateWithoutSaveIsRefused() {
        assertNull(parse("""
                steps:
                  - type: fetch
                    fetch_type: marked
                    fetch_id: "0"
                """));
        assertTrue(onlyWarning().contains("no 'save' step"), warnings.toString());
    }

    @Test
    void invalidSaveAmountIsRefused() {
        assertNull(parse("""
                steps:
                  - type: save
                    amount: 0
                """));
        assertTrue(onlyWarning().contains("'amount'"), warnings.toString());
    }
}
