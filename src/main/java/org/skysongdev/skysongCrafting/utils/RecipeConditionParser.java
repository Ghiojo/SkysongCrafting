package org.skysongdev.skysongCrafting.utils;

import org.bukkit.configuration.file.YamlConfiguration;
import org.skysongdev.skysongCrafting.SkysongCrafting;
import org.skysongdev.skysongCrafting.data.recipe.RecipeCondition;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Reads the optional {@code conditions} list of a recipe file.
 * Returns an empty list when the key is absent, and {@code null} (after a warning naming the
 * file and entry) when any entry is malformed, so a recipe with a broken condition is not loaded.
 */
public final class RecipeConditionParser {
    private RecipeConditionParser() {}

    /** Thrown for a malformed entry; the message is logged with the entry number. */
    private static class ParseException extends Exception {
        ParseException(String message) { super(message); }
    }

    public static List<RecipeCondition> parse(File file, YamlConfiguration yaml) {
        return parse(file, yaml, message -> SkysongCrafting.Instance().getLogger().warning(message));
    }

    public static List<RecipeCondition> parse(File file, YamlConfiguration yaml, Consumer<String> warnings) {
        List<RecipeCondition> conditions = new ArrayList<>();
        if (!yaml.contains("conditions")) return conditions;

        List<Map<?, ?>> entries = yaml.getMapList("conditions");
        if (entries.isEmpty() && yaml.getList("conditions") != null && !yaml.getList("conditions").isEmpty()) {
            warnings.accept("Recipe '" + file.getName() + "' conditions must be a list of entries with item/check/operator/value. Recipe not loaded.");
            return null;
        }

        for (int i = 0; i < entries.size(); i++) {
            try {
                conditions.add(parseEntry(entries.get(i)));
            } catch (ParseException e) {
                warnings.accept("Recipe '" + file.getName() + "' condition " + (i + 1) + ": " + e.getMessage() + ". Recipe not loaded.");
                return null;
            }
        }
        return conditions;
    }

    private static RecipeCondition parseEntry(Map<?, ?> entry) throws ParseException {
        String item = require(entry, "item", "e.g. marked:0");
        String check = require(entry, "check", "e.g. tag:layer_count, material, amount");
        String operatorSymbol = require(entry, "operator", "==, !=, <, >, <=, >=");
        String value = require(entry, "value", null);

        if (!item.toLowerCase().startsWith("marked:")) {
            throw new ParseException("'item' must be marked:N (there is no pivot before crafting), got '" + item + "'");
        }
        RecipeCondition.Operator operator = RecipeCondition.Operator.fromSymbol(operatorSymbol);
        if (operator == null) {
            throw new ParseException("'operator' is '" + operatorSymbol + "', expected one of ==, !=, <, >, <=, >=");
        }
        return new RecipeCondition(item, check, operator, value);
    }

    private static String require(Map<?, ?> entry, String key, String hint) throws ParseException {
        Object raw = entry.get(key);
        String value = raw == null ? "" : String.valueOf(raw).trim();
        if (value.isEmpty()) {
            throw new ParseException("missing '" + key + "'" + (hint == null ? "" : " (" + hint + ")"));
        }
        return value;
    }
}
