package org.skysongdev.skysongCrafting.utils;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** The public recipe id: the file's path under recipes/, without its extension. */
class RecipeIdTest {
    private static final Path ROOT = Path.of("plugins", "SkysongCrafting", "recipes");

    @Test
    void isTheFolderAndFileNameWithoutExtension() {
        assertEquals("shaped/iron_sword", YamlParsingUtils.recipeId(ROOT, ROOT.resolve("shaped").resolve("iron_sword.yml")));
        assertEquals("smelting/ingot", YamlParsingUtils.recipeId(ROOT, ROOT.resolve("smelting").resolve("ingot.yaml")));
    }

    @Test
    void keepsTheCaseAndOtherDots() {
        assertEquals("shapeless/Big.Potion", YamlParsingUtils.recipeId(ROOT, ROOT.resolve("shapeless").resolve("Big.Potion.YML")));
    }
}
