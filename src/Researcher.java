import java.security.*;
import javax.crypto.*;

class Researcher extends User {
    Researcher(String id, String password) {
        super(id, "Researcher", password);
    }

    public byte[] decryptKey(byte[] encryptedData) {
        try {
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.DECRYPT_MODE, this.privateKey);
            return cipher.doFinal(encryptedData);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean verifySignature(String data, byte[] signature, PublicKey medicalStaffKey) {
        try {
            Signature sig = Signature.getInstance("SHA256withRSA");
            sig.initVerify(medicalStaffKey);
            sig.update(data.getBytes());
            return sig.verify(signature);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}

