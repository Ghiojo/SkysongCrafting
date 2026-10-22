package org.skysongdev.skysongCrafting.data.output.utils;

import org.bukkit.Color;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.components.CustomModelDataComponent;
import org.bukkit.persistence.PersistentDataType;
import org.skysongdev.skysongCrafting.data.output.OutputException;
import org.skysongdev.skysongCrafting.data.output.data.StepData;
import org.skysongdev.skysongitems.utils.Constants;
import org.skysongdev.skysongitems.utils.Utils;

import java.util.List;
import java.util.UUID;

/**
 * Resolves the values written in a composite template.
 * <p>
 * A value is either a literal ({@code "1"}, {@code "255,0,0"}, {@code "hello"}) or a placeholder.
 * Placeholders contain a {@code ;} and have the form {@code reference;data}:
 * <pre>
 *   reference:  marked:N   (the N-th marked item, counting from 0)
 *               pivot      (the item currently being edited)
 *   data:       tag:TAG_ID             value of a SkysongItems tag on that item
 *               cmd:float:I            I-th float of the item's custom model data
 *               cmd:string:I           I-th string
 *               cmd:boolean:I          I-th flag
 *               cmd:color:I            I-th colour, as an RGB integer
 *               cmd:floats | cmd:strings | cmd:booleans | cmd:colors   number of entries
 *               uuid                   a fresh random UUID (reference is ignored)
 * </pre>
 * Example: {@code marked:1;tag:dye_color} reads the dye_color tag of the second marked item.
 */
public final class PlaceholderParser {
    private PlaceholderParser() {}

    public static boolean isPlaceholder(String value) {
        return value != null && value.contains(";");
    }

    /** Returns the literal unchanged, or the resolved placeholder as a string. */
    public static String resolve(String value, StepData stepData) throws OutputException {
        if (value == null) throw new OutputException("missing value");
        if (!isPlaceholder(value)) return value;

        String[] parts = value.split(";", 2);
        String reference = parts[0].trim();
        String data = parts[1].trim();

        String[] dataParts = data.split(":");
        String dataType = dataParts[0].trim().toLowerCase();

        if (dataType.equals("uuid")) {
            return UUID.randomUUID().toString();
        }

        ItemStack item = resolveReference(reference, stepData);
        ItemMeta meta = item.getItemMeta();
        if (meta == null) throw new OutputException("item referenced by '" + reference + "' has no item data");

        switch (dataType) {
            case "tag": {
                if (dataParts.length < 2) throw new OutputException("placeholder '" + value + "' needs a tag id (tag:TAG_ID)");
                String tagId = dataParts[1].trim();
                if (!Utils.isATag(tagId)) throw new OutputException("'" + tagId + "' is not a tag registered in SkysongItems");
                NamespacedKey key = Utils.getTag(tagId);
                PersistentDataType<?, ?> type = Constants.tagList.get(key);
                if (!meta.getPersistentDataContainer().has(key, type)) {
                    throw new OutputException("item referenced by '" + reference + "' has no '" + tagId + "' tag");
                }
                Object tagValue = meta.getPersistentDataContainer().get(key, type);
                return String.valueOf(tagValue);
            }
            case "cmd": {
                if (dataParts.length < 2) throw new OutputException("placeholder '" + value + "' needs a custom model data kind (cmd:float:I, cmd:color:I, ...)");
                CustomModelDataComponent cmd = meta.getCustomModelDataComponent();
                String kind = dataParts[1].trim().toLowerCase();
                switch (kind) {
                    case "floats": return String.valueOf(cmd.getFloats().size());
                    case "strings": return String.valueOf(cmd.getStrings().size());
                    case "booleans": case "flags": return String.valueOf(cmd.getFlags().size());
                    case "colors": return String.valueOf(cmd.getColors().size());
                }
                if (dataParts.length < 3) throw new OutputException("placeholder '" + value + "' needs an index (cmd:" + kind + ":I)");
                int index = parseInt(dataParts[2].trim(), "index in placeholder '" + value + "'");
                switch (kind) {
                    case "float": return String.valueOf(indexed(cmd.getFloats(), index, "floats", value));
                    case "string": return indexed(cmd.getStrings(), index, "strings", value);
                    case "boolean": case "flag": return String.valueOf(indexed(cmd.getFlags(), index, "flags", value));
                    case "color": return String.valueOf(indexed(cmd.getColors(), index, "colors", value).asRGB());
                    default: throw new OutputException("unknown custom model data kind '" + kind + "' in placeholder '" + value + "'");
                }
            }
            default:
                throw new OutputException("unknown data type '" + dataType + "' in placeholder '" + value + "' (expected tag, cmd or uuid)");
        }
    }

