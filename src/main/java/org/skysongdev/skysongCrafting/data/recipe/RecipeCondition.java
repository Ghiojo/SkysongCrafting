package org.skysongdev.skysongCrafting.data.recipe;

import org.skysongdev.skysongCrafting.data.output.OutputException;
import org.skysongdev.skysongCrafting.data.output.data.StepData;
import org.skysongdev.skysongCrafting.data.output.utils.PlaceholderParser;

/**
 * One extra requirement a recipe places on its marked ingredients, beyond the pattern itself.
 * <pre>
 *   conditions:
 *     - item: "marked:0"           # same references as composite placeholders
 *       check: "tag:layer_count"   # tag:TAG_ID | material | amount | cmd:... (anything a placeholder accepts)
 *       operator: "<"              # == != < > <= >=
 *       value: 8
 * </pre>
 * Numeric comparisons are used when both sides parse as numbers; otherwise {@code ==} and {@code !=}
 * compare text ignoring case and the ordering operators fail.
 */
public class RecipeCondition {
    public enum Operator {
        EQUALS("=="), NOT_EQUALS("!="), LESS("<"), GREATER(">"), LESS_OR_EQUAL("<="), GREATER_OR_EQUAL(">=");

        public final String symbol;
        Operator(String symbol) { this.symbol = symbol; }

        public static Operator fromSymbol(String symbol) {
            for (Operator op : values()) if (op.symbol.equals(symbol.trim())) return op;
            return null;
        }
    }

    private final String item;
    private final String check;
    private final Operator operator;
    private final String value;

    public RecipeCondition(String item, String check, Operator operator, String value) {
        this.item = item;
        this.check = check;
        this.operator = operator;
        this.value = value;
    }

    public String getItem() { return item; }
    public String getCheck() { return check; }
    public Operator getOperator() { return operator; }
    public String getValue() { return value; }

    /** Human-readable form used in the craft button lore and in refusal messages. */
    public String describe() {
        return item + " " + check + " " + operator.symbol + " " + value;
    }

    /**
     * @return {@code null} when the condition holds, otherwise a short reason.
     * @throws OutputException when the condition cannot be evaluated (missing tag, missing marked item...)
     */
    public String test(StepData data) throws OutputException {
        String actual = PlaceholderParser.resolve(item + ";" + check, data);
        boolean holds;
        try {
            holds = compare(actual, operator, value);
        } catch (IllegalArgumentException e) {
            throw new OutputException("condition '" + describe() + "' " + e.getMessage());
        }
        return holds ? null : describe() + " (is " + actual + ")";
    }

    /**
     * Numeric comparison when both sides are numbers, otherwise case-insensitive text for == and !=.
     * @throws IllegalArgumentException for an ordering operator on non-numeric values
     */
    static boolean compare(String actual, Operator operator, String expected) {
        Double actualNumber = parseNumber(actual);
        Double expectedNumber = parseNumber(expected);
        if (actualNumber != null && expectedNumber != null) {
            int cmp = Double.compare(actualNumber, expectedNumber);
            return switch (operator) {
                case EQUALS -> cmp == 0;
                case NOT_EQUALS -> cmp != 0;
                case LESS -> cmp < 0;
                case GREATER -> cmp > 0;
                case LESS_OR_EQUAL -> cmp <= 0;
                case GREATER_OR_EQUAL -> cmp >= 0;
            };
        }
        return switch (operator) {
            case EQUALS -> actual.trim().equalsIgnoreCase(expected.trim());
            case NOT_EQUALS -> !actual.trim().equalsIgnoreCase(expected.trim());
            default -> throw new IllegalArgumentException("compares non-numbers with " + operator.symbol + " (got '" + actual + "')");
        };
    }

    private static Double parseNumber(String s) {
        try {
            return Double.parseDouble(s.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
