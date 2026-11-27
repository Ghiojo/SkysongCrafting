package org.skysongdev.skysongCrafting.api;

import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

/**
 * Entry point of the SkysongCrafting API, the supported way for other plugins to work with
 * SkysongCrafting. Classes outside the {@code org.skysongdev.skysongCrafting.api} package are
 * internal and may change without notice.
 *
 * <p>SkysongCrafting registers this API with Bukkit's {@link org.bukkit.plugin.ServicesManager}
 * in its {@code onEnable}. Plugins that use it add {@code softdepend: [SkysongCrafting]} (or
 * {@code depend}) to their plugin.yml, so they enable after SkysongCrafting, and get it with
 * {@link #get()}:
 *
 * <pre>{@code
 * SkysongCraftingApi crafting = SkysongCraftingApi.get();
 * }</pre>
 *
 * <p>Events need no API object: listen to them like any Bukkit event. They are
 * {@link CraftFinishedEvent}, {@link WorkstationCreateEvent}, {@link WorkstationRemoveEvent} and
 * {@link WorkstationAccessEvent}.
 *
 * <p>SkysongCrafting implements this interface; other plugins must not.
 *
 * @since 1.1.0
 */
@ApiStatus.NonExtendable
public interface SkysongCraftingApi {

    /**
     * The API registered by SkysongCrafting.
     *
     * @return the API
     * @throws IllegalStateException when SkysongCrafting is not installed or not enabled yet
     */
    static @NotNull SkysongCraftingApi get() {
        RegisteredServiceProvider<SkysongCraftingApi> registration =
                Bukkit.getServicesManager().getRegistration(SkysongCraftingApi.class);
        if (registration == null) {
            throw new IllegalStateException("The SkysongCrafting API is not available: SkysongCrafting is not enabled."
                    + " Add 'softdepend: [SkysongCrafting]' to your plugin.yml and check that SkysongCrafting is installed.");
        }
        return registration.getProvider();
    }

    /**
     * The version of the API that the installed SkysongCrafting implements, as
     * {@code major.minor.patch}. A plugin can compare it with the version it was built against
     * and log a clear message instead of failing with {@link NoSuchMethodError}. Adding methods or
     * events raises the minor version; removing or changing one raises the major.
     *
     * @return the API version, for example {@code "1.1.0"}
     */
    @NotNull String apiVersion();

    /** Reads the placed workstations. The same object as the {@link Workstations} service. */
    @NotNull Workstations workstations();
}
