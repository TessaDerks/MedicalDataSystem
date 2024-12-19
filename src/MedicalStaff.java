import java.security.*;
import javax.crypto.*;
import java.util.Base64;

class MedicalStaff extends User {
    MedicalStaff(String id, String password) {
        super(id, "MedicalStaff", password);
    }


    // used to encrypt the secret key
    public String encryptData(byte[] data, PublicKey researcherKey) {
        try {
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.ENCRYPT_MODE, researcherKey);
            return Base64.getEncoder().encodeToString(cipher.doFinal(data));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public String signData(String data) {
        try {
            Signature signature = Signature.getInstance("SHA256withRSA");
            signature.initSign(this.privateKey);
            signature.update(data.getBytes());
            return Base64.getEncoder().encodeToString(signature.sign());
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public String getId(){
        return this.id;
    }
}
