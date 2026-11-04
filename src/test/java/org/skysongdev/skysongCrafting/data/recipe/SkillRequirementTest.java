package org.skysongdev.skysongCrafting.data.recipe;

import org.junit.jupiter.api.Test;
import org.skysongdev.skysongstats.api.CharacterId;
import org.skysongdev.skysongstats.api.Skill;
import org.skysongdev.skysongstats.api.SkillChange;
import org.skysongdev.skysongstats.api.SkillLevel;
import org.skysongdev.skysongstats.api.Skills;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class SkillRequirementTest {

    /** The default skills.yml levels and two of its skills, with XP per character. */
    private static final class FakeSkills implements Skills {
        final List<SkillLevel> levels = List.of(
                new SkillLevel("unskilled", "Unskilled", "<dark_gray>", 0),
                new SkillLevel("novice", "Novice", "<green>", 16),
                new SkillLevel("competent", "Competent", "<blue>", 40),
                new SkillLevel("journeyman", "Journeyman", "<light_purple>", 80),
                new SkillLevel("expert", "Expert", "<gold>", 128));
        final List<Skill> skills = List.of(
                new Skill("metalworking", "Metalworking", "Metalworking", "<gray>", List.of("metal")),
                new Skill("tande", "Transportation and Exploration", "T&E", "<green>", List.of("transportationandexploration", "t&e")));
        final Map<CharacterId, Integer> xp = new HashMap<>();

        @Override public List<Skill> all() { return skills; }

        @Override public Optional<Skill> find(String idOrAlias) {
            for (Skill skill : skills) {
                if (skill.id().equalsIgnoreCase(idOrAlias)) return Optional.of(skill);
                for (String alias : skill.aliases()) if (alias.equalsIgnoreCase(idOrAlias)) return Optional.of(skill);
            }
            return Optional.empty();
        }

        @Override public List<SkillLevel> levels() { return levels; }

        @Override public SkillLevel levelFor(int amount) {
            SkillLevel reached = levels.get(0);
            for (SkillLevel level : levels) if (amount >= level.minXp()) reached = level;
            return reached;
        }

        @Override public OptionalInt xp(CharacterId character, Skill skill) {
            Integer value = xp.get(character);
            return value == null ? OptionalInt.empty() : OptionalInt.of(value);
        }

        @Override public Optional<SkillLevel> level(CharacterId character, Skill skill) {
            OptionalInt value = xp(character, skill);
            return value.isPresent() ? Optional.of(levelFor(value.getAsInt())) : Optional.empty();
        }

        @Override public Optional<SkillChange> setXp(CharacterId character, Skill skill, int amount) { throw new UnsupportedOperationException(); }
        @Override public Optional<SkillChange> addXp(CharacterId character, Skill skill, int amount) { throw new UnsupportedOperationException(); }
    }

    private final FakeSkills skills = new FakeSkills();
    private final CharacterId player = new CharacterId(UUID.randomUUID(), "Default");

    @Test
    void acceptsIdsAndAliasesOnly() {
        assertEquals("metalworking", SkillRequirement.parse("metalworking:novice", skills).getSkill().id());
        assertEquals("metalworking", SkillRequirement.parse("METAL:novice", skills).getSkill().id());
        assertEquals("tande", SkillRequirement.parse("t&e:novice", skills).getSkill().id());
    }

    @Test
    void oldEnumNamesAreInvalidSkills() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> SkillRequirement.parse("TRANSPORTATION_AND_EXPLORATION:16", skills));
        assertTrue(e.getMessage().startsWith("invalid skill"), e.getMessage());
        assertThrows(IllegalArgumentException.class, () -> SkillRequirement.parse("smithing:16", skills));
    }

    @Test
    void malformedEntriesAndUnknownLevelsAreRefused() {
        assertThrows(IllegalArgumentException.class, () -> SkillRequirement.parse("metalworking", skills));
        assertThrows(IllegalArgumentException.class, () -> SkillRequirement.parse("metalworking:", skills));
        assertThrows(IllegalArgumentException.class, () -> SkillRequirement.parse("metalworking:-1", skills));
        assertThrows(IllegalArgumentException.class, () -> SkillRequirement.parse("metalworking:master", skills));
    }

    @Test
    void xpRequirementMeansTheLevelThatXpReaches() {
        SkillRequirement requirement = SkillRequirement.parse("metalworking:20", skills);
        assertEquals("novice", requirement.requiredLevel(skills).orElseThrow().id());

        skills.xp.put(player, 16); // Novice, below the 20 XP written in the recipe
        SkillRequirement.Check check = requirement.check(skills, Optional.of(player));
        assertTrue(check.met());
        assertEquals("Novice", check.playerLevel());
        assertEquals("Novice", check.requiredLevel());
    }

    @Test
    void comparesLevelsNotXp() {
        SkillRequirement requirement = SkillRequirement.parse("metalworking:Competent", skills);
        skills.xp.put(player, 39);
        assertFalse(requirement.check(skills, Optional.of(player)).met());
        skills.xp.put(player, 40);
        assertTrue(requirement.check(skills, Optional.of(player)).met());
        skills.xp.put(player, 500);
        assertTrue(requirement.check(skills, Optional.of(player)).met());
    }

    @Test
    void usesTheConfiguredShortName() {
        SkillRequirement requirement = SkillRequirement.parse("tande:novice", skills);
        skills.xp.put(player, 0);
        SkillRequirement.Check check = requirement.check(skills, Optional.of(player));
        assertEquals("T&E", check.skillName());
        assertEquals("Unskilled", check.playerLevel());
        assertFalse(check.met());
    }

    @Test
    void playerWithoutACharacterNeverMeetsARequirement() {
        SkillRequirement requirement = SkillRequirement.parse("metalworking:unskilled", skills);
        SkillRequirement.Check noActive = requirement.check(skills, Optional.empty());
        assertFalse(noActive.met());
        assertEquals("No active character", noActive.playerLevel());

        // An active profile SkysongStats has no data for.
        SkillRequirement.Check noData = requirement.check(skills, Optional.of(new CharacterId(UUID.randomUUID(), "Ghost")));
        assertFalse(noData.met());
    }
}
