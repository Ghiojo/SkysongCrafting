package org.skysongdev.skysongCrafting.utils;

import org.bukkit.configuration.file.YamlConfiguration;
import org.skysongdev.skysongCrafting.SkysongCrafting;
import org.skysongdev.skysongCrafting.data.item.SkysongItemWithAmmount;
import org.skysongdev.skysongCrafting.data.output.CompositeOutput;
import org.skysongdev.skysongCrafting.data.output.steps.*;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Reads the {@code steps} list of a composite output file into {@link OutputStep}s.
 * See docs/COMPOSITE_FORMAT.md.
 * <p>
 * Any mistake in any step refuses the whole output (returns {@code null}) after logging a warning
 * that names the file and the step number, so a half-loaded template can never run on a server.
 */
public final class CompositeOutputParser {
    private CompositeOutputParser() {}

    /** Thrown internally for a malformed step; the message is logged with the step number. */
    private static class ParseException extends Exception {
        ParseException(String message) { super(message); }
    }

    public static CompositeOutput parse(File file, YamlConfiguration yaml,
                                        ArrayList<SkysongItemWithAmmount> outputItems, ArrayList<String> outputStrings) {
        List<Map<?, ?>> rawSteps = yaml.getMapList("steps");
        if (rawSteps.isEmpty()) {
            warn(file, "has no 'steps' list" + (yaml.contains("output_steps") ? " (found the old 'output_steps' map; see docs/COMPOSITE_FORMAT.md)" : ""));
            return null;
        }

        List<OutputStep> steps = new ArrayList<>();
        boolean hasSave = false;
        for (int i = 0; i < rawSteps.size(); i++) {
            Map<?, ?> raw = rawSteps.get(i);
            try {
                OutputStep step = parseStep(raw);
                if (step instanceof SaveStep) hasSave = true;
                steps.add(step);
            } catch (ParseException e) {
                warn(file, "step " + (i + 1) + ": " + e.getMessage() + ". Output not loaded.");
                return null;
            }
        }
        if (!hasSave) {
            warn(file, "has no 'save' step, so it would never produce an item. Output not loaded.");
            return null;
        }
        return new CompositeOutput(outputItems, outputStrings, steps);
    }

    private static OutputStep parseStep(Map<?, ?> raw) throws ParseException {
        String type = requireString(raw, "type").toLowerCase();
        switch (type) {
            case "fetch": {
                FetchStep.FetchType fetchType = requireEnum(raw, "fetch_type", FetchStep.FetchType.class);
                String fetchId = requireString(raw, "fetch_id");
                if (fetchType == FetchStep.FetchType.MARKED) {
                    try {
                        Integer.parseInt(fetchId);
                    } catch (NumberFormatException e) {
                        throw new ParseException("'fetch_id' must be a marked index (0, 1, ...) when fetch_type is marked, got '" + fetchId + "'");
                    }
                }
                return new FetchStep(fetchId, fetchType);
            }
            case "tag": {
                TagStep.Operation op = requireEnum(raw, "operation", TagStep.Operation.class);
                String tagId = requireString(raw, "tag_id");
                List<String> values = stringList(raw, "values");
                if (op != TagStep.Operation.REMOVE && values.isEmpty()) {
                    throw new ParseException("'" + op.name().toLowerCase() + "' needs a 'values' list");
                }
                return new TagStep(op, tagId, values);
            }
            case "property": {
                PropertyStep.Operation op = requireEnum(raw, "operation", PropertyStep.Operation.class);
                return new PropertyStep(op, requireString(raw, "property_id"));
            }
            case "cmd": {
                CMDStep.CmdType cmdType = requireEnum(raw, "cmd_type", CMDStep.CmdType.class);
                CMDStep.Operation op = requireEnum(raw, "operation", CMDStep.Operation.class);
                String index = optionalString(raw, "index");
                List<String> values = stringList(raw, "values");
                if (op != CMDStep.Operation.ADD && index == null) {
                    throw new ParseException("'" + op.name().toLowerCase() + "' needs an 'index'");
                }
                if (op != CMDStep.Operation.REMOVE && values.isEmpty()) {
                    throw new ParseException("'" + op.name().toLowerCase() + "' needs a 'values' list");
                }
                return new CMDStep(cmdType, op, index, values);
            }
            case "blend": {
                String tagId = requireString(raw, "tag_id");
                List<String> sources = stringList(raw, "sources");
                if (sources.size() < 2) {
                    throw new ParseException("'blend' needs a 'sources' list with at least two item references");
                }
                return new BlendStep(tagId, sources);
            }
            case "save": {
                int amount = 1;
                Object rawAmount = raw.get("amount");
                if (rawAmount != null) {
                    try {
                        amount = Integer.parseInt(String.valueOf(rawAmount));
                    } catch (NumberFormatException e) {
                        throw new ParseException("'amount' must be a whole number, got '" + rawAmount + "'");
                    }
                    if (amount < 1) throw new ParseException("'amount' must be at least 1");
                }
                return new SaveStep(amount);
            }
            default:
                throw new ParseException("unknown step type '" + type + "' (expected fetch, tag, property, cmd, blend or save)");
        }
    }

    // ---- field helpers ----

    private static String requireString(Map<?, ?> raw, String key) throws ParseException {
        String value = optionalString(raw, key);
        if (value == null) throw new ParseException("missing '" + key + "'");
        return value;
    }

    private static String optionalString(Map<?, ?> raw, String key) {
        Object value = raw.get(key);
        if (value == null) return null;
        String s = String.valueOf(value).trim();
        return s.isEmpty() ? null : s;
    }

    private static <E extends Enum<E>> E requireEnum(Map<?, ?> raw, String key, Class<E> type) throws ParseException {
        String value = requireString(raw, key);
        try {
            return Enum.valueOf(type, value.toUpperCase());
        } catch (IllegalArgumentException e) {
            StringBuilder options = new StringBuilder();
            for (E constant : type.getEnumConstants()) {
                if (options.length() > 0) options.append(", ");
                options.append(constant.name().toLowerCase());
            }
            throw new ParseException("'" + key + "' is '" + value + "', expected one of: " + options);
        }
    }

    /** Accepts a YAML list or a single scalar, so {@code values: "1"} and {@code values: ["1"]} both work. */
    private static List<String> stringList(Map<?, ?> raw, String key) {
        Object value = raw.get(key);
        List<String> result = new ArrayList<>();
        if (value == null) return result;
        if (value instanceof List<?> list) {
            for (Object o : list) if (o != null) result.add(String.valueOf(o));
        } else {
            result.add(String.valueOf(value));
        }
        return result;
    }

    private static void warn(File file, String message) {
        SkysongCrafting.Instance().getLogger().warning("Composite output '" + file.getName() + "' " + message);
    }
}
