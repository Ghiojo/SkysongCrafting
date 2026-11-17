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
                  - type: trait
                    operation: set
                    trait_id: painted
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
        assertInstanceOf(TraitStep.class, steps.get(3));
        assertInstanceOf(BlendStep.class, steps.get(4));
        assertInstanceOf(SaveStep.class, steps.get(5));
        assertEquals(2, ((SaveStep) steps.get(5)).getAmount());
    }

    @Test
    void legacyPropertyStepStillLoadsAsTraitStep() {
        CompositeOutput output = parse("""
                steps:
                  - type: fetch
                    fetch_type: marked
                    fetch_id: "0"
                  - type: property
                    operation: remove
                    property_id: painted
                  - type: save
                """);
        assertNotNull(output, warnings.toString());
        assertInstanceOf(TraitStep.class, output.getSteps().get(1));
    }

    @Test
    void traitStepReadsAnOptionalDuration() {
        CompositeOutput output = parse("""
                steps:
                  - type: fetch
                    fetch_type: marked
                    fetch_id: "0"
                  - type: trait
                    operation: set
                    trait_id: honed
                    duration: 1d12h
                  - type: trait
                    operation: remove
                    trait_id: rusted
                  - type: save
                """);
        assertNotNull(output, warnings.toString());
        assertEquals(StepTiming.forTime("1d12h"), ((TraitStep) output.getSteps().get(1)).getTiming());
        assertEquals(StepTiming.NOW, ((TraitStep) output.getSteps().get(2)).getTiming(), "no time means for good");
    }

    @Test
    void traitStepWithMalformedDurationIsRefused() {
        assertNull(parse("""
                steps:
                  - type: fetch
                    fetch_type: marked
                    fetch_id: "0"
                  - type: trait
                    operation: set
                    trait_id: honed
                    duration: 24
                  - type: save
                """));
        assertTrue(onlyWarning().contains("step 2") && onlyWarning().contains("duration"), warnings.toString());
    }

    private static final String FETCH = """
            steps:
              - type: fetch
                fetch_type: marked
                fetch_id: "0"
            """;

    /** A template of a fetch, the given step (indented as a list entry) and a save. */
    private CompositeOutput parseStep(String step) {
        return parse(FETCH + step.indent(2) + "  - type: save\n");
    }

    @Test
    void everyTimerStepLoadsWithItsTiming() {
        CompositeOutput output = parse(FETCH + """
                  - type: trait
                    operation: set
                    trait_id: blessed
                    after: 1d
                  - type: tag
                    operation: set
                    tag_id: sharpness
                    values: ["3"]
                    duration: 12h
                  - type: tag
                    operation: add
                    tag_id: rust
                    values: ["1"]
                    every: 1w
                  - type: tag
                    operation: remove
                    tag_id: wet
                    after: 30m
                  - type: charges
                    operation: add
                    values: ["2"]
                    duration: 24h
                  - type: hp
                    operation: set_max
                    values: ["20"]
                    after: 1mo
                  - type: charges
                    operation: remove
                    every: 1d
                  - type: transform
                    every: 1mo
                  - type: timers
                    operation: clear
                    action: remove_trait
                    target: honed
                  - type: save
                """);
        assertNotNull(output, warnings.toString());
        List<OutputStep> steps = output.getSteps();
        assertEquals(StepTiming.after("1d"), ((TraitStep) steps.get(1)).getTiming());
        assertEquals(StepTiming.forTime("12h"), ((TagStep) steps.get(2)).getTiming());
        assertEquals(StepTiming.every("1w"), ((TagStep) steps.get(3)).getTiming());
        assertEquals(StepTiming.after("30m"), ((TagStep) steps.get(4)).getTiming());
        MeterStep charges = (MeterStep) steps.get(5);
        assertEquals(MeterStep.Meter.CHARGES, charges.getMeter());
        assertEquals(StepTiming.forTime("24h"), charges.getTiming());
        MeterStep hp = (MeterStep) steps.get(6);
        assertEquals(MeterStep.Meter.HP, hp.getMeter());
        assertEquals(MeterStep.Operation.SET_MAX, hp.getOperation());
        assertEquals(StepTiming.every("1d"), ((MeterStep) steps.get(7)).getTiming());
        assertEquals(StepTiming.every("1mo"), ((TransformStep) steps.get(8)).getTiming());
        assertInstanceOf(TimersStep.class, steps.get(9));
    }

    @Test
    void chargesAndHpCanChangeNow() {
        assertNotNull(parseStep("""
                - type: hp
                  operation: add
                  values: ["-2", "marked:1;tag:bonus"]
                """), warnings.toString());
        assertEquals(StepTiming.NOW, ((MeterStep) parseStep("""
                - type: charges
                  operation: set
                  values: ["5"]
                """).getSteps().get(1)).getTiming());
    }

    @Test
    void timingCombinationsSkysongItemsCantDoAreRefused() {
        String[] refused = {
                // two timings on one step
                "- type: trait\n  operation: set\n  trait_id: honed\n  duration: 1d\n  after: 2d\n",
                // a tag can't be removed for a while
                "- type: tag\n  operation: remove\n  tag_id: wet\n  duration: 1h\n",
                // set and set_max on charges/HP only now, after or every
                "- type: hp\n  operation: set\n  values: [\"5\"]\n  duration: 1h\n",
                "- type: charges\n  operation: set_max\n  values: [\"5\"]\n  duration: 1h\n",
                // remove on charges/HP only after or every
                "- type: charges\n  operation: remove\n",
                "- type: hp\n  operation: remove\n  duration: 1h\n",
                // transform only after or every
                "- type: transform\n",
                "- type: transform\n  duration: 1d\n",
                // charges/HP need values except for remove
                "- type: charges\n  operation: add\n",
                // malformed times
                "- type: tag\n  operation: set\n  tag_id: a\n  values: [\"1\"]\n  every: soon\n",
                "- type: transform\n  after: 7\n",
                // unknown timers operation
                "- type: timers\n  operation: list\n",
        };
        for (String step : refused) {
            warnings.clear();
            assertNull(parseStep(step), "should be refused: " + step);
            assertTrue(onlyWarning().contains("step 2"), step + " -> " + warnings);
        }
    }

    @Test
    void traitStepWithoutTraitIdIsRefused() {
        assertNull(parse("""
                steps:
                  - type: fetch
                    fetch_type: marked
                    fetch_id: "0"
                  - type: trait
                    operation: set
                  - type: save
                """));
        assertTrue(onlyWarning().contains("step 2") && onlyWarning().contains("trait_id"), warnings.toString());
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
