package org.skysongdev.skysongCrafting.data.database;

import org.bukkit.Bukkit;
import org.skysongdev.skysongCrafting.data.workstations.Workstation;
import org.skysongdev.skysongCrafting.data.workstations.WorkstationReference;
import org.skysongdev.skysongCrafting.utils.Utils;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Database {
    private final String HOST;
    private final String PORT;
    private final String USER;
    private final String PASSWORD;
    private final String DATABASE_NAME;

    public Database(String HOST, String PORT, String USER, String PASSWORD, String DATABASE_NAME) {
        this.HOST = HOST;
        this.PORT = PORT;
        this.USER = USER;
        this.PASSWORD = PASSWORD;
        this.DATABASE_NAME = DATABASE_NAME;
    }

    private Connection sqlConnection;

    public Connection getConnection() throws SQLException {
        if(sqlConnection != null){
            return sqlConnection;
        }
        String url = "jdbc:mysql://" + this.HOST + ":" + this.PORT + "/" + this.DATABASE_NAME;
        sqlConnection = DriverManager.getConnection(url, this.USER, this.PASSWORD);
        Bukkit.getLogger().info("Connected to the database");
        return sqlConnection;
    }

    public void initializeDatabase() throws SQLException {
        Statement buildStatement = getConnection().createStatement();
        String sql = "CREATE TABLE IF NOT EXISTS skysong_workstations(id INT AUTO_INCREMENT PRIMARY KEY, type VARCHAR(255), ownerID varchar(36), authorizedUsers varchar(1000), isPublic BOOLEAN, x INT, y INT, z INT, world VARCHAR(255))";
        buildStatement.execute(sql);
    }

    public void createWorkstationData(WorkstationReference workstationReference) throws SQLException{
        PreparedStatement statement = getConnection().prepareStatement("INSERT INTO skysong_workstations (type, ownerID, authorizedUsers, isPublic, x, y, z, world) VALUES (?, ?, ?, ?, ?, ?, ?, ?)");
        statement.setString(1, workstationReference.getWorkstation().getId());
        statement.setString(2, workstationReference.getOwnerID().toString());
        statement.setString(3, ""); // No authorized users initially
        statement.setBoolean(4, false); // Not public initially
        statement.setInt(5, workstationReference.getLocation().get(0));
        statement.setInt(6, workstationReference.getLocation().get(1));
        statement.setInt(7, workstationReference.getLocation().get(2));
        statement.setString(8 , workstationReference.getWorldName());
        statement.executeUpdate();
        statement.close();
    }

    public void updateWorkstationData(WorkstationReference workstationReference) throws SQLException {
        PreparedStatement statement = getConnection().prepareStatement("UPDATE skysong_workstations SET type = ?, ownerID = ?, authorizedUsers = ?, isPublic = ? WHERE x = ? AND y = ? AND z = ? AND world = ?");
        statement.setString(1, workstationReference.getWorkstation().getId());
        statement.setString(2, workstationReference.getOwnerID().toString());
        statement.setString(3, workstationReference.getAuthorizedUsersAsString());
        statement.setBoolean(4, workstationReference.isPublic());
        statement.setInt(5, workstationReference.getLocation().get(0));
        statement.setInt(6, workstationReference.getLocation().get(1));
        statement.setInt(7, workstationReference.getLocation().get(2));
        statement.setString(8 , workstationReference.getWorldName());
        statement.executeUpdate();
        statement.close();
    }

    public void deleteWorkstationData(WorkstationReference workstationReference) throws SQLException {
        PreparedStatement statement = getConnection().prepareStatement("DELETE FROM skysong_workstations WHERE x = ? AND y = ? AND z = ? AND world = ?");
        statement.setInt(1, workstationReference.getLocation().get(0));
        statement.setInt(2, workstationReference.getLocation().get(1));
        statement.setInt(3, workstationReference.getLocation().get(2));
        statement.setString(4 , workstationReference.getWorldName());
        statement.executeUpdate();
        statement.close();
    }

    public WorkstationReference findWorkstationData(int x, int y, int z, String world) throws SQLException {
        PreparedStatement statement = getConnection().prepareStatement("SELECT * FROM skysong_workstations WHERE x = ? AND y = ? AND z = ? AND world = ?");
        statement.setInt(1, x);
        statement.setInt(2, y);
        statement.setInt(3, z);
        statement.setString(4 , world);
        ResultSet resultSet = statement.executeQuery();
        if(resultSet.next()){
            String type = resultSet.getString("type");
            String ownerID = resultSet.getString("ownerID");
            String authorizedUsers = resultSet.getString("authorizedUsers");
            boolean isPublic = resultSet.getBoolean("isPublic");
            String[] authorizedUsersArray = authorizedUsers.split(";");
            ArrayList<UUID> authorizedUsersList = new ArrayList<>();
            for(String userID : authorizedUsersArray){
                if(!userID.isEmpty()){
                    authorizedUsersList.add(UUID.fromString(userID));
                }
            }
            WorkstationReference workstationReference = new WorkstationReference(Utils.Instance().getWorkstationTypes().get(type), UUID.fromString(ownerID), authorizedUsersList, isPublic, new ArrayList<>(List.of(x, y, z)), world);
            statement.close();
            return workstationReference;
        }
        statement.close();
        return null;
    }

    public void dumpWorkstationData() throws SQLException {
        Statement statement = getConnection().createStatement();
        ResultSet resultSet = statement.executeQuery("SELECT * FROM skysong_workstations");
        while(resultSet.next()){
            String type = resultSet.getString("type");
            String ownerID = resultSet.getString("ownerID");
            String authorizedUsers = resultSet.getString("authorizedUsers");
            boolean isPublic = resultSet.getBoolean("isPublic");
            int x = resultSet.getInt("x");
            int y = resultSet.getInt("y");
            int z = resultSet.getInt("z");
            String world = resultSet.getString("world");
            String[] authorizedUsersArray = authorizedUsers.split(";");
            ArrayList<UUID> authorizedUsersList = new ArrayList<>();
            for(String userID : authorizedUsersArray){
                if(!userID.isEmpty()){
                    authorizedUsersList.add(UUID.fromString(userID));
                }
            }
            WorkstationReference workstationReference = new WorkstationReference(Utils.Instance().getWorkstationTypes().get(type), UUID.fromString(ownerID), authorizedUsersList, isPublic, new ArrayList<>(List.of(x, y, z)), world);
            ArrayList<WorkstationReference> existingRefs = Utils.getWorkstationReferences().get(new ArrayList<>(List.of(x, y, z)));
            existingRefs = Utils.Instance().addOrReplaceWorkstationReference(existingRefs, workstationReference);
            Utils.getWorkstationReferences().put(new ArrayList<>(List.of(x, y, z)), existingRefs);
        }
        statement.close();
    }

    public void dumpDatabaseData() throws SQLException{
        dumpWorkstationData();
    }
}
