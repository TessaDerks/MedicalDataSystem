import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.util.Arrays;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;


abstract class User {
    String id;
    String role;
    private byte[] passwordHash;
    PublicKey publicKey;
    protected PrivateKey privateKey;
    private byte[] salt;
    private static cryptographyMethods crypt = new cryptographyMethods();


    User(String id, String role, String password) {
        this.id = id;
        this.role = role;
        this.salt = crypt.getSalt();
        this.passwordHash = generateHash(password);
        generateKeyPair();
        
    }


    // add padding??
    private byte[] generateHash(String input){ 
        
        try{
            KeySpec spec = new PBEKeySpec(input.toCharArray(), this.salt, 65536, 128);
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            byte[] hash = factory.generateSecret(spec).getEncoded();
            return hash;
        }catch(Exception e){
            e.printStackTrace();
            return null;
        }   
    }

    private void generateKeyPair() {
        try {
            KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance("RSA");
            keyPairGen.initialize(2048);
            KeyPair pair = keyPairGen.generateKeyPair();
            this.publicKey = pair.getPublic();
            this.privateKey = pair.getPrivate(); // SHOULD ENCRYPT THIS WITH THE PASSWORD
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    public boolean checkPassword(String passwordAttempt){
        byte[] hashAttempt = generateHash(passwordAttempt);
        boolean permission = Arrays.equals(hashAttempt,this.passwordHash);
        return permission;
    }
 
}

