package org.skysongdev.skysongCrafting.data.workstations;

import java.util.ArrayList;
import java.util.UUID;

public class WorkstationReference {
    private Workstation workstation;
    private UUID ownerID;
    private ArrayList<UUID> authorizedUsers;
    private boolean isPublic;
    private ArrayList<Integer> location;

    public void setOwnerID(UUID ownerID) {
        this.ownerID = ownerID;
    }

    private String worldName;

    public WorkstationReference(Workstation workstation, UUID ownerID, ArrayList<UUID> authorizedUsers, boolean isPublic, ArrayList<Integer> location, String worldName) {
        this.workstation = new Workstation(workstation.getType(), workstation.getIcon().getType().toString(), workstation.getGui().getGuiType(), workstation.getId());
        this.ownerID = ownerID;
        this.authorizedUsers = authorizedUsers;
        this.isPublic = isPublic;
        this.location = location;
        this.worldName = worldName;
    }

    public void setAuthorizedUsers(ArrayList<UUID> authorizedUsers) {
        this.authorizedUsers = authorizedUsers;
    }

    public String getWorldName() {
        return worldName;
    }

    public Workstation getWorkstation() {
        return workstation;
    }
    public ArrayList<Integer> getLocation() {
        return location;
    }

    public UUID getOwnerID() {
        return ownerID;
    }

    public ArrayList<UUID> getAuthorizedUsers() {
        return authorizedUsers;
    }

    public boolean isPublic() {
        return isPublic;
    }

    public void togglePublic() {
        this.isPublic = !this.isPublic;
    }

    public String getAuthorizedUsersAsString(){
        StringBuilder sb = new StringBuilder();
        for (UUID uuid : authorizedUsers) {
            sb.append(uuid.toString()).append(";");
        }
        if (sb.length() > 0) {
            sb.setLength(sb.length() - 1); // Remove trailing semicolon
        }
        return sb.toString();
    }
}
