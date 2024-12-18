import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import MedicalDataPlatform.lib.mindrot.BCrypt;

abstract class User {
    String id;
    String role;
    String password;
    PublicKey publicKey;
    PrivateKey privateKey;


    User(String id, String role, String password) {
        this.id = id;
        this.role = role;
        this.password = BCrypt.hashpw(password, BCrypt.gensalt());
        generateKeyPair();
    }

    // add check for password??
    private void generateKeyPair() {
        try {
            KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance("RSA");
            keyPairGen.initialize(2048);
            KeyPair pair = keyPairGen.generateKeyPair();
            this.publicKey = pair.getPublic();
            this.privateKey = pair.getPrivate();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }


    private boolean checkAuthenticity(String password,String hash){
        return BCrypt.checkpw(password, hash);
    }
 
}

