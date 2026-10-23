package org.skysongdev.skysongCrafting.data.recipe;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * How one ingredient is consumed when the recipe is crafted. Written as a suffix on the
 * ingredient in the recipe file:
 * <pre>
 *   iron_ingot          consume one (default)
 *   wand^2              reduce the item's charges by 2 instead of consuming it
 *   anvil!keep          leave the item untouched   (anvil^0 means the same)
 * </pre>
 * The suffix may come before or after a shapeless count: {@code dye*2^1} and {@code dye^1*2} are equal.
 */
public final class ConsumptionRule {
    public enum Kind { CONSUME, CHARGES, KEEP }

    public static final ConsumptionRule CONSUME_ONE = new ConsumptionRule(Kind.CONSUME, 0);
    public static final ConsumptionRule KEEP_ITEM = new ConsumptionRule(Kind.KEEP, 0);

    private static final Pattern CHARGES = Pattern.compile("\\^(\\d+)");
    private static final Pattern KEEP = Pattern.compile("(?i)!keep");

    private final Kind kind;
    private final int charges;

    private ConsumptionRule(Kind kind, int charges) {
        this.kind = kind;
        this.charges = charges;
    }

    public static ConsumptionRule charges(int amount) {
        return amount <= 0 ? KEEP_ITEM : new ConsumptionRule(Kind.CHARGES, amount);
    }

    public Kind getKind() { return kind; }
    public int getCharges() { return charges; }

    /** The rule encoded in an ingredient token; {@link #CONSUME_ONE} when there is no suffix. */
    public static ConsumptionRule parse(String token) {
        if (token == null) return CONSUME_ONE;
        if (KEEP.matcher(token).find()) return KEEP_ITEM;
        Matcher m = CHARGES.matcher(token);
        if (m.find()) return charges(Integer.parseInt(m.group(1)));
        return CONSUME_ONE;
    }

    /** The token without its consumption suffix (a shapeless {@code *N} count is kept). */
    public static String strip(String token) {
        if (token == null) return "";
        return KEEP.matcher(CHARGES.matcher(token).replaceAll("")).replaceAll("").trim();
    }

    @Override
    public String toString() {
        return switch (kind) {
            case CONSUME -> "consume";
            case CHARGES -> "^" + charges;
            case KEEP -> "!keep";
        };
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ConsumptionRule other)) return false;
        return kind == other.kind && charges == other.charges;
    }

    @Override
    public int hashCode() {
        return Objects.hash(kind, charges);
    }
}
