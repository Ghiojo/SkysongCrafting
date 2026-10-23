package org.skysongdev.skysongCrafting.data.output;

/**
 * Thrown when an output cannot produce its items.
 * <p>
 * The message is shown to the crafting player, so it should describe what went wrong
 * in terms a server owner can act on (e.g. "Step 3 (cmd): index 7 is out of range (list has 2 entries)").
 * When this is thrown, the workbench refuses the craft and leaves the input grid untouched.
 */
public class OutputException extends Exception {
    public OutputException(String message) {
        super(message);
    }
}
