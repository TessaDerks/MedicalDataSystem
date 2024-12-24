import java.util.*;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.nio.charset.StandardCharsets;


public class MedicalDataSystem {

    private static cryptographyMethods crypt = new cryptographyMethods();
    private static Server server = new Server();
    private static MedicalStaff loggedInStaff;
    private static Researcher loggedInResearcher;
    static String role;
    private static int dataID = 300;

    public static void main(String[] args) {
        // Create Users >>>> have file with users instead of creating them each time?
        server.registerUser(new MedicalStaff("124361", "cryptographyCourse5"));
        server.registerUser(new Researcher("271724", "trentoUni8")); // Franklin
        server.registerUser(new Researcher("249201", "computerScience9")); // Ehrlich
        server.registerUser(new Researcher("200613", "examSeason3")); // Blackwell

        server.registerUser(new MedicalStaff("1234", "zucht"));

        // Launch GUI
        SwingUtilities.invokeLater(MedicalDataSystem::loginGUI);
    }

    private static void loginGUI() {
        JFrame loginFrame = new JFrame("Login");
        loginFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        loginFrame.setSize(400, 300);

        JPanel loginPanel = new JPanel(new GridLayout(4, 2));

        JLabel userLabel = new JLabel("     User ID:");
        JTextField userField = new JTextField();
        JLabel passLabel = new JLabel("     Password:");
        JPasswordField passField = new JPasswordField();
        JButton loginButton = new JButton("Login");
           
        JRadioButton rb1=new JRadioButton("Medical staff");     
        JRadioButton rb2=new JRadioButton("Researcher");       
        ButtonGroup group=new ButtonGroup();    
        group.add(rb1);group.add(rb2);      

        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String userId = userField.getText();
                String password = new String(passField.getPassword());

                if(rb1.isSelected()){
                    role = "MedicalStaff";
                }
                else if(rb2.isSelected()){
                    role = "Researcher";
                }

                User logInAttempt = server.authenticateUser(userId, password, role);
                if(logInAttempt!= null){
                    if (role.equals("MedicalStaff")) {
                        loggedInStaff = (MedicalStaff) logInAttempt;
                        loginFrame.dispose();
                        medicalStaffGUI();
                    } else {
                        loggedInResearcher = (Researcher) logInAttempt; 
                        loginFrame.dispose();
                        researcherGUI();
                    }
                    
                } else {
                    JOptionPane.showMessageDialog(loginFrame, "Invalid credentials!", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        loginPanel.add(rb1);
        loginPanel.add(rb2);
        loginPanel.add(userLabel);
        loginPanel.add(userField);
        loginPanel.add(passLabel);
        loginPanel.add(passField);
        loginPanel.add(new JLabel()); 
        loginPanel.add(loginButton);

        loginFrame.add(loginPanel);
        loginFrame.setVisible(true);

    }

    private static void medicalStaffGUI() {
        JFrame staffFrame = new JFrame("Medical Data Input System");
        staffFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        staffFrame.setSize(600, 400);

        JPanel staffPanel = new JPanel();
        staffPanel.setLayout(new BoxLayout(staffPanel, BoxLayout.Y_AXIS));

        JLabel label = new JLabel("Enter Medical Data:");
        JTextField dataField = new JTextField(20);

        // for now an easy implementation of selecting researchers to have access
        JRadioButton r1_access=new JRadioButton("R. Franklin");     
        JRadioButton r2_access=new JRadioButton("P. Ehrlich");   
        JRadioButton r3_access=new JRadioButton("E. Blackwell");

        JButton encryptButton = new JButton("Store Data");
        encryptButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(loggedInStaff == null){
                    JOptionPane.showMessageDialog(staffFrame, "Only medical staff can store data.", "Error", JOptionPane.ERROR_MESSAGE);
                }
                else{
                    String medicalData = dataField.getText();
                    // check if there is data to be encrypted
                    if (medicalData.isEmpty()) {
                        JOptionPane.showMessageDialog(staffFrame, "Please enter data.", "Error", JOptionPane.ERROR_MESSAGE);
                    } // check if the medical staff user selected researchers to have access
                    else if(!(r1_access.isSelected()|r2_access.isSelected()|r3_access.isSelected())){
                        JOptionPane.showMessageDialog(staffFrame, "Please select who can have access", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                    else{
                        // first get private symmetric key, then encrypt medical data with that key, store medical data with signature
                        SecretKey aesKey = crypt.generateAESKey();
                        byte[] aesIV = crypt.getSalt();
                        byte[] encryptedMedicalData = crypt.getAESEncryption(medicalData, aesKey, aesIV);
                        byte[] signature = loggedInStaff.signData(medicalData);
                        server.storeData(dataID, encryptedMedicalData, signature, loggedInStaff.getId());

                        Set<String> allowedResearchers = new HashSet<>();
                        if(r1_access.isSelected()){
                            allowedResearchers.add("271724");}
                        if(r2_access.isSelected()){
                            allowedResearchers.add("249201");}
                        if(r3_access.isSelected()){
                            allowedResearchers.add("200613");}
                        


                        // then store private symmetric key encrypted with the public key of the researchers that are granted access
                        for (String researcher : allowedResearchers) {
                            byte[] encrypted_key = loggedInStaff.encryptData(aesKey.getEncoded(), server.getUserPublicKey(researcher));
                            server.storeResearcherKey(researcher, dataID, encrypted_key, aesIV);
                        } 

                        
                        dataID++;

                        JOptionPane.showMessageDialog(staffFrame, "Data encrypted and stored successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                        dataField.setText("");
                    }
                    
                }
                
            }
        });

        JButton logoutButton = new JButton("Log out");
        logoutButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                loggedInStaff = null;
                staffFrame.dispose();
                loginGUI();
            }
        });

