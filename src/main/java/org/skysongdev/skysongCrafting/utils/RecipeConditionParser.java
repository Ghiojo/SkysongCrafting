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

    public static List<RecipeCondition> parse(File file, YamlConfiguration yaml) {
        return parse(file, yaml, message -> SkysongCrafting.Instance().getLogger().warning(message));
    }

    public static List<RecipeCondition> parse(File file, YamlConfiguration yaml, Consumer<String> warnings) {
        List<RecipeCondition> conditions = new ArrayList<>();
        if (!yaml.contains("conditions")) return conditions;

        List<Map<?, ?>> raw = yaml.getMapList("conditions");
        if (raw.isEmpty() && yaml.getList("conditions") != null && !yaml.getList("conditions").isEmpty()) {
            warnings.accept("Recipe '" + file.getName() + "' conditions must be a list of entries with item/check/operator/value. Recipe not loaded.");
            return null;
        }

        for (int i = 0; i < raw.size(); i++) {
            Map<?, ?> entry = raw.get(i);
            String item = string(entry, "item");
            String check = string(entry, "check");
            String operatorSymbol = string(entry, "operator");
            String value = string(entry, "value");

            String problem = null;
            if (item == null) problem = "missing 'item' (e.g. marked:0)";
            else if (check == null) problem = "missing 'check' (e.g. tag:layer_count, material, amount)";
            else if (operatorSymbol == null) problem = "missing 'operator' (==, !=, <, >, <=, >=)";
            else if (value == null) problem = "missing 'value'";
            else if (!item.toLowerCase().startsWith("marked:") && !item.equalsIgnoreCase("pivot")) problem = "'item' must be marked:N";
            else if (item.equalsIgnoreCase("pivot")) problem = "'item' cannot be 'pivot' in a recipe condition (there is no pivot before crafting); use marked:N";

            RecipeCondition.Operator operator = operatorSymbol == null ? null : RecipeCondition.Operator.fromSymbol(operatorSymbol);
            if (problem == null && operator == null) problem = "'operator' is '" + operatorSymbol + "', expected one of ==, !=, <, >, <=, >=";

            if (problem != null) {
                warnings.accept("Recipe '" + file.getName() + "' condition " + (i + 1) + ": " + problem + ". Recipe not loaded.");
                return null;
            }
            conditions.add(new RecipeCondition(item, check, operator, value));
        }
        return conditions;
    }

    private static String string(Map<?, ?> entry, String key) {
        Object value = entry.get(key);
        if (value == null) return null;
        String s = String.valueOf(value).trim();
        return s.isEmpty() ? null : s;
    }
}
