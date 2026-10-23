package org.skysongdev.skysongCrafting.data.recipe;

import org.junit.jupiter.api.Test;
import org.skysongdev.skysongCrafting.data.recipe.RecipeCondition.Operator;

import static org.junit.jupiter.api.Assertions.*;

class RecipeConditionTest {

    @Test
    void numericComparisons() {
        assertTrue(RecipeCondition.compare("3", Operator.LESS, "8"));
        assertFalse(RecipeCondition.compare("8", Operator.LESS, "8"));
        assertTrue(RecipeCondition.compare("8", Operator.LESS_OR_EQUAL, "8"));
        assertTrue(RecipeCondition.compare("9", Operator.GREATER, "8.5"));
        assertTrue(RecipeCondition.compare("2.0", Operator.EQUALS, "2"));
        assertTrue(RecipeCondition.compare("2", Operator.NOT_EQUALS, "3"));
    }

    @Test
    void textComparisonsIgnoreCase() {
        assertTrue(RecipeCondition.compare("SHIELD", Operator.EQUALS, "shield"));
        assertTrue(RecipeCondition.compare("SHIELD", Operator.NOT_EQUALS, "sword"));
        assertFalse(RecipeCondition.compare("true", Operator.EQUALS, "false"));
    }

    @Test
    void orderingOnTextIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> RecipeCondition.compare("SHIELD", Operator.LESS, "8"));
    }

    @Test
    void operatorSymbols() {
        assertEquals(Operator.GREATER_OR_EQUAL, Operator.fromSymbol(">="));
        assertEquals(Operator.EQUALS, Operator.fromSymbol(" == "));
        assertNull(Operator.fromSymbol("=>"));
    }
}
