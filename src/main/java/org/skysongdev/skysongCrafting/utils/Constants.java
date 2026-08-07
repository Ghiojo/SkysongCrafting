package org.skysongdev.skysongCrafting.utils;

import org.bukkit.NamespacedKey;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.persistence.PersistentDataType;
import org.skysongdev.skysongCrafting.SkysongCrafting;
import org.skysongdev.skysongCrafting.data.output.data.AmountType;
import org.skysongdev.skysongitems.SkysongItems;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Map;

import static org.bukkit.plugin.java.JavaPlugin.getPlugin;

public abstract class Constants {
    public static final String NEXT_CRAFT = "NEXT_CRAFT";
    public static final String PREV_CRAFT = "PREV_CRAFT";
    public static final String CRAFT = "CRAFT";
    public static final String CRAFTING_KEY_STRING = SkysongCrafting.Instance().getConfig().getString("crafting_key", "material_id");

    public static final Path RESOURCES_PATH = Path.of(getPlugin(SkysongCrafting.class).getDataFolder().getPath());
    public static final Path ITEMS_PATH = RESOURCES_PATH.resolve("items");
    public static final Path OUTPUT_PATH = RESOURCES_PATH.resolve("output");
    public static final Path TAGS_PATH = RESOURCES_PATH.resolve("tags");
    public static final Path RECIPES_PATH = RESOURCES_PATH.resolve("recipes");
    public static final Path WORKSTATIONS_PATH = RESOURCES_PATH.resolve("workstations.yml");
    public static final NamespacedKey CRAFTING_KEY = new NamespacedKey(getPlugin(SkysongItems.class), CRAFTING_KEY_STRING);

    public static final String PLUGIN_TAG = "<dark_gray>[<gold>SkysongCrafting<dark_gray>] <reset>";

    public static final ArrayList<InventoryAction> UPDATING_INVENTORY_ACTIONS = new ArrayList<InventoryAction>() {{
        add(InventoryAction.PLACE_ALL);
        add(InventoryAction.PLACE_SOME);
        add(InventoryAction.PLACE_ONE);
        add(InventoryAction.SWAP_WITH_CURSOR);
        add(InventoryAction.HOTBAR_SWAP);
        add(InventoryAction.DROP_ALL_CURSOR);
        add(InventoryAction.DROP_ONE_CURSOR);
        add(InventoryAction.PICKUP_ONE);
        add(InventoryAction.PICKUP_SOME);
        add(InventoryAction.PICKUP_ALL);
        add(InventoryAction.COLLECT_TO_CURSOR);
        add(InventoryAction.MOVE_TO_OTHER_INVENTORY);
    }};

    public static final String COMPOSITE_REFERENCE_PATTERN = "([^:;]+)(?::([^;:]+))?;([^:;]+):([^:;]+)(?::([^;:]+))?";

    public static final Map<PersistentDataType<?,?>, Class<?>> TYPE_MAP = Map.of(
            PersistentDataType.INTEGER, Integer.class,
            PersistentDataType.FLOAT, Float.class,
            PersistentDataType.STRING, String.class,
            PersistentDataType.BOOLEAN, Boolean.class
    );

    public static final Map<AmountType, Class<?>> AMOUNT_TYPE_MAP = Map.of(
            AmountType.INTEGER, Integer.class,
            AmountType.FLOAT, Float.class,
            AmountType.STRING, String.class,
            AmountType.BOOLEAN, Boolean.class
    );
}
