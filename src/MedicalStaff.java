import java.security.*;
import javax.crypto.*;

class MedicalStaff extends User {
    MedicalStaff(String id, String password) {
        super(id, "MedicalStaff", password);
    }

    // used to encrypt the secret key
    public byte[] encryptKey(byte[] data, PublicKey researcherKey) {
        try {
            Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding");
            cipher.init(Cipher.ENCRYPT_MODE, researcherKey);
            return cipher.doFinal(data);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public byte[] signData(String data) {
        try {
            Signature signature = Signature.getInstance("SHA256withRSA");
            signature.initSign(this.privateKey);
            signature.update(data.getBytes());
            return signature.sign();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public String getId(){
        return this.id;
    }
}
