package org.skysongdev.skysongCrafting.data.output.steps;

import org.bukkit.Color;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.components.CustomModelDataComponent;
import org.skysongdev.skysongCrafting.data.output.OutputException;
import org.skysongdev.skysongCrafting.data.output.data.StepData;
import org.skysongdev.skysongCrafting.data.output.utils.PlaceholderParser;

import java.util.ArrayList;
import java.util.List;

/**
 * Edits one of the four custom model data lists of the pivot item.
 * <pre>
 *   - type: cmd
 *     cmd_type: float | string | boolean | color
 *     operation: set | add | remove
 *     index: 2                                  # set/remove: required. add: optional (append when absent)
 *     values: ["marked:1;tag:dye_color"]        # set/add only; floats sum all values, others take the first
 * </pre>
 * {@code index} and every value may be a placeholder, e.g. {@code index: "marked:0;tag:layer_count"}.
 * Colours accept {@code R,G,B}, {@code #RRGGBB} or an RGB integer (what colour tags store).
 */
public class CMDStep extends OutputStep {
    public enum CmdType { FLOAT, STRING, BOOLEAN, COLOR }
    public enum Operation { SET, ADD, REMOVE }

    private final CmdType cmdType;
    private final Operation operation;
    private final String index;      // null = none given
    private final List<String> values;

    public CMDStep(CmdType cmdType, Operation operation, String index, List<String> values) {
        this.cmdType = cmdType;
        this.operation = operation;
        this.index = index;
        this.values = values;
    }

    @Override
    public String getType() {
        return "cmd";
    }

    @Override
    public StepData execute(StepData stepData) throws OutputException {
        ItemStack item = requirePivot(stepData);
        ItemMeta meta = item.getItemMeta();
        CustomModelDataComponent cmd = meta.getCustomModelDataComponent();

        switch (cmdType) {
            case FLOAT: {
                List<Float> list = new ArrayList<>(cmd.getFloats());
                apply(list, stepData, () -> {
                    float sum = 0;
                    for (String v : requireValues()) sum += PlaceholderParser.resolveFloat(v, stepData, "float value");
                    return sum;
                });
                cmd.setFloats(list);
                break;
            }
            case STRING: {
                List<String> list = new ArrayList<>(cmd.getStrings());
                apply(list, stepData, () -> PlaceholderParser.resolve(requireValues().get(0), stepData));
                cmd.setStrings(list);
                break;
            }
            case BOOLEAN: {
                List<Boolean> list = new ArrayList<>(cmd.getFlags());
                apply(list, stepData, () -> PlaceholderParser.resolveBoolean(requireValues().get(0), stepData, "boolean value"));
                cmd.setFlags(list);
                break;
            }
            case COLOR: {
                List<Color> list = new ArrayList<>(cmd.getColors());
                apply(list, stepData, () -> PlaceholderParser.resolveColor(requireValues().get(0), stepData, "color value"));
                cmd.setColors(list);
                break;
            }
        }

        meta.setCustomModelDataComponent(cmd);
        item.setItemMeta(meta);
        stepData.setPivotItem(item);
        return stepData;
    }

    private interface ValueSupplier<T> {
        T get() throws OutputException;
    }

    private <T> void apply(List<T> list, StepData stepData, ValueSupplier<T> value) throws OutputException {
        String listName = cmdType.name().toLowerCase() + " list";
        switch (operation) {
            case SET: {
                int i = requireIndex(stepData);
                checkIndex(i, list.size(), listName);
                list.set(i, value.get());
                break;
            }
            case ADD: {
                if (index == null) {
                    list.add(value.get());
                } else {
                    int i = PlaceholderParser.resolveInt(index, stepData, "index");
                    if (i < 0 || i > list.size()) {
                        throw new OutputException("cannot insert at index " + i + " (" + listName + " has " + list.size() + " entries)");
                    }
                    list.add(i, value.get());
                }
                break;
            }
            case REMOVE: {
                int i = requireIndex(stepData);
                checkIndex(i, list.size(), listName);
                list.remove(i);
                break;
            }
        }
    }

    private int requireIndex(StepData stepData) throws OutputException {
        if (index == null) {
            throw new OutputException("'" + operation.name().toLowerCase() + "' needs an 'index'");
        }
        return PlaceholderParser.resolveInt(index, stepData, "index");
    }

    private List<String> requireValues() throws OutputException {
        if (values == null || values.isEmpty()) {
            throw new OutputException("'" + operation.name().toLowerCase() + "' needs at least one entry in 'values'");
        }
        return values;
    }

    private static void checkIndex(int i, int size, String listName) throws OutputException {
        if (i < 0 || i >= size) {
            throw new OutputException("index " + i + " is out of range (" + listName + " has " + size + " entries)");
        }
    }
}
