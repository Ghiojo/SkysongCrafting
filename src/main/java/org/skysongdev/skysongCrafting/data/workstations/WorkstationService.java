package org.skysongdev.skysongCrafting.data.workstations;

import org.skysongdev.skysongCrafting.api.WorkstationInfo;
import org.skysongdev.skysongCrafting.api.Workstations;
import org.skysongdev.skysongCrafting.utils.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/** The {@link Workstations} service, reading the placed workstations SkysongCrafting keeps in memory. */
public final class WorkstationService implements Workstations {
    @Override
    public Optional<WorkstationInfo> at(String world, int x, int y, int z) {
        Objects.requireNonNull(world, "world");
        List<WorkstationReference> atCoordinates = Utils.getWorkstationReferences().get(new ArrayList<>(List.of(x, y, z)));
        if (atCoordinates == null) {
            return Optional.empty();
        }
        return atCoordinates.stream()
                .filter(Objects::nonNull)
                .filter(reference -> world.equals(reference.getWorldName()))
                .findFirst()
                .map(WorkstationReference::toInfo);
    }

    @Override
    public List<WorkstationInfo> all() {
        return Utils.getWorkstationReferences().values().stream()
                .filter(Objects::nonNull)
                .flatMap(List::stream)
                .filter(Objects::nonNull)
                .map(WorkstationReference::toInfo)
                .toList();
    }
}
