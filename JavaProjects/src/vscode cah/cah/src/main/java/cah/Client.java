package cah;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import java.awt.*;
import java.awt.event.*;

import java.util.*;

import javax.swing.*;
import javax.swing.event.*;

@SuppressWarnings("serial")
public class Client extends JFrame {

    private static boolean isDebugSession = true;
    private static final String ADMIN_PASSWORD = "";
    private static final int PORT = 9999;
    String toSend = "";
    String name;
    PrintWriter output;
    BufferedReader input;
    Socket client;
    boolean isAdmin;
    int playerCount = 0;

    public static void main(String[] args) {
        if (isDebugSession)
            new Client();
        else {
            String servername = JOptionPane.showInputDialog(null, "Enter the IP address of whom you are connecting to:",
                    "Enter IP Address", JOptionPane.PLAIN_MESSAGE);
            String name = JOptionPane.showInputDialog(null, "Enter your name:", "Name Entry",
                    JOptionPane.PLAIN_MESSAGE);
            if (name.equalsIgnoreCase("admin")) {
                String password = JOptionPane.showInputDialog(null, "Enter the admin password: ", "Password",
                        JOptionPane.WARNING_MESSAGE);
                if (!password.equals(ADMIN_PASSWORD)) {
                    JOptionPane.showMessageDialog(null,
                            "Sorry, you entered the wrong admin password. The program will now terminate.",
                            "Wrong Password", JOptionPane.WARNING_MESSAGE);
                }
            }
            try {
                new Client(name, servername, true);
            } catch (Exception e) {
                System.err.println("Something went wrong when creating the user: " + e.getMessage());
            }
        }
    }

    public Client(String name, String servername, boolean isAdmin) throws Exception {
        this.isAdmin = isAdmin;
        this.name = name;
        client = new Socket(servername, PORT);
        input = new BufferedReader(new InputStreamReader(client.getInputStream(), "UTF-8"));
        output = new PrintWriter(client.getOutputStream(), true);
        output.println(name);
        build();
        new MessageListener().start();
    }

    public Client() {
        build();
    }

