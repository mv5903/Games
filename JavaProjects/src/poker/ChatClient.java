package poker;

import java.io.*;
import java.util.*;
import java.net.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import static java.lang.System.out;

@SuppressWarnings("serial")
public class ChatClient extends JFrame implements ActionListener, KeyListener, Constants {
    String uname;
    PrintWriter pw;
    BufferedReader br;
    JTextArea  taMessages;
    JTextField tfInput;
    JButton btnSend,btnExit;
    Socket client;
    final static String adminPassword = "Matt5903!";
    boolean isAdmin;
    
    public ChatClient(String uname, String servername, boolean isAdmin) throws Exception {
        super(uname);  // set title for frame
        this.isAdmin = isAdmin;
        this.uname = uname;
        client  = new Socket(servername,9999);
        br = new BufferedReader(new InputStreamReader(client.getInputStream()));
        pw = new PrintWriter(client.getOutputStream(), true);
        pw.println(uname);  // send name to server
        buildInterface();
        new MessagesThread().start();  // create thread for listening for messages
    }
    
    public void buildInterface() {
        btnSend = new JButton("Send");
        btnExit = new JButton("Exit");
        taMessages = new JTextArea();
        taMessages.setRows(10);
        taMessages.setColumns(50);
        taMessages.setEditable(false);
        tfInput  = new JTextField(50);
        JScrollPane sp = new JScrollPane(taMessages, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        add(sp,"Center");
        JPanel bp = new JPanel( new FlowLayout());
        bp.add(tfInput);
        bp.add(btnSend);
        bp.add(btnExit);
        add(bp,"South");
        btnSend.addActionListener(this);
        btnExit.addActionListener(this);
        setSize(500,300);
        setVisible(true);
        tfInput.addKeyListener(this);
        addWindowListener(new WindowAdapter() {
        	@Override
        	public void windowClosing(WindowEvent windowEvent) {
        		pw.println(uname + ": has left the game!");
        		pw.println("end");  // send end to server so that server know about the termination
                System.exit(0);
        	}
        });
        setTitle("Poker Game");
        pack();
        tfInput.requestFocusInWindow();
    }
    
    public void actionPerformed(ActionEvent evt) {
        if (evt.getSource() == btnExit) {
            pw.println("end");  // send end to server so that server know about the termination
            System.exit(0);
        } else {
            // send message to server
            pw.println(tfInput.getText());
        }
    }
    
    public static void main(String[] args) {
    	// request username list from server 
    	String servername = JOptionPane.showInputDialog(null, "Enter the ip address of whom you are connecting to: ", "IP Address", JOptionPane.PLAIN_MESSAGE);
        // take username from user
        String name = JOptionPane.showInputDialog(null, "Enter your name: ", "Username", JOptionPane.PLAIN_MESSAGE);
        String password;
        if (name.equalsIgnoreCase("admin")) {
        	password = JOptionPane.showInputDialog(null, "Enter admin password: ", "Password", JOptionPane.WARNING_MESSAGE);
        	if (password.equals(adminPassword)) {
        		try {
        			new ChatClient(name, servername, true);
        		} catch (Exception e) {
        			out.println("Error occured creating the admin user: " + e.getMessage());
        		}
        	} else {
        		JOptionPane.showMessageDialog(null, "Wrong password.");
        		System.exit(0);
        	}
        }
        
        //NetIndex.DELETE(name);
        
        try {
            new ChatClient(name,servername, false);
        } catch(Exception ex) {
            JOptionPane.showMessageDialog(null, "An exception has been raised:\n" + ex.getMessage() + "\nProgram will now terminate.");
        }
        
    } // end of main
    
    // inner class for Messages Thread
    class MessagesThread extends Thread {
        public void run() {
            String line;
            try {
                while(true) {
                    line = br.readLine();
                    if (line.contains(uname + "-privately: duplicate user name exists")) {
                    	System.out.println(line);
                    	JOptionPane.showMessageDialog(null, "Sorry, someone on the server already has this name. Please join again with another name!");
                    	System.exit(0);
                    }
                    taMessages.append(line + "\n");
                } // end of while
            } catch(Exception ex) {}
        }
    }

	@Override
	public void keyTyped(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void keyPressed(KeyEvent e) {
		int key = e.getKeyCode();
		if (key == KeyEvent.VK_ENTER) {
			pw.println(tfInput.getText());
			tfInput.setText("");
		}
		
	}

	@Override
	public void keyReleased(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}
} //  end of client
