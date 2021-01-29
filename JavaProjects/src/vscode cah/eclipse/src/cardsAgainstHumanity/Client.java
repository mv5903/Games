package cardsAgainstHumanity;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.event.MouseInputAdapter;

public class Client extends JFrame implements Serializable, Constants {

	private static final long serialVersionUID = 6667457335070770460L;
	String receivedCard = "";
	String name;
	String judgeBlack = "";
	ObjectOutputStream output;
	ObjectInputStream input;
	Socket client;
	static boolean isAdmin;
	boolean isCurrentlyDealer, lookAtCardsOk = false, gameInSession = false;
	int playerCount = 0;
	ArrayList<String> blackCardTotals;
	ArrayList<String> cardsToLookAt;
	Message messageReceived = new Message("", "");
	MainUI main = new MainUI();
	JFrame mainFrame;

	public static void main(String[] args) {
		isAdmin = false;
		String servername = JOptionPane.showInputDialog(null, "Enter the IP address of whom you are connecting to:",
				"Enter IP Address", JOptionPane.PLAIN_MESSAGE);
		String name = JOptionPane.showInputDialog(null, "Enter your name:", "Name Entry", JOptionPane.PLAIN_MESSAGE);
		if (name.equalsIgnoreCase("admin")) {
			String password = JOptionPane.showInputDialog(null, "Enter the admin password: ", "Password",
					JOptionPane.WARNING_MESSAGE);
			if (!password.equals(ADMIN_PASSWORD)) {
				JOptionPane.showMessageDialog(null,
						"Sorry, you entered the wrong admin password. The program will now terminate.",
						"Wrong Password", JOptionPane.ERROR_MESSAGE);
			}
			isAdmin = true;
		}
		try {
			new Client(name, servername, isAdmin);
		} catch (Exception e) {
			System.err.println("Something went wrong when creating the user: " + e.getMessage() + "\n"
					+ e.getStackTrace().toString());
		}
	}

	public Client(String name, String servername, boolean isAdmin) throws Exception {
		this.name = name;
		client = new Socket(servername, PORT);
		output = new ObjectOutputStream(client.getOutputStream());
		output.flush();
		input = new ObjectInputStream(client.getInputStream());
		output.writeObject(new Message("name", name));
		main.setName("Main Game Thread Window");
		main.start();
		new MessageListener().start();
	}
	
	public void rest(long ms) {
		try {
			Thread.sleep(ms);
		} catch (InterruptedException e) {}
	}
	
