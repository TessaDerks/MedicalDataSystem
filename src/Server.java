import java.nio.charset.StandardCharsets;
import java.security.*;
import java.util.*;

class Server {

    
    private Map<String, User> users = new HashMap<>();
    private Map<Integer, byte[]> database = new HashMap<>(); // the actual (encrypted) data, stored by dataid
    private Map<Integer, ArrayList<byte[]>> dataSignatures = new HashMap<>(); // for each data item, signature + userid to check to ensure integrity
    // aes keys used for encrypting data, encrypted by public key of researcher needed to decrypt medical data
    // have researcherid as key with List<dataid,encrypted key, salt> as value
    private Map<ArrayList<String>, ArrayList<byte[]>> accessControl = new HashMap<>(); 

    public User authenticateUser(String id, String passwordAttempt, String role) {
        User user = users.get(id);
        if (user != null && user.role.equals(role)) {
            if(user.checkPassword(passwordAttempt)){
                System.out.println("user is in system and authenticated");
                return user;
            } else{ return null; }
        } else { return null; }
    }

    public void registerUser(User user) {
        users.put(user.id, user);
    }
    
    public void storeData(int dataId, byte[] encryptedData, byte[] signature, String medicalStaffId) {
        database.put(dataId, encryptedData);
        ArrayList<byte[]> valueSet = new ArrayList<byte[]>();
        valueSet.add(signature); valueSet.add(medicalStaffId.getBytes(StandardCharsets.UTF_8));
        dataSignatures.put(dataId, valueSet);
    }

    public void storeResearcherKey(String researcherId,int dataId, byte[] encryptedDataKey,byte[] salt){
        ArrayList<byte[]> valueSet = new ArrayList<byte[]>();
        valueSet.add(encryptedDataKey); valueSet.add(salt);
        ArrayList<String> keySet = new ArrayList<String>();
        keySet.add(researcherId); keySet.add(String.valueOf(dataId));
        accessControl.put(keySet,valueSet);
    }

    public ArrayList<byte[]> getEncryptedKey(String researcherId, String dataId){
        return accessControl.get(new ArrayList<String>(Arrays.asList(researcherId,dataId)));
    }


    public byte[] getEncryptedData(int dataId) {
        // optional add (double) check if researcher indeed has been granted access
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
     
    public String[] getOptions(String researcherId){
        ArrayList<String> tempSet = new ArrayList<String>();
        for(ArrayList<String> key : accessControl.keySet()){
            if(key.contains(researcherId)){
                tempSet.add(key.get(1));
            }
        }
        //tempSet.remove(researcherId);
        String[] dataItems = new String[tempSet.size()];
        tempSet.toArray(dataItems);
        return dataItems;
    }

    public ArrayList<byte[]> getSignature(int dataId) {
        return dataSignatures.get(dataId);
    }
     
    public PublicKey getUserPublicKey(String userId) {
        return users.get(userId).publicKey;
    }

}