    class MessageListener extends Thread {
        public void run() {
            String messageReceived;
            try {
                while (true) {
                    messageReceived = input.readLine();
                    if (messageReceived.contains("!count ")) {
                        playerCount = Integer.parseInt(messageReceived.substring(messageReceived.indexOf(" ") + 1));
                        continue;
                    }
                    if (messageReceived.contains("!judge ")) {
                        String whiteCard = messageReceived.substring(messageReceived.indexOf(" ") + 1);
                        toSend = whiteCard;
                        continue;
                    }
                    if (messageReceived.contains("!dealer " + name)) {
                        System.out.println("Received dealer: " + messageReceived);
                        new Judge().start();
                        continue;
                    }
                    if (messageReceived.contains("!data ")) {
                        ArrayList<Card> cardsToDisplay = sortCardsIntoList(messageReceived);
                        for (int i = 0; i < cardsToDisplay.size(); i++) {
                            cardText[i].setText(cardsToDisplay.get(i).contents);
                        }
                        continue;
                    }
                    if (messageReceived.contains("!win")) {
                        guideText.setText("The winner was " + messageReceived.substring(messageReceived.indexOf(" ") + 1));
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public ArrayList<Card> sortCardsIntoList(String data) {
        ArrayList<Card> cards = new ArrayList<Card>();
        data = data.substring(data.indexOf(" ") + 1);
        String cardToAdd = "";
        boolean isBlack = true;
        int counter = 0;
        while (counter < data.length()) {
            if (data.charAt(counter) == '^') {
                cards.add(new Card(cardToAdd, isBlack));
                cardToAdd = "";
                isBlack = false;
            } else {
                cardToAdd += data.charAt(counter);
            }
            counter++;
        }
        return cards;
    }

    public void reinstateCards(ArrayList<Card> cards) {
        guideText.setText("Select a card that best matches the black one.");
        blackCardText.setText(cards.get(0).contents);
        cards.remove(0);
        for (int i = 0; i < cards.size(); i++) {
            cardText[i].setText(cards.get(i).contents);
        }
        for (JTextArea text : cardText) {
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

    // Main Frame
    JFrame frame;
    JPanel mainPanel;
    JTextArea guideText;
    JPanel blackCardPanel;
    JTextArea blackCardText;
    JPanel guideTextPanel;
    JPanel whiteCardPanel;
    JPanel[] cardPanels = new JPanel[7];
    JTextArea[] cardText = new JTextArea[7];
    JButton submitButton = new JButton("Submit");
    Font font = new Font("Comic Sans MS", Font.PLAIN, 36);
    Font cardFont = new Font("Comic Sans MS", Font.PLAIN, 20);
    Font boldCardFont = new Font("Comic Sans MS", Font.BOLD, 20);
    Font italicCardFont = new Font("Comic Sans MS", Font.ITALIC, 20);
    int chosenCard = 0;
    boolean isCardClicked = false;

    public void build() {
        frame = new JFrame();
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
                @Override
                public void mouseClicked(MouseEvent e) {
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
                boolean oneSelected = false;
                guideText.setText("Card selected. Please wait for the next black card.");
                for (JTextArea text : cardText) {
                    if (text.getBackground() == Color.LIGHT_GRAY) {
                        oneSelected = true;
                        output.println("!chosen " + text.getText());
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
        });

        blackCardPanel.add(blackCardText);
        blackCardPanel.add(submitButton);

        mainPanel.setPreferredSize(new Dimension(1000, 700));
        mainPanel.add(guideTextPanel, BorderLayout.PAGE_START);
        mainPanel.add(whiteCardPanel, BorderLayout.LINE_START);
        mainPanel.add(blackCardPanel, BorderLayout.PAGE_END);
        mainPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(2, 2, 2, 2, Color.black),
                BorderFactory.createEmptyBorder(3, 3, 3, 3)));
        mainPanel.setVisible(true);

        frame.add(mainPanel);
        frame.setResizable(false);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
        frame.setTitle(name);
        if (isDebugSession) {
            try {
                Thread.sleep(2000);
            } catch (Exception e) {
            }
            reinstateCards(null);
            try {
                Thread.sleep(1000);
            } catch (Exception e) {
            }
            new Judge().start();
        }
    }

    public void setHighlighted(JTextArea text, boolean shouldHighlight) {
        if (shouldHighlight) {
            text.setBackground(Color.LIGHT_GRAY);
            text.setFont(boldCardFont);
            text.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(5, 5, 5, 5, Color.black),
                    BorderFactory.createEmptyBorder(3, 3, 3, 3)));
        } else {
            text.setBackground(Color.white);
            text.setFont(cardFont);
            text.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(2, 2, 2, 2, Color.black),
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

    class Judge extends Thread {

        public void run() {
            build();
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

        public void build() {
            frame = new JFrame("Choose the Winning White Card!");
            mainPanel = new JPanel(new BorderLayout(5, 5));
            bottomPanel = new JPanel(new FlowLayout());
            topPanel = new JPanel();
            dropdown = new JComboBox<String>();
            guideText = new JTextArea("Choose the winner by using the dropdown on the left.");
            whiteCardText = new JTextArea("Select your favorite white card from the dropdown menu to the left.");
            blackCardText = new JTextArea("The black card will be displayed here.");
            confirmButton = new JButton();
            middlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 20));

            guideText.setFont(headingFont);
            guideText.setVisible(true);
            guideText.setEditable(false);
            guideText.setBorder(
                    BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(3, 3, 3, 3, Color.black),
                            BorderFactory.createEmptyBorder(5, 5, 5, 5)));

            cardsToDisplay.put("Card 1", false);
            cardsToDisplay.put("Card 2", false);
            cardsToDisplay.put("Card 3", false);

            for (Map.Entry<String, Boolean> e : cardsToDisplay.entrySet()) {
                dropdown.addItem(e.getKey());
            }
            dropdown.setFont(cardFont);
            dropdown.setBorder(
                    BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(3, 3, 3, 3, Color.black),
                            BorderFactory.createEmptyBorder(5, 5, 5, 5)));
            dropdown.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    JComboBox<String> combo = (JComboBox<String>) e.getSource();
                    String selectedItem = (String) combo.getSelectedItem();
                    for (Map.Entry<String, Boolean> item : cardsToDisplay.entrySet()) {
                        if (item.getKey().equals(selectedItem)) {
                            item.setValue(true);
                        }
                    }
                    whiteCardText.setText(selectedItem);
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
                    output.println("!win " + whiteCardText.getText());
                    System.out.println(whiteCardText.getText());
                    frame.setVisible(false);
                }
            });

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
            for (int i = 0; i < playerCount; i++) {
                while (true) {
                    if (toSend.equals("")) {
                        try {
                            Thread.sleep(10);
                        } catch (Exception e) {
                        }
                    } else {
                        dropdown.addItem(toSend);
                        toSend = "";
                        break;
                    }
                }
            }
            dropdown.setVisible(true);
        }
    }
}