	public void setHighlighted(JTextArea text, boolean shouldHighlight) {
		if (shouldHighlight) {
			text.setBackground(Color.LIGHT_GRAY);
			text.setFont(boldCardFont);
			text.setBorder(
					BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(5, 5, 5, 5, Color.black),
							BorderFactory.createEmptyBorder(3, 3, 3, 3)));
		} else {
			text.setBackground(Color.white);
			text.setFont(cardFont);
			text.setBorder(
					BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(2, 2, 2, 2, Color.black),
							BorderFactory.createEmptyBorder(3, 3, 3, 3)));
		}
	}

	public void setViewable(JTextArea text, boolean isVisible) {
		if (isVisible) {
			text.setBackground(Color.white);
			text.setFont(cardFont);
		} else {
			text.setBackground(Color.darkGray);
			text.setFont(italicCardFont);
		}
	}

	class MessageListener extends Thread {
		public void run() {
			try {
				while (true) {
					messageReceived = (Message) input.readObject();
					System.out.println(messageReceived);
					if (messageReceived.simple) {
						if (messageReceived.subject.equals("Player Count")) {
							gameInSession = true;
							playerCount = Integer.parseInt(messageReceived.message);
						} else if (messageReceived.subject.equals("You are judge") && messageReceived.message
								.substring(0, messageReceived.message.indexOf("$")).equals(name)) {
							judgeBlack = messageReceived.message.substring(messageReceived.message.indexOf("$") + 1);
							isCurrentlyDealer = true;
						} else if (messageReceived.subject.equals("White card from player") && isCurrentlyDealer) {
							receivedCard = messageReceived.message;
						} else if (messageReceived.subject.equals("Black Card")) {
							blackCardText.setText(messageReceived.message);
						} else if (messageReceived.subject.equals("You win") && messageReceived.message.equals(name)) {
							guideText.setText("You won the round!");
						} else if (messageReceived.subject.equals("You win") && !messageReceived.message.equals(name)) {
							guideText.setText(messageReceived.message + " has won the round!");
						} else if (messageReceived.subject.equals("Guide")) {
							guideText.setText(messageReceived.message);
						} else if (messageReceived.subject.equals("Final winner")) {
							guideText.setText("You won the game!");
						} else if (messageReceived.subject.equals("Duplicate")) {
							JOptionPane.showMessageDialog(null,
									"Sorry, someone on the server has this name. Please join again with another name.");
							System.exit(0);
						}
					} else {
						if (messageReceived.subject.equals("White Cards") && messageReceived.list.get(0).equals(name)) {
							sleep(3000);
							guideText.setText("Pick a white card the best matches the black one.");
							ArrayList<String> temp = messageReceived.list;
							temp.remove(0);
							for (int i = 0; i < cardText.length; i++) {
								cardText[i].setText(messageReceived.list.get(i));
								setHighlighted(cardText[i], false);
								setViewable(cardText[i], true);
								int loop = i;
								cardText[i].addMouseListener(new MouseInputAdapter() {
									public void mouseClicked(MouseEvent e) {
										if (gameInSession) {
											for (JTextArea text : cardText) {
												if (text.getBackground() == Color.LIGHT_GRAY) {
													setHighlighted(text, false);
												}
											}
											if (cardText[loop].getBackground() == Color.LIGHT_GRAY) {
												setHighlighted(cardText[loop], false);
											} else {
												setHighlighted(cardText[loop], true);
											}
										}
									}
								});
							}
						} else if (messageReceived.subject.equals("Totals")
								&& messageReceived.list.get(0).equals(name)) {
							ArrayList<String> temp = messageReceived.list;
							temp.remove(0);
							blackCardTotals = temp;
						} else if (messageReceived.subject.equals("Cards to judge")) {
							cardsToLookAt = messageReceived.list;
							lookAtCardsOk = true;
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	// Main Frame
	JPanel mainPanel;
	JTextArea guideText;
	JPanel blackCardPanel;
	JTextArea blackCardText;
	JPanel guideTextPanel;
	JPanel whiteCardPanel;
	JPanel[] cardPanels = new JPanel[7];
	JTextArea[] cardText = new JTextArea[7];
	JButton submitButton = new JButton("Submit");
	JButton seeCardAmounts = new JButton("See Black Card Totals");
	Font font = new Font("Comic Sans MS", Font.PLAIN, 36);
	Font cardFont = new Font("Comic Sans MS", Font.PLAIN, 20);
	Font boldCardFont = new Font("Comic Sans MS", Font.BOLD, 20);
	Font italicCardFont = new Font("Comic Sans MS", Font.ITALIC, 20);
	int chosenCard = 0;
	boolean isCardClicked = false;

	class MainUI extends Thread {

		public void run() {
			build(isAdmin);
		}

		public void build(boolean isAdmin) {
			new Judge().start();
			if (isAdmin) {
				mainFrame = new JFrame();
				submitButton = new JButton("start");
				mainPanel = new JPanel();

				submitButton.setPreferredSize(new Dimension(400, 100));
				submitButton.setText("Click to Start");
				submitButton.setFont(cardFont);
				submitButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						try {
							output.writeObject(new Message("Start", "admin"));
						} catch (Exception ex) {
							ex.printStackTrace();
						}
					}
				});

				mainPanel.setPreferredSize(new Dimension(500, 500));
				mainPanel.add(submitButton);
				mainFrame.add(mainPanel);
				mainFrame.pack();
				mainFrame.setVisible(true);

			} else {

				mainFrame = new JFrame();
				mainPanel = new JPanel(new BorderLayout(5, 5));
				guideText = new JTextArea("Waiting for game to begin...");
				guideTextPanel = new JPanel();
				whiteCardPanel = new JPanel(new FlowLayout());

				guideText.setFont(font);
				guideText.setEditable(false);

				guideTextPanel.add(guideText);

				whiteCardPanel.setVisible(true);
				whiteCardPanel.setPreferredSize(new Dimension(1000, 400));

				for (int i = 0; i < cardPanels.length; i++) {
					cardPanels[i] = new JPanel();
					cardPanels[i].setVisible(true);
					cardPanels[i].setPreferredSize(new Dimension(120, 380));
					cardText[i] = new JTextArea("This is a test message, box " + i);
					cardText[i].setWrapStyleWord(true);
					cardText[i].setLineWrap(true);
					cardText[i].setFont(cardFont);
					cardText[i].setEditable(false);
					cardText[i].setPreferredSize(new Dimension(120, 360));
					cardText[i].setBorder(
							BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(2, 2, 2, 2, Color.black),
									BorderFactory.createEmptyBorder(3, 3, 3, 3)));
					int loop = i;
					cardText[i].addMouseListener(new MouseInputAdapter() {
						public void mouseClicked(MouseEvent e) {
							if (gameInSession) {
								for (JTextArea text : cardText) {
									if (text.getBackground() == Color.LIGHT_GRAY) {
										setHighlighted(text, false);
									}
								}
								if (cardText[loop].getBackground() == Color.LIGHT_GRAY) {
									setHighlighted(cardText[loop], false);
								} else {
									setHighlighted(cardText[loop], true);
								}
							}
						}
					});
					cardPanels[i].add(cardText[i]);
					whiteCardPanel.add(cardPanels[i]);
				}

				blackCardPanel = new JPanel(new FlowLayout());
				blackCardPanel.setPreferredSize(new Dimension(1000, 250));
				blackCardPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

				blackCardText = new JTextArea("Black card text will go here.");
				blackCardText.setFont(cardFont);
				blackCardText.setPreferredSize(new Dimension(250, 250));
				blackCardText.setWrapStyleWord(true);
				blackCardText.setLineWrap(true);
				blackCardText.setBackground(Color.black);
				blackCardText.setForeground(Color.white);
				blackCardText.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3),
						BorderFactory.createMatteBorder(3, 3, 3, 3, Color.black)));

				submitButton.setBackground(Color.BLACK);
				submitButton.setForeground(Color.WHITE);
				submitButton.setPreferredSize(new Dimension(200, 100));
				submitButton.setFont(cardFont);
				submitButton.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						if (gameInSession) {
							boolean oneSelected = false;
							guideText.setText("Card selected. Please wait for the next black card.");
							for (JTextArea text : cardText) {
								if (text.getBackground() == Color.LIGHT_GRAY) {
									oneSelected = true;
									try {
										output.writeObject(
												new Message("White card from player", name + "?" + text.getText()));
									} catch (IOException e1) {
										e1.printStackTrace();
									}
								}
							}
							if (oneSelected) {
								for (JTextArea text : cardText) {
									setViewable(text, false);
									for (MouseListener m : text.getMouseListeners()) {
										text.removeMouseListener(m);
									}
								}
							} else {
								guideText.setText("You need to select an item first!");
							}
						}
					}
				});

				seeCardAmounts.setFont(font);
				seeCardAmounts.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						if (gameInSession) {
							try {
								output.writeObject(new Message("Request Totals", name));
							} catch (IOException e1) {
								System.out.println("Coulnd't send the message: " + e1.getStackTrace());
							}
							while (true) {
								rest(50);
								if (!blackCardTotals.isEmpty()) {
									new Totals().start();
									break;
								}
							}
						}
					}
				});
				seeCardAmounts.setBackground(Color.black);
				seeCardAmounts.setForeground(Color.white);

				blackCardPanel.add(blackCardText);
				blackCardPanel.add(submitButton);
				blackCardPanel.add(seeCardAmounts);

				mainPanel.setPreferredSize(new Dimension(1000, 700));
				mainPanel.add(guideTextPanel, BorderLayout.PAGE_START);
				mainPanel.add(whiteCardPanel, BorderLayout.LINE_START);
				mainPanel.add(blackCardPanel, BorderLayout.PAGE_END);
				mainPanel.setBorder(
						BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(2, 2, 2, 2, Color.black),
								BorderFactory.createEmptyBorder(3, 3, 3, 3)));
				mainPanel.setVisible(true);

				mainFrame.add(mainPanel);
				mainFrame.setResizable(false);
				mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				mainFrame.pack();
				mainFrame.setVisible(true);
				mainFrame.setTitle(name);
			}
		}

		public void reinstateCards(ArrayList<Card> cards) {
			guideText.setText("Select a card that best matches the black one.");
			blackCardText.setText(cards.get(0).contents);
			cards.remove(0);
			for (int i = 0; i < cards.size(); i++) {
				cardText[i].setText(cards.get(i).contents);
			}
			for (JTextArea text : cardText) {
				setViewable(text, true);
				setHighlighted(text, false);
				text.addMouseListener(new MouseInputAdapter() {
					@Override
					public void mouseClicked(MouseEvent e) {
						for (JTextArea text : cardText) {
							if (text.getBackground() == Color.LIGHT_GRAY) {
								setHighlighted(text, false);
							}
						}
						if (text.getBackground() == Color.LIGHT_GRAY) {
							setHighlighted(text, false);
						} else {
							setHighlighted(text, true);
						}
					}
				});
			}
		}

		class Judge extends Thread {

			public void run() {
				while (true) {
					rest(1000);
					if (isCurrentlyDealer) {
						mainFrame.setVisible(false);
						isCurrentlyDealer = false;
						build();
						continue;
					}
				}
			}

			JFrame frame;
			JPanel mainPanel;
			JPanel bottomPanel;
			JPanel topPanel;
			JPanel middlePanel;
			JTextArea guideText;
			JTextArea whiteCardText;
			JTextArea blackCardText;
			JComboBox<String> dropdown;
			JButton confirmButton;
			Font cardFont = new Font("Comic Sans MS", Font.PLAIN, 24);
			Font headingFont = new Font("Comic Sans MS", Font.BOLD, 36);
			HashMap<String, Boolean> cardsToDisplay = new HashMap<String, Boolean>();
			HashMap<String, String> whatToDisplay = new HashMap<String, String>();
			boolean waiting = true;
			
			public void build() {
				frame = new JFrame("Choose the Winning White Card!");
				mainPanel = new JPanel(new BorderLayout(5, 5));
				bottomPanel = new JPanel(new FlowLayout());
				topPanel = new JPanel();
				dropdown = new JComboBox<String>();
				guideText = new JTextArea("Waiting for players to select their white card...");
				whiteCardText = new JTextArea(
						"The white card will be displayed here based on the dropdown on the left.");
				blackCardText = new JTextArea(judgeBlack);
				confirmButton = new JButton();
				middlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 20));

				guideText.setFont(headingFont);
				guideText.setVisible(true);
				guideText.setEditable(false);
				guideText.setBorder(
						BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(3, 3, 3, 3, Color.black),
								BorderFactory.createEmptyBorder(5, 5, 5, 5)));

				for (Map.Entry<String, Boolean> e : cardsToDisplay.entrySet()) {
					dropdown.addItem(e.getKey());
				}
				dropdown.setFont(cardFont);
				dropdown.setBorder(
						BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(3, 3, 3, 3, Color.black),
								BorderFactory.createEmptyBorder(5, 5, 5, 5)));
				dropdown.addActionListener(new ActionListener() {
					@SuppressWarnings("unchecked")
					public void actionPerformed(ActionEvent e) {
						JComboBox<String> combo = (JComboBox<String>) e.getSource();
						String selectedItem = (String) combo.getSelectedItem();
						for (Map.Entry<String, Boolean> item : cardsToDisplay.entrySet()) {
							if (item.getKey().equals(selectedItem)) {
								item.setValue(true);
							}
						}
						whiteCardText.setText(whatToDisplay.get(selectedItem));
					}
				});
				dropdown.setVisible(false);

				whiteCardText.setFont(cardFont);
				whiteCardText.setPreferredSize(new Dimension(400, 400));
				whiteCardText.setWrapStyleWord(true);
				whiteCardText.setLineWrap(true);
				whiteCardText.setEditable(false);
				whiteCardText.setBorder(
						BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(3, 3, 3, 3, Color.black),
								BorderFactory.createEmptyBorder(5, 5, 5, 5)));

				blackCardText.setEditable(false);
				blackCardText.setFont(cardFont);
				blackCardText.setPreferredSize(new Dimension(400, 400));
				blackCardText.setWrapStyleWord(true);
				blackCardText.setLineWrap(true);
				blackCardText.setBackground(Color.BLACK);
				blackCardText.setForeground(Color.WHITE);
				blackCardText.setBorder(
						BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(3, 3, 3, 3, Color.white),
								BorderFactory.createEmptyBorder(5, 5, 5, 5)));

				confirmButton.setPreferredSize(new Dimension(500, 100));
				confirmButton.setText("Confirm Selection");
				confirmButton.setFont(headingFont);
				confirmButton.setBackground(Color.LIGHT_GRAY);
				confirmButton.setBorder(
						BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(3, 3, 3, 3, Color.black),
								BorderFactory.createEmptyBorder(5, 5, 5, 5)));
				confirmButton.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						for (Map.Entry<String, Boolean> item : cardsToDisplay.entrySet()) {
							if (!item.getValue()) {
								guideText.setText("You need to view all of the cards first!");
								return;
							}
						}
						try {
							mainFrame.setVisible(true);
							frame.setVisible(false);
							output.writeObject(new Message("Selected winner", whiteCardText.getText()));
							cardsToDisplay.clear();
							whatToDisplay.clear();
							waiting = false;
						} catch (IOException e1) {
							e1.printStackTrace();
						}
						System.out.println(whiteCardText.getText());
						isCurrentlyDealer = false;
						frame.setVisible(false);

					}
				});
				confirmButton.setVisible(false);

				middlePanel.add(dropdown);
				middlePanel.add(whiteCardText);
				middlePanel.add(blackCardText);

				topPanel.add(guideText);
				topPanel.setVisible(true);

				bottomPanel.add(confirmButton);

				mainPanel.add(topPanel, BorderLayout.PAGE_START);
				mainPanel.add(middlePanel, BorderLayout.LINE_START);
				mainPanel.add(bottomPanel, BorderLayout.PAGE_END);
				mainPanel.setVisible(true);
				mainPanel.setPreferredSize(new Dimension(1000, 700));

				frame.add(mainPanel);
				frame.setResizable(false);
				frame.pack();
				frame.setVisible(true);
				waitForEveryonesCards();
			}

			public void waitForEveryonesCards() {
				while (true) {
					if (lookAtCardsOk) {
						dropdown.removeAllItems();
						cardsToDisplay.clear();
						confirmButton.setVisible(true);
						ArrayList<String> theCards = cardsToLookAt;
						int i = 1;
						for (String s : theCards) {
							dropdown.addItem("Card " + i);
							cardsToDisplay.put("Card " + i, false);
							whatToDisplay.put("Card " + i, s);
							i++;
						}
						dropdown.setVisible(true);
						cardsToLookAt.clear();
						guideText.setText("Choose your favorite card!");
						lookAtCardsOk = false;
					} else if (!waiting) {
						return;
					}
					rest(100);
				}
			}

		}

		class Totals extends Thread {
			public void run() {
				build();
			}

			public void build() {
				JFrame frame = new JFrame("Black Card Totals");
				JPanel panel = new JPanel();
				panel.setPreferredSize(new Dimension(500, 500));
				JTextArea text = new JTextArea();
				text.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10),
						BorderFactory.createMatteBorder(3, 3, 3, 3, Color.black)));
				text.setBackground(Color.black);
				panel.setBackground(Color.black);
				panel.add(text);
				text.setForeground(Color.white);
				String toSet = "";
				for (String num : blackCardTotals) {
					toSet += num + "\n";
				}
				blackCardTotals.clear();
				text.setText(toSet);
				text.setFont(cardFont);
				frame.add(panel);
				frame.pack();
				frame.setVisible(true);
				frame.setDefaultCloseOperation(HIDE_ON_CLOSE);
				frame.addWindowListener(new WindowListener() {

					@Override
					public void windowOpened(WindowEvent e) {
						// TODO Auto-generated method stub

					}

					@Override
					public void windowClosing(WindowEvent e) {
						// TODO Auto-generated method stub

					}

					@Override
					public void windowClosed(WindowEvent e) {
						// TODO Auto-generated method stub
						currentThread().interrupt();
						frame.setVisible(false);
					}

					@Override
					public void windowIconified(WindowEvent e) {
						// TODO Auto-generated method stub

					}

					@Override
					public void windowDeiconified(WindowEvent e) {
						// TODO Auto-generated method stub

					}

					@Override
					public void windowActivated(WindowEvent e) {
						// TODO Auto-generated method stub

					}

					@Override
					public void windowDeactivated(WindowEvent e) {
						// TODO Auto-generated method stub

					}

				});

			}
		}
	}

}
