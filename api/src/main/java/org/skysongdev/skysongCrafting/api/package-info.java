/**
 * SkysongCrafting's public API. Start at {@link org.skysongdev.skysongCrafting.api.SkysongCraftingApi#get()}
 * to read placed workstations, and listen to
 * {@link org.skysongdev.skysongCrafting.api.CraftFinishedEvent},
 * {@link org.skysongdev.skysongCrafting.api.WorkstationCreateEvent},
 * {@link org.skysongdev.skysongCrafting.api.WorkstationRemoveEvent} and
 * {@link org.skysongdev.skysongCrafting.api.WorkstationAccessEvent}.
 * {@link org.skysongdev.skysongCrafting.api.Workstations} is also registered on its own with the Bukkit
 * services manager, as in API 1.0.0.
 * <p>
 * Only the classes in this package are published API. Everything else in SkysongCrafting can change
 * without notice.
 */
package org.skysongdev.skysongCrafting.api;
