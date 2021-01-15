package main;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.file.FileSystems;
import java.util.Date;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.text.DefaultCaret;

@SuppressWarnings("serial")
public class Client extends JFrame implements ActionListener, KeyListener, Constants {
	String uname;
	PrintWriter pw;
	BufferedReader br;
	JTextArea taMessages, cardView, centerView, potView, balanceView, currentPlayer;
	JTextField tfInput;
	JButton btnSend, btnExit;
	Socket client;
	Font font = new Font("Segoe UI Symbol", Font.PLAIN, 20);
	boolean isAdmin;

	public Client(String uname, String servername, boolean isAdmin) throws Exception {
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
		//Send Button
		btnSend = new JButton("Send");
		btnSend.setBackground(Color.black);
		btnSend.setForeground(Color.yellow);
		btnSend.setPreferredSize(new Dimension(80, 46));
		btnSend.setBorder(BorderFactory.createMatteBorder(3, 3, 3, 3, Color.white));
		btnSend.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				pw.println(tfInput.getText());
			}
		});
		
		//Exit Button
		btnExit = new JButton("Exit");
		btnExit.setBackground(Color.black);
		btnExit.setForeground(Color.red);
		btnExit.setBorder(BorderFactory.createMatteBorder(3, 3, 3, 3, Color.white));
		btnExit.setPreferredSize(new Dimension(80, 46));
		btnExit.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				pw.println("leave");
				System.exit(0);
			}
		});
		
		//Messages View Text Area
		taMessages = new JTextArea();
		taMessages.setBackground(Color.black);
		taMessages.setForeground(Color.white);
		taMessages.setRows(20);
		taMessages.setColumns(52);
		taMessages.setEditable(false);
		taMessages.setFont(font);
		taMessages.setWrapStyleWord(true);
		taMessages.setLineWrap(true);
		taMessages.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		DefaultCaret caret = (DefaultCaret) taMessages.getCaret();
		caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
		
		//Scroll Pane
		JScrollPane sp = new JScrollPane(taMessages, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		sp.getVerticalScrollBar().setUI(new BasicScrollBarUI() {
			protected void configureScrollBarColors() {
				this.thumbColor = Color.gray;
				this.trackColor = Color.lightGray;
			}
		});
		sp.setBorder(BorderFactory.createMatteBorder(10, 10, 10, 10, Color.decode("#2B2D2F")));
		
		//Player Hand View
		cardView = new JTextArea();
		cardView.setText("Your Hand:\n" + DEFAULT_HAND);
		cardView.setBackground(Color.black);
		cardView.setForeground(Color.white);
		cardView.setEditable(false);
		cardView.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(3, 3, 3, 3, Color.white), BorderFactory.createEmptyBorder(5, 5, 5, 5)));
		
		//Center Cards View
		centerView = new JTextArea();
		centerView.setText("Center:\n" + DEFAULT_CENTER);
		centerView.setBackground(Color.black);
		centerView.setForeground(Color.white);
		centerView.setEditable(false);
		centerView.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(3, 3, 3, 3, Color.white), BorderFactory.createEmptyBorder(5, 5, 5, 5)));
		
		//Player balance view
		balanceView = new JTextArea();
		balanceView.setFont(font);
		balanceView.setText("Balance\n");
		balanceView.setBackground(Color.black);
		balanceView.setForeground(Color.white);
		balanceView.setEditable(false);
		balanceView.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(3, 3, 3, 3, Color.white), BorderFactory.createEmptyBorder(5, 5, 5, 5)));
		balanceView.setLineWrap(true);
		balanceView.setWrapStyleWord(true);
		
		//Pot view
		potView = new JTextArea();
		potView.setFont(font);
		potView.setText("Pot\n");
		potView.setBackground(Color.black);
		potView.setForeground(Color.white);
		potView.setEditable(false);
		potView.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(3, 3, 3, 3, Color.white), BorderFactory.createEmptyBorder(5, 5, 5, 5)));
		potView.setLineWrap(true);
		potView.setWrapStyleWord(true);
		
		//Current Player
		currentPlayer = new JTextArea();
		currentPlayer.setFont(font);
		currentPlayer.setText("Current Player\n");
		currentPlayer.setBackground(Color.black);
		currentPlayer.setForeground(Color.white);
		currentPlayer.setEditable(false);
		currentPlayer.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(3, 3, 3, 3, Color.white), BorderFactory.createEmptyBorder(5, 5, 5, 5)));
		currentPlayer.setLineWrap(true);
		currentPlayer.setWrapStyleWord(true);
		
		//Textfield to enter chat messages
		tfInput = new JTextField(50);
		if (!uname.equals("admin")) {
			tfInput.setEditable(false);
			tfInput.setBackground(Color.LIGHT_GRAY);
			tfInput.setText("User input disabled. Wait for your turn!");
		}
		tfInput.setPreferredSize(new Dimension(300, 46));
		tfInput.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(3, 3, 3, 3, Color.white), BorderFactory.createEmptyBorder(5, 5, 5, 5)));
		tfInput.requestFocusInWindow();
		tfInput.addKeyListener(this);
		
		//Right JPanel
		JPanel rp = new JPanel(new BorderLayout());
		rp.add(potView, "North");
		rp.add(currentPlayer, "Center");
		rp.add(balanceView, "South");
		rp.setBorder(BorderFactory.createMatteBorder(3, 3, 3, 14, Color.decode("#2B2D2F")));
		
		//Top JPanel contains the Scroll Pane which contains taMessages
		JPanel tp = new JPanel(new FlowLayout());
		tp.add(sp);
		tp.add(rp);
		tp.setBackground(Color.decode("#2B2D2F"));
		tp.setBorder(BorderFactory.createMatteBorder(10, 10, 10, 10, Color.blue));
		
		//Bottom JPanel contains the rest
		JPanel bp = new JPanel(new FlowLayout());
		bp.add(cardView);
		bp.add(centerView);
		bp.add(tfInput);
		bp.add(btnSend);
		bp.add(btnExit);
		bp.setBackground(Color.black);
		bp.setBorder(BorderFactory.createMatteBorder(5, 5, 5, 5, Color.black));
		
		
		//Main Frame
		ImageIcon img = new ImageIcon(FileSystems.getDefault().getPath("").toAbsolutePath() + "\\src\\poker\\images\\icon.png");
		setIconImage(img.getImage());
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent windowEvent) {
				pw.println("leave");
				System.exit(0);
			}
		});
		setExtendedState(java.awt.Frame.MAXIMIZED_BOTH);
		setResizable(false);
		setLayout(new BorderLayout(0, 0));
		setFont(font);
		setPreferredSize(new Dimension(1175, 700));
		setLocationRelativeTo(null);
		add(tp, "North");
		add(bp, "South");
		setTitle("Poker Game - " + uname);
		pack();	
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		setLocation(dim.width/2-getSize().width/2, dim.height/2-getSize().height/2);
		setVisible(true);
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
					new Client(name, servername, true);
				} catch (Exception e) {
					System.out.println("Error occured creating the admin user: " + e.getMessage());
				}
			} else {
				JOptionPane.showMessageDialog(null, "Wrong password.");
				System.exit(0);
			}
		} else {
			try {
				new Client(name, servername, false);
			} catch (Exception ex) {
				JOptionPane.showMessageDialog(null,
						"An exception has been raised:\n" + ex.getMessage() + "\nProgram will now terminate.");
			}
		}
	}

	// inner class for Messages Thread
	class MessagesThread extends Thread {
		public void run() {
			String line;
			try {
				while (true) {
					line = br.readLine();
					if (line.contains("DATA")) {
						Date time = new Date();
						System.out.println(time.toString());
						
						System.out.println(line);
						String pot = line.substring(line.indexOf(":") + 1, line.indexOf(";"));
						String center = line.substring(line.indexOf(";") + 1, line.indexOf("~"));			
						String currentPlayerName = line.substring(line.indexOf("~") + 1, line.indexOf("^"));
						String balance = line.substring(line.indexOf("^") + 1, line.indexOf(">"));
						String hand = line.substring(line.indexOf(">") + 1);
						
						pot = pot.substring(pot.indexOf(":") + 1);
						center = center.substring(center.indexOf(":") + 1);
						currentPlayerName = currentPlayerName.substring(currentPlayerName.indexOf(":") + 1);
						balance = balance.substring(balance.indexOf(":") + 1);
						hand = hand.substring(hand.indexOf(":") + 1);
						
						System.out.println("Pot: " + pot);
						System.out.println("Center: " + center);
						System.out.println("Player Name: " + currentPlayerName);
						System.out.println("Balance: " + balance);
						System.out.println("Hand: " + hand);
						
						potView.setText("Pot:\n" + pot);
						balanceView.setText("Your Balance:\n" + balance);
						currentPlayer.setText("Current Player:\n" + currentPlayerName);
						cardView.setText("Your Hand:\n" + hand);
						centerView.setText("Center:\n" + center);
						continue;
					}
					if (line.equals("Privately to you: allow send button")) { // enable send button
						for (ActionListener a : btnSend.getActionListeners()) {
							btnSend.removeActionListener(a);
						}
						for (KeyListener a : tfInput.getKeyListeners()) {
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


							}

						});
						tfInput.setBackground(Color.WHITE);
						tfInput.setText("");
						tfInput.setEditable(true);
						continue;
					}
					if (line.equals("Privately to you: disable send button")) {
						for (ActionListener a : btnSend.getActionListeners()) {
							btnSend.removeActionListener(a);
						}
						for (KeyListener a : tfInput.getKeyListeners()) {
							tfInput.removeKeyListener(a);
						}
						tfInput.setBackground(Color.LIGHT_GRAY); // grey out area
						tfInput.setText("User input disabled. Wait for your turn!");
						tfInput.setEditable(false);
						continue;
					}
					if (line.contains(uname + ": is duplicate!")) {
						System.out.println(line);
						JOptionPane.showMessageDialog(null,
								"Sorry, someone on the server already has this name. Please join again with another name!");
						System.exit(0);
					}
					line = line.substring(line.indexOf(":") + 1);
					taMessages.append(line + "\n");
				} // end of while
			} catch (Exception ex) {
				ex.printStackTrace();
				System.out.println(ex.getMessage());
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


	}

	@Override
	public void actionPerformed(ActionEvent e) {

		
	}
} // end of client
