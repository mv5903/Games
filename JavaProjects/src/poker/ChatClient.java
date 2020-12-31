package poker;

import java.io.*;
import java.util.*;
import java.net.*;
import javax.swing.*;
import javax.swing.text.html.HTML;

import java.awt.*;
import java.awt.event.*;
import static java.lang.System.out;

@SuppressWarnings("serial")
public class ChatClient extends JFrame implements ActionListener, KeyListener, Constants {
	String uname;
	PrintWriter pw;
	BufferedReader br;
	JTextArea taMessages;
	JTextField tfInput;
	JButton btnSend, btnExit;
	Socket client;
	final static String adminPassword = "Matt5903!";
	boolean isAdmin;

	public ChatClient(String uname, String servername, boolean isAdmin) throws Exception {
		super(uname); // set title for frame
		this.isAdmin = isAdmin;
		this.uname = uname;
		client = new Socket(servername, 9999);
		br = new BufferedReader(new InputStreamReader(client.getInputStream(), "UTF-8"));
		pw = new PrintWriter(client.getOutputStream(), true);
		pw.println(uname); // send name to server
		buildInterface();
		new MessagesThread().start(); // create thread for listening for messages
	}

	public void buildInterface() {
		btnSend = new JButton("Send");
		btnExit = new JButton("Exit");
		taMessages = new JTextArea();
		taMessages.setRows(10);
		taMessages.setColumns(50);
		taMessages.setEditable(false);
		taMessages.setFont(new Font("Segoe UI Symbol", Font.PLAIN, 18));
		tfInput = new JTextField(50);
		tfInput.setEditable(false);
		tfInput.setBackground(Color.LIGHT_GRAY); //grey out area
		tfInput.setText("User input disabled.");
		JScrollPane sp = new JScrollPane(taMessages, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		add(sp, "Center");
		setExtendedState(java.awt.Frame.MAXIMIZED_BOTH);
		JPanel bp = new JPanel(new FlowLayout());
		bp.add(tfInput);
		bp.add(btnSend);
		bp.add(btnExit);
		add(bp, "South");
		if (uname.equals("admin")) {
			btnSend.addActionListener(this);
			tfInput.addKeyListener(this);
			tfInput.setBackground(Color.WHITE);
			tfInput.setText("");
			tfInput.setEditable(true);
		}
		btnExit.addActionListener(this);
		setSize(500, 300);
		setVisible(true);
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent windowEvent) {
				pw.println("has left the game!");
				pw.println("end");
				System.exit(0);
			}
		});
		setTitle("Poker Game: " + uname);
		setFont(new Font("Segoe UI Symbol", Font.PLAIN, 18));
		pack();
		tfInput.requestFocusInWindow();
	}

	public void actionPerformed(ActionEvent evt) {
		if (evt.getSource() == btnExit) {
			pw.println("end"); // send end to server so that server know about the termination
			System.exit(0);
		} else {
			// send message to server
			pw.println(tfInput.getText());
		}
	}

	public void send(String text) {
		pw.println(text);
	}

	public void sendPrivately(String user, String message) {
		pw.println("PRIVATE TO " + user + ": " + message);
	}

	public static void main(String[] args) {
		// request username list from server
		String servername = JOptionPane.showInputDialog(null, "Enter the ip address of whom you are connecting to: ",
				"IP Address", JOptionPane.PLAIN_MESSAGE);
		// take username from user
		String name = JOptionPane.showInputDialog(null, "Enter your name: ", "Username", JOptionPane.PLAIN_MESSAGE);
		String password;
		if (name.equalsIgnoreCase("admin")) {
			password = JOptionPane.showInputDialog(null, "Enter admin password: ", "Password",
					JOptionPane.WARNING_MESSAGE);
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
		} else {
			try {
				new ChatClient(name, servername, false);
			} catch (Exception ex) {
				JOptionPane.showMessageDialog(null,
						"An exception has been raised:\n" + ex.getMessage() + "\nProgram will now terminate.");
			}
		}

		// NetIndex.DELETE(name);

	} // end of main

	// inner class for Messages Thread
	class MessagesThread extends Thread {
		public void run() {
			String line;
			try {
				while (true) {
					line = br.readLine();
					if (line.equals("Privately to you: allow send button")) { //enable send button
						for (ActionListener a: btnSend.getActionListeners()) {
							btnSend.removeActionListener(a);
						}
						for (KeyListener a: tfInput.getKeyListeners()) {
							tfInput.removeKeyListener(a);
						}
						btnSend.addActionListener(new ActionListener() {

							@Override
							public void actionPerformed(ActionEvent e) {
								if (e.getSource() == btnExit) {
									pw.println("end"); // send end to server so that server know about the termination
									System.exit(0);
								} else {
									// send message to server
									pw.println(tfInput.getText());
								}
							}
							
						});
						tfInput.addKeyListener(new KeyListener() {

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
							
						});
						tfInput.setBackground(Color.WHITE);
						tfInput.setText("");
						tfInput.setEditable(true);
						continue;
					}
					if (line.equals("Privately to you: disable send button")) {
						for (ActionListener a: btnSend.getActionListeners()) {
							btnSend.removeActionListener(a);
						}
						for (KeyListener a: tfInput.getKeyListeners()) {
							tfInput.removeKeyListener(a);
						}
						tfInput.setBackground(Color.LIGHT_GRAY); //grey out area
						tfInput.setText("User input disabled.");
						tfInput.setEditable(false);
						continue;
					}
					if (line.contains(uname + "-privately: duplicate user name exists")) {
						System.out.println(line);
						JOptionPane.showMessageDialog(null,
								"Sorry, someone on the server already has this name. Please join again with another name!");
						System.exit(0);
					}
					taMessages.append(line + "\n");
				} // end of while
			} catch (Exception ex) {
			}
		}

		public String waitForResponse(String username) {
			try {
				while (true) {
					String line = br.readLine();
					if (line.substring(0, line.indexOf(":")).equals(username)) {
						return line.substring(line.indexOf(":") + 1);
					}
				}
			} catch (Exception ex) {
				return "Couldn't get a reponse";
			}
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
} // end of client
