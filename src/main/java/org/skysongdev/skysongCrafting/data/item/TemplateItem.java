package org.skysongdev.skysongCrafting.data.item;

/**
 * One entry of an output file's {@code output_items}: a SkysongItems saved item (the id
 * {@code /si loaditem} takes, such as {@code sword} or {@code weapons/sword}) and how many
 * of it a craft gives. The item itself is created fresh on every craft.
 */
public record TemplateItem(String templateId, int amount) {
}
