package org.skysongdev.skysongCrafting.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.skysongdev.skysongCrafting.SkysongCrafting;
import org.skysongdev.skysongCrafting.utils.SkysongApis;
import org.skysongdev.skysongCrafting.utils.Utils;
import org.skysongdev.skysongitems.api.ItemTags;
import org.skysongdev.skysongitems.api.TagType;
import org.skysongdev.skysongitems.api.event.SkysongItemsReadyEvent;
import org.skysongdev.skysongitems.api.event.SkysongItemsRegisterEvent;

import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

/**
 * Registers the item tags SkysongCrafting owns with SkysongItems, so it never writes a tag
 * SkysongItems doesn't know. These are STRING tags: the {@code crafting_key} and every
 * {@code id_to_randomize} of a {@code random_id} output (collected while recipes load, see
 * {@link Utils#getRequiredStringTags()}).
 * <p>
 * SkysongItems asks once all plugins have enabled and again after every {@code /si reload}.
 * A {@code /sc reload} can't start a new round, so a tag that only a reload added is reported
 * and becomes available after the next {@code /si reload}.
 */
public class SkysongItemsRegistrationListener implements Listener {
    /** The tags SkysongCrafting registered in the last round; registered again every round. */
    private final Set<String> registeredByUs = new HashSet<>();

    @EventHandler
    public void onRegister(SkysongItemsRegisterEvent event) {
        ItemTags tags = SkysongApis.items().tags();
        Set<String> registered = new HashSet<>();
        for (String tagId : Utils.Instance().getRequiredStringTags()) {
            // During the event, exists() still answers for the previous round. A tag that exists and
            // that we didn't register is SkysongItems' own (built-in, such as material_id, or from its
            // config.yml): registering a built-in one is refused with a warning, so leave those alone.
            if (tags.exists(tagId) && !registeredByUs.contains(tagId)) continue;
            if (event.registerTag(SkysongCrafting.Instance(), tagId, TagType.STRING)) {
                registered.add(tagId);
            }
        }
        registeredByUs.clear();
        registeredByUs.addAll(registered);
    }

    @EventHandler
    public void onReady(SkysongItemsReadyEvent event) {
        warnAboutMissingTags();
    }

    /**
     * Logs the tags SkysongCrafting needs that SkysongItems doesn't have, or that aren't text tags.
     *
     * @return the missing tag ids, empty when everything is in place
     */
    public static Set<String> warnAboutMissingTags() {
        ItemTags tags = SkysongApis.items().tags();
        Set<String> missing = new TreeSet<>();
        for (String tagId : Utils.Instance().getRequiredStringTags()) {
            if (!tags.exists(tagId)) {
                missing.add(tagId);
            } else if (tags.type(tagId).orElse(null) != TagType.STRING) {
                SkysongCrafting.Instance().getLogger().warning("SkysongItems tag '" + tagId + "' is not a string tag,"
                        + " but SkysongCrafting uses it for ingredient ids or random ids. Change its type in SkysongItems' config.yml.");
            }
        }
        if (!missing.isEmpty()) {
            SkysongCrafting.Instance().getLogger().warning("SkysongItems does not have these tags yet: " + String.join(", ", missing)
                    + ". Run /si reload so SkysongCrafting can register them.");
        }
        return missing;
    }
}