        staffPanel.add(label);
        staffPanel.add(dataField);
        staffPanel.add(r1_access);
        staffPanel.add(r2_access);
        staffPanel.add(r3_access);
        staffPanel.add(encryptButton);
        staffPanel.add(logoutButton);

        staffFrame.getContentPane().add(staffPanel);
        staffFrame.setVisible(true);  
    }


    private static void researcherGUI(){
        JFrame reseacherFrame = new JFrame("Medical Data Access System");
        reseacherFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        reseacherFrame.setSize(600, 400);

        JPanel researcherPanel = new JPanel();
        researcherPanel.setLayout(new BoxLayout(researcherPanel, BoxLayout.Y_AXIS));

        String options[] = server.getOptions(loggedInResearcher.id);
        if(options.length == 0){
            JOptionPane.showMessageDialog(reseacherFrame, "No data available", "Error", JOptionPane.ERROR_MESSAGE);
        }
        JComboBox<String> dataOptions = new JComboBox<>(options);

        JButton decryptButton = new JButton("Fetch and Decrypt Data");
        decryptButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (loggedInResearcher == null) {
                    JOptionPane.showMessageDialog(reseacherFrame, "Only researchers can fetch data.", "Error", JOptionPane.ERROR_MESSAGE);
                }
                else{
                    String selectedData;
                    try {
                        selectedData = (String) dataOptions.getSelectedItem();
                        }
                        catch (NumberFormatException error) {
                        selectedData = "0";
                        }
                    
                    // get encrypted key and IV for AES
                    ArrayList<byte[]> keyAndIV = server.getEncryptedKey(loggedInResearcher.id, selectedData);
                    byte[] encryptedKey = keyAndIV.get(0);
                    byte[] iv = keyAndIV.get(1);

                    // decrypt key
                    byte[] decryptedKey = loggedInResearcher.decryptKey(encryptedKey);
                    // get encrypted data
                    int selectedDataId = Integer.parseInt(selectedData);
                    byte[] encryptedData = server.getEncryptedData(selectedDataId);
                    //use decrypted key to decrypt data
                    String decryptedData = crypt.getAESDecryption(encryptedData,new SecretKeySpec(decryptedKey, 0, decryptedKey.length, "AES") , iv);
                    
                    // obtain signature of data
                    ArrayList<byte[]> signatureAndUserId = server.getSignature(selectedDataId);
                    byte[] signature = signatureAndUserId.get(0);
                    String medicalStaffId = new String(signatureAndUserId.get(1),StandardCharsets.UTF_8);

                    //verify the integrity of data and if verified, display
                    if (loggedInResearcher.verifySignature(decryptedData, signature, server.getUserPublicKey(medicalStaffId))) { 
                        JOptionPane.showMessageDialog(reseacherFrame, "Data decrypted and verified: " + decryptedData, "Success", JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(reseacherFrame, "Data integrity verification failed.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
                
            }
        });

        JButton logoutButton = new JButton("Log out");
        logoutButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                loggedInResearcher = null;
                reseacherFrame.dispose();
                loginGUI();
            }
        });

        researcherPanel.add(dataOptions);
        researcherPanel.add(decryptButton);
        researcherPanel.add(logoutButton);

        reseacherFrame.getContentPane().add(researcherPanel);
        reseacherFrame.setVisible(true);
    }

    
}