    /** Resolves {@code marked:N} or {@code pivot} to an item. */
    public static ItemStack resolveReference(String reference, StepData stepData) throws OutputException {
        String[] refParts = reference.split(":");
        String refType = refParts[0].trim().toLowerCase();
        switch (refType) {
            case "pivot": {
                ItemStack pivot = stepData.getPivotItem();
                if (pivot == null) throw new OutputException("'pivot' used before any 'fetch' step");
                return pivot;
            }
            case "marked": {
                if (refParts.length < 2) throw new OutputException("'marked' reference needs an index (marked:N)");
                int index = parseInt(refParts[1].trim(), "marked index '" + reference + "'");
                int count = stepData.getMarkedItems().size();
                if (index < 0 || index >= count) {
                    throw new OutputException("marked item " + index + " does not exist (this recipe marked " + count + " item" + (count == 1 ? "" : "s") + ")");
                }
                return stepData.getMarkedItems().get(index).getItemStack();
            }
            default:
                throw new OutputException("unknown reference '" + reference + "' (expected marked:N or pivot)");
        }
    }

    // ---- typed helpers used by the steps ----

    public static int resolveInt(String value, StepData stepData, String what) throws OutputException {
        String resolved = resolve(value, stepData);
        try {
            return Math.round(Float.parseFloat(resolved));
        } catch (NumberFormatException e) {
            throw new OutputException(what + " must be a number, got '" + resolved + "'" + (isPlaceholder(value) ? " (from " + value + ")" : ""));
        }
    }

    public static float resolveFloat(String value, StepData stepData, String what) throws OutputException {
        String resolved = resolve(value, stepData);
        try {
            return Float.parseFloat(resolved);
        } catch (NumberFormatException e) {
            throw new OutputException(what + " must be a number, got '" + resolved + "'" + (isPlaceholder(value) ? " (from " + value + ")" : ""));
        }
    }

    public static boolean resolveBoolean(String value, StepData stepData, String what) throws OutputException {
        String resolved = resolve(value, stepData).trim().toLowerCase();
        if (resolved.equals("true")) return true;
        if (resolved.equals("false")) return false;
        throw new OutputException(what + " must be true or false, got '" + resolved + "'");
    }

    /** Accepts {@code R,G,B}, {@code #RRGGBB}, or an RGB integer (which is what colour tags store). */
    public static Color resolveColor(String value, StepData stepData, String what) throws OutputException {
        String resolved = resolve(value, stepData).trim();
        try {
            if (resolved.contains(",")) {
                String[] rgb = resolved.split(",");
                if (rgb.length != 3) throw new NumberFormatException();
                return Color.fromRGB(Integer.parseInt(rgb[0].trim()), Integer.parseInt(rgb[1].trim()), Integer.parseInt(rgb[2].trim()));
            }
            if (resolved.startsWith("#")) {
                return Color.fromRGB(Integer.parseInt(resolved.substring(1), 16));
            }
            return Color.fromRGB(Integer.parseInt(resolved));
        } catch (IllegalArgumentException e) {
            throw new OutputException(what + " must be a colour (R,G,B / #RRGGBB / RGB integer), got '" + resolved + "'");
        }
    }

    private static int parseInt(String s, String what) throws OutputException {
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            throw new OutputException(what + " must be a whole number, got '" + s + "'");
        }
    }

    private static <T> T indexed(List<T> list, int index, String name, String placeholder) throws OutputException {
        if (index < 0 || index >= list.size()) {
            throw new OutputException("index " + index + " is out of range in '" + placeholder + "' (item has " + list.size() + " " + name + ")");
        }
        return list.get(index);
    }
}
