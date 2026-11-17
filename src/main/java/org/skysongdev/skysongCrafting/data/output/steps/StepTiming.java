package org.skysongdev.skysongCrafting.data.output.steps;

import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * When a step's change happens, read from the step's optional {@code duration}, {@code after}
 * or {@code every} key (at most one per step):
 * <ul>
 *     <li>{@link Kind#NOW}: no key; the change is made by the craft, for good.</li>
 *     <li>{@link Kind#FOR} ({@code duration: 24h}): the change is made by the craft and undone by
 *     SkysongItems once the time has passed.</li>
 *     <li>{@link Kind#AFTER} ({@code after: 1d}): the craft only stores a timer; SkysongItems makes
 *     the change once the time has passed.</li>
 *     <li>{@link Kind#EVERY} ({@code every: 1mo}): the craft stores a repeating timer; SkysongItems
 *     makes the change each time the time passes, counted from the craft.</li>
 * </ul>
 * Times are written as SkysongItems reads them ({@code 30m}, {@code 24h}, {@code 1d12h}) and count
 * from the craft.
 *
 * @param kind when the change happens
 * @param time the time as written, null for {@link Kind#NOW}
 */
public record StepTiming(Kind kind, @Nullable String time) {
    public enum Kind {
        NOW("now"), FOR("duration"), AFTER("after"), EVERY("every");

        private final String key;

        Kind(String key) {
            this.key = key;
        }

        /** The YAML key that selects this kind, {@code now} for none. */
        public String key() {
            return key;
        }
    }

    public static final StepTiming NOW = new StepTiming(Kind.NOW, null);

    public StepTiming {
        Objects.requireNonNull(kind, "kind");
        if ((kind == Kind.NOW) != (time == null)) {
            throw new IllegalArgumentException("a time is needed for every kind except NOW");
        }
    }

    public static StepTiming forTime(String time) {
        return new StepTiming(Kind.FOR, time);
    }

    public static StepTiming after(String time) {
        return new StepTiming(Kind.AFTER, time);
    }

    public static StepTiming every(String time) {
        return new StepTiming(Kind.EVERY, time);
    }

    /** Whether the craft only stores a timer and the change itself happens later. */
    public boolean isScheduled() {
        return kind == Kind.AFTER || kind == Kind.EVERY;
    }

    /** For messages: {@code "for 24h"}, {@code "after 1d"}, {@code "every 1mo"} or {@code "now"}. */
    public String describe() {
        return switch (kind) {
            case NOW -> "now";
            case FOR -> "for " + time;
            case AFTER -> "after " + time;
            case EVERY -> "every " + time;
        };
    }
}
