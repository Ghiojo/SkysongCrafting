package org.skysongdev.skysongCrafting.api;

import java.util.Objects;

/**
 * A SkysongCrafting recipe as other plugins see it.
 *
 * @param id        the recipe file's path under {@code recipes/}, '/'-joined and without its
 *                  extension, such as {@code shaped/iron_sword}. It is the stable way to tell
 *                  recipes apart: it only changes when the file is renamed or moved.
 * @param name      the recipe's {@code name} as written in its file, in MiniMessage format, such as
 *                  {@code <gold>Iron Sword}. For display only; two recipes can share a name.
 * @param tableType the workstation type the recipe needs ({@code table_type}, a key in
 *                  {@code workstations.yml}), or {@code ANY}
 * @since 1.1.0
 */
public record RecipeInfo(String id, String name, String tableType) {
    public RecipeInfo {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(tableType, "tableType");
    }
}
