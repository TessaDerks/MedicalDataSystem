import java.security.SecureRandom;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

public final class cryptographyMethods {

    public SecretKey generateAESKey(){
        SecureRandom securerandom = new SecureRandom();
        try {
            KeyGenerator keygenerator = KeyGenerator.getInstance("AES");
            // Initializing the KeyGenerator with 256 bits.  IS THAT SAFE ENOUGH?
            keygenerator.init(256, securerandom);
            SecretKey key = keygenerator.generateKey();
            return key;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // create salt / initialization vector
    public byte[] getSalt(){
        SecureRandom random = new SecureRandom();
        byte[] salt= new byte[16];
        random.nextBytes(salt);
        return salt;
    }

    public byte[] getAESEncryption(String plainText, SecretKey secretKey, byte[] iv)
    {
        try {
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
 
            IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);
    
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivParameterSpec);
    
            return cipher.doFinal(plainText.getBytes());
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        
    }
 

    public String getAESDecryption(byte[] cipherText,SecretKey secretKey,byte[] iv) 
    {
        try {
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
 
            IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);

            cipher.init(Cipher.DECRYPT_MODE, secretKey, ivParameterSpec);
    
            byte[] result = cipher.doFinal(cipherText);
    
            return new String(result);

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        
    }
}
