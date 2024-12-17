import java.security.*;
import java.util.*;
import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class MedicalDataSystem {

    /* 
    public static void main(String[] args) {
        try {
            Server server = new Server();

            // Create Users
            MedicalStaff staff = new MedicalStaff("staff1");
            Researcher researcher = new Researcher("researcher1");

            server.registerUser(staff);
            server.registerUser(researcher);

            // Medical Staff Encrypts and Signs Data
            String medicalData = "Sensitive Patient Data";
            String encryptedData = staff.encryptData(medicalData, researcher.publicKey);
            String signature = staff.signData(medicalData);

            // Store Data on Server
            Set<String> allowedResearchers = new HashSet<>();
            allowedResearchers.add(researcher.id);
            server.storeData("data1", encryptedData, signature, staff.id, allowedResearchers);

            // Researcher Fetches and Verifies Data
            String fetchedEncryptedData = server.fetchData("data1", researcher.id);
            String decryptedData = researcher.decryptData(fetchedEncryptedData);

            if (researcher.verifySignature(decryptedData, server.getSignature("data1"), staff.publicKey)) {
                System.out.println("Data verified and decrypted: " + decryptedData);
            } else {
                System.out.println("Data integrity verification failed.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
        */

    private static Server server = new Server();
    private static MedicalStaff loggedInStaff;
    private static Researcher loggedInResearcher;
    static String role;

    private static final Map<String, String> credentials = new HashMap<>() {{
        put("staff1:MedicalStaff", "password1");
        put("researcher1:Researcher", "password2");
    }};

    public static void main(String[] args) {
        // Create Users
        MedicalStaff staff = new MedicalStaff("staff1");
        Researcher researcher = new Researcher("researcher1");

        server.registerUser(staff);
        server.registerUser(researcher);

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
        //rb1.setBounds(200,50,100,30);      
        JRadioButton rb2=new JRadioButton("Researcher");    
        //rb2.setBounds(200,100,100,30);    
        ButtonGroup group=new ButtonGroup();    
        group.add(rb1);group.add(rb2);    
        //b.setBounds(100,150,80,30);    
        //group.addActionListener(this);    
        //add(rb1);add(rb2);add(b);    

        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String userId = userField.getText();
                String password = new String(passField.getPassword());
                //String role = userId.startsWith("staff") ? "MedicalStaff" : "Researcher";
                if(rb1.isSelected()){
                    role = "MedicalStaff";
                }
                else if(rb2.isSelected()){
                    role = "Researcher";
                }

                if (credentials.get(userId + ":" + role) != null && credentials.get(userId + ":" + role).equals(password)) {
                    User user = server.authenticateUser(userId, role);
                    if (role.equals("MedicalStaff")) {
                        loggedInStaff = (MedicalStaff) user;
                        loginFrame.dispose();
                        medicalStaffGUI();
                    } else {
                        loggedInResearcher = (Researcher) user; //switch to different screen!!
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
        //loginPanel.add(group);
        loginPanel.add(userLabel);
        loginPanel.add(userField);
        loginPanel.add(passLabel);
        loginPanel.add(passField);
        loginPanel.add(new JLabel()); // Spacer
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

        JButton encryptButton = new JButton("Encrypt and Store Data");
        encryptButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(loggedInStaff == null){
                    JOptionPane.showMessageDialog(staffFrame, "Only medical staff can store data.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                String medicalData = dataField.getText();
                if (medicalData.isEmpty()) {
                    JOptionPane.showMessageDialog(staffFrame, "Please enter medical data.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                String encryptedData = loggedInStaff.encryptData(medicalData, loggedInResearcher.publicKey);  //hier wat aanpassen dat het gekozen lijstje is?
                String signature = loggedInStaff.signData(medicalData);

                Set<String> allowedResearchers = new HashSet<>();
                allowedResearchers.add(loggedInResearcher.id);
                server.storeData("data1", encryptedData, signature, loggedInStaff.id, allowedResearchers);

                JOptionPane.showMessageDialog(staffFrame, "Data encrypted and stored successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                dataField.setText("");
            }
        });

        staffPanel.add(label);
        staffPanel.add(dataField);
        staffPanel.add(encryptButton);
        //panel.add(decryptButton);

        staffFrame.getContentPane().add(staffPanel);
        staffFrame.setVisible(true);
    }

    private static void researcherGUI(){
        JFrame reseacherFrame = new JFrame("Medical Data Access System");
        reseacherFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        reseacherFrame.setSize(600, 400);

        JPanel researcherPanel = new JPanel();
        researcherPanel.setLayout(new BoxLayout(researcherPanel, BoxLayout.Y_AXIS));

        JButton decryptButton = new JButton("Fetch and Decrypt Data");
        decryptButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (loggedInResearcher == null) {
                    JOptionPane.showMessageDialog(reseacherFrame, "Only researchers can fetch data.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                try {
                    String fetchedEncryptedData = server.fetchData("data1", loggedInResearcher.id);
                    String decryptedData = loggedInResearcher.decryptData(fetchedEncryptedData);

                    if (loggedInResearcher.verifySignature(decryptedData, server.getSignature("data1"), loggedInStaff.publicKey)) { // think I need to change something here so it goes with multiple keys
                        JOptionPane.showMessageDialog(reseacherFrame, "Data decrypted and verified: " + decryptedData, "Success", JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(reseacherFrame, "Data integrity verification failed.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (SecurityException ex) {
                    JOptionPane.showMessageDialog(reseacherFrame, ex.getMessage(), "Access Denied", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        researcherPanel.add(decryptButton);

        reseacherFrame.getContentPane().add(researcherPanel);
        reseacherFrame.setVisible(true);
    }
}

