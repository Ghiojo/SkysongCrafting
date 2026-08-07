package org.skysongdev.skysongCrafting.utils;

import org.bukkit.persistence.PersistentDataType;
import org.skysongdev.skysongCrafting.data.output.data.AmountType;

import static org.skysongdev.skysongCrafting.SkysongCrafting.Instance;

public class TypeCastingUtils {

    @SuppressWarnings("unchecked")
    public static <Z> Z castObjectToType(PersistentDataType<?, Z> type, Object obj) {
        if (obj == null) {
            Instance().getLogger().warning("Attempted to cast a null object to type: " + type);
            return null;
        }

        Class<?> expected = Constants.TYPE_MAP.get(type);
        if (expected == null) {
            Instance().getLogger().warning("Unsupported PersistentDataType: " + type);
            throw new IllegalArgumentException("Unsupported PersistentDataType: " + type);
        }

        try {
            return (Z) obj;
        } catch (ClassCastException e) {
            Instance().getLogger().warning("Failed to cast object: " + obj + " to type: " + type);
            return null;
        }
    }
}
