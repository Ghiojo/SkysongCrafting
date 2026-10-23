package org.skysongdev.skysongCrafting.data.recipe;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ConsumptionRuleTest {

    @Test
    void plainTokenConsumesOne() {
        assertEquals(ConsumptionRule.CONSUME_ONE, ConsumptionRule.parse("iron_ingot"));
        assertEquals("iron_ingot", ConsumptionRule.strip("iron_ingot"));
        assertEquals(ConsumptionRule.CONSUME_ONE, ConsumptionRule.parse(""));
        assertEquals("", ConsumptionRule.strip(""));
    }

    @Test
    void chargesSuffix() {
        ConsumptionRule rule = ConsumptionRule.parse("wand^2");
        assertEquals(ConsumptionRule.Kind.CHARGES, rule.getKind());
        assertEquals(2, rule.getCharges());
        assertEquals("wand", ConsumptionRule.strip("wand^2"));
    }

    @Test
    void keepSuffixAndZeroChargesAreTheSame() {
        assertEquals(ConsumptionRule.KEEP_ITEM, ConsumptionRule.parse("anvil!keep"));
        assertEquals(ConsumptionRule.KEEP_ITEM, ConsumptionRule.parse("anvil!KEEP"));
        assertEquals(ConsumptionRule.KEEP_ITEM, ConsumptionRule.parse("anvil^0"));
        assertEquals("anvil", ConsumptionRule.strip("anvil!keep"));
    }

    @Test
    void shapelessCountIsKeptWhateverTheOrder() {
        assertEquals("dye*2", ConsumptionRule.strip("dye*2^1"));
        assertEquals("dye*2", ConsumptionRule.strip("dye^1*2"));
        assertEquals(ConsumptionRule.charges(1), ConsumptionRule.parse("dye^1*2"));
        assertEquals("dye*3", ConsumptionRule.strip("dye*3!keep"));
    }

    @Test
    void tagTokensKeepTheirHash() {
        assertEquals("#metals", ConsumptionRule.strip("#metals^1"));
        assertEquals(ConsumptionRule.charges(1), ConsumptionRule.parse("#metals^1"));
    }
}
