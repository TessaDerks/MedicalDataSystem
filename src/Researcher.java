import java.security.*;
import javax.crypto.*;
import java.util.Base64;

class Researcher extends User {
    Researcher(String id, String password) {
        super(id, "Researcher", password);
    }

    public String decryptData(String encryptedData) {
        try {
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.DECRYPT_MODE, this.privateKey);
            return new String(cipher.doFinal(Base64.getDecoder().decode(encryptedData)));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean verifySignature(String data, String signature, PublicKey medicalStaffKey) {
        try {
            Signature sig = Signature.getInstance("SHA256withRSA");
            sig.initVerify(medicalStaffKey);
            sig.update(data.getBytes());
            return sig.verify(Base64.getDecoder().decode(signature));
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}

