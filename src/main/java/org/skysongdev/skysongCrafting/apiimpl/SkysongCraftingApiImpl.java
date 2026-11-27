package org.skysongdev.skysongCrafting.apiimpl;

import org.jetbrains.annotations.NotNull;
import org.skysongdev.skysongCrafting.api.SkysongCraftingApi;
import org.skysongdev.skysongCrafting.api.Workstations;

/** SkysongCrafting's side of {@link SkysongCraftingApi}, registered with the services manager on enable. */
public final class SkysongCraftingApiImpl implements SkysongCraftingApi {
    /** Raise the minor version when the api module gains something, the major when something changes or goes. */
    public static final String API_VERSION = "1.1.0";

    private final Workstations workstations;

    public SkysongCraftingApiImpl(Workstations workstations) {
        this.workstations = workstations;
    }

    @Override
    public @NotNull String apiVersion() {
        return API_VERSION;
    }

    @Override
    public @NotNull Workstations workstations() {
        return workstations;
    }
}
