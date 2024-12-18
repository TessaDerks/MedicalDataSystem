import java.security.*;
import java.util.*;

class Server {

    
    private Map<String, User> users = new HashMap<>();
    private Map<Integer, byte[]> database = new HashMap<>(); // the actual (encrypted) data, stored by dataid
    private Map<Integer, String> dataSignatures = new HashMap<>(); // for each data item signatures to ensure integrity
    private Map<Integer, Set<String>> accessControl = new HashMap<>(); // which researchers are granted access for each data item
    private Map<Integer, String> dataKeys = new HashMap<>(); // aes keys used for encrypting data, encrypted by public key of researcher needed to decrypt medical data
    //private Map<String,PublicKey> publicKeysResearchers = new HashMap<>(); // to encrypt the symmetric key for the researchers
    //private Map<String,PublicKey> publicKeysMedicalStaff = new HashMap<>(); // to verify signature of medical staff
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

    public User authenticateUser(String id, String passwordAttempt, String role) {
        User user = users.get(id);
        if (user != null && user.role.equals(role)) {
            System.out.println("user is in system and correct role");
            if(user.checkPassword(passwordAttempt)){
                return user;
            }
            else{
                return null;
            }
        } else {
            return null;
        }
    }

    public void registerUser(User user) {
        users.put(user.id, user);
    }
    
    // MAYBE GET EVERYTHING IN 1 HASHMAP/TABLE? IDK?
    public void storeData(int dataId, byte[] encryptedData, String signature, Set<String> allowedResearchers) {
        database.put(dataId, encryptedData);
        dataSignatures.put(dataId, signature);
        accessControl.put(dataId, allowedResearchers);
    }

    public void storeDataKey(int dataId, String dataKey){

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
     
    public byte[] fetchData(int dataId, String researcherId) {
        if (!accessControl.getOrDefault(dataId, new HashSet<>()).contains(researcherId)) {
            throw new SecurityException("Access Denied");
        }
        return database.get(dataId);
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
     
    public String getSignature(int dataId) {
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