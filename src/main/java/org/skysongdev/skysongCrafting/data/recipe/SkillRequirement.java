package org.skysongdev.skysongCrafting.data.recipe;

import org.skysongdev.skysongstats.api.CharacterId;
import org.skysongdev.skysongstats.api.Skill;
import org.skysongdev.skysongstats.api.SkillLevel;
import org.skysongdev.skysongstats.api.Skills;

import java.util.Locale;
import java.util.Optional;

/**
 * One entry of a recipe's {@code skill_requirements}, written {@code "skill:level"}.
 * <ul>
 *   <li>{@code skill} is a skill id or alias from SkysongStats' skills.yml, such as
 *       {@code metalworking}, {@code metal} or {@code tande}. Nothing else is accepted.</li>
 *   <li>{@code level} is a level id or name from skills.yml's {@code levels:} (such as
 *       {@code novice}), or an amount of XP, which stands for the level that XP reaches
 *       ({@code 16} is Novice with the default skills.yml).</li>
 * </ul>
 * A player meets the requirement when their skill <b>level</b> (from the XP of the character
 * they are playing) is the required level or a higher one.
 */
public final class SkillRequirement {
    private final String written;
    private final Skill skill;
    /** The required level's id, or {@code null} when the requirement was written as XP. */
    private final String levelId;
    private final int xp;

    private SkillRequirement(String written, Skill skill, String levelId, int xp) {
        this.written = written;
        this.skill = skill;
        this.levelId = levelId;
        this.xp = xp;
    }

    /**
     * Reads one {@code "skill:level"} entry.
     *
     * @throws IllegalArgumentException with the reason, when the entry is malformed, the skill
     *                                  doesn't exist in skills.yml or the level doesn't exist
     */
    public static SkillRequirement parse(String entry, Skills skills) {
        String[] parts = entry == null ? new String[0] : entry.split(":", 2);
        if (parts.length != 2 || parts[0].isBlank() || parts[1].isBlank()) {
            throw new IllegalArgumentException("'" + entry + "' is not written as \"skill:level\"");
        }
        String skillName = parts[0].trim();
        String levelText = parts[1].trim();

        Skill skill = skills.find(skillName).orElseThrow(() -> new IllegalArgumentException(
                "invalid skill '" + skillName + "': SkysongStats' skills.yml has no skill with that id or alias"));

        try {
            int xp = Integer.parseInt(levelText);
            if (xp < 0) throw new IllegalArgumentException("'" + entry + "' asks for negative XP");
            return new SkillRequirement(entry, skill, null, xp);
        } catch (NumberFormatException notANumber) {
            SkillLevel level = findLevel(skills, levelText).orElseThrow(() -> new IllegalArgumentException(
                    "invalid skill level '" + levelText + "' in '" + entry + "': SkysongStats' skills.yml has no level with that id or name"));
            return new SkillRequirement(entry, skill, level.id(), 0);
        }
    }

    private static Optional<SkillLevel> findLevel(Skills skills, String idOrName) {
        String wanted = idOrName.toLowerCase(Locale.ROOT);
        for (SkillLevel level : skills.levels()) {
            if (level.id().equalsIgnoreCase(wanted) || level.name().equalsIgnoreCase(wanted)) return Optional.of(level);
        }
        return Optional.empty();
    }

    /** The entry as written in the recipe file. */
    public String getWritten() {
        return written;
    }

    public Skill getSkill() {
        return skill;
    }

    /**
     * The level this requirement asks for, looked up again in skills.yml so a {@code /ssreload}
     * that changed the levels is followed. Empty when the level was removed from skills.yml.
     */
    public Optional<SkillLevel> requiredLevel(Skills skills) {
        return levelId == null ? Optional.of(skills.levelFor(xp)) : findLevel(skills, levelId);
    }

    /**
     * Checks a player against this requirement, using the character they are playing now.
     * Every missing piece (no character, a skill or level removed by {@code /ssreload}) fails
     * the check and says why in the result, instead of throwing.
     */
    public Check check(Skills skills, Optional<CharacterId> character) {
        // Display text is the skill's short name, exactly as skills.yml configures it.
        String skillName = skill.shortName();
        Optional<SkillLevel> required = requiredLevel(skills);
        String requiredName = required.map(SkillLevel::name).orElse("Invalid level");

        if (skills.find(skill.id()).isEmpty()) {
            return new Check(skillName, "Invalid skill", requiredName, false);
        }
        if (character.isEmpty()) {
            return new Check(skillName, "No active character", requiredName, false);
        }
        Optional<SkillLevel> playerLevel = skills.level(character.get(), skill);
        if (playerLevel.isEmpty()) {
            return new Check(skillName, "No active character", requiredName, false);
        }
        boolean met = required.isPresent() && playerLevel.get().minXp() >= required.get().minXp();
        return new Check(skillName, playerLevel.get().name(), requiredName, met);
    }

    /**
     * The outcome of one requirement for one player, with the texts the craft button shows.
     *
     * @param skillName     the skill's short name from skills.yml
     * @param playerLevel   the player's level name, or why there is none
     * @param requiredLevel the required level's name
     * @param met           whether the player may craft as far as this skill is concerned
     */
    public record Check(String skillName, String playerLevel, String requiredLevel, boolean met) {
    }
}
