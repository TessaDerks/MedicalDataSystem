import java.security.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;

class Server {

    
    private Map<String, User> users = new HashMap<>();
    private Map<String, String> encryptedDataStore = new HashMap<>();
    private Map<String, String> dataSignatures = new HashMap<>();
    private Map<String, Set<String>> accessControl = new HashMap<>();
    /* 
    private Map<String, User> users = new HashMap<>();
    private Connection dbConnection;
    
    public Server() {
        try {
            dbConnection = DriverManager.getConnection("jdbc:mysql://localhost:3306/medical_data", "root", "password");
            System.out.println("Database connection established.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void registerUser(User user) {
        users.put(user.id, user);
        try {
            String query = "INSERT INTO users (id, role) VALUES (?, ?)";
            PreparedStatement stmt = dbConnection.prepareStatement(query);
            stmt.setString(1, user.id);
            stmt.setString(2, user.role);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    */

    public User authenticateUser(String id, String role) {
        User user = users.get(id);
        if (user != null && user.role.equals(role)) {
            return user;
        } else {
            return null;
        }
    }

    public void registerUser(User user) {
        users.put(user.id, user);
    }
    
    public void storeData(String dataId, String encryptedData, String signature, String medicalStaffId, Set<String> allowedResearchers) {
        encryptedDataStore.put(dataId, encryptedData);
        dataSignatures.put(dataId, signature);
        accessControl.put(dataId, allowedResearchers);
    }


    /*
    public void storeData(String dataId, String encryptedData, String signature, String medicalStaffId, Set<String> allowedResearchers) {
        try {
            String query = "INSERT INTO data_store (data_id, encrypted_data, signature, medical_staff_id) VALUES (?, ?, ?, ?)";
            PreparedStatement stmt = dbConnection.prepareStatement(query);
            stmt.setString(1, dataId);
            stmt.setString(2, encryptedData);
            stmt.setString(3, signature);
            stmt.setString(4, medicalStaffId);
            stmt.executeUpdate();

            for (String researcherId : allowedResearchers) {
                String accessQuery = "INSERT INTO access_control (data_id, researcher_id) VALUES (?, ?)";
                PreparedStatement accessStmt = dbConnection.prepareStatement(accessQuery);
                accessStmt.setString(1, dataId);
                accessStmt.setString(2, researcherId);
                accessStmt.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    */
     
    public String fetchData(String dataId, String researcherId) {
        if (!accessControl.getOrDefault(dataId, new HashSet<>()).contains(researcherId)) {
            throw new SecurityException("Access Denied");
        }
        return encryptedDataStore.get(dataId);
    }
    /*
    public String fetchData(String dataId, String researcherId) {
            try {
                String query = "SELECT encrypted_data FROM data_store ds JOIN access_control ac ON ds.data_id = ac.data_id WHERE ds.data_id = ? AND ac.researcher_id = ?";
                PreparedStatement stmt = dbConnection.prepareStatement(query);
                stmt.setString(1, dataId);
                stmt.setString(2, researcherId);
                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    return rs.getString("encrypted_data");
                } else {
                    throw new SecurityException("Access Denied");
                }
            } catch (SQLException e) {
                e.printStackTrace();
                return null;
            }
        }
    */
     
    public String getSignature(String dataId) {
        return dataSignatures.get(dataId);
    }
     
    /*
    public String getSignature(String dataId) {
        try {
            String query = "SELECT signature FROM data_store WHERE data_id = ?";
            PreparedStatement stmt = dbConnection.prepareStatement(query);
            stmt.setString(1, dataId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getString("signature");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    */
    public PublicKey getUserPublicKey(String userId) {
        return users.get(userId).publicKey;
    }
}