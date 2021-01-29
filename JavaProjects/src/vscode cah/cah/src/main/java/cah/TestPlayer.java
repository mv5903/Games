package cah;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.event.*;

public class TestPlayer {
    public static void main(String[] args) {
        new TestPlayer();
    }

    public TestPlayer() {
        build();
    }

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
        guideText = new JTextArea("Select a card that best matches the black one.");
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
                guideText.setText("Card selected. Please wait for the next black card.");
                for (JTextArea text : cardText) {
                    if (text.getBackground() == Color.LIGHT_GRAY) {
                        System.out.println(text.getText());
                    }
                    setViewable(text, false);
                    for (MouseListener m : text.getMouseListeners()) {
                        text.removeMouseListener(m);
                    }
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
        try {
            Thread.sleep(5000);
        } catch (Exception e) {
        }
        reinstateCards();
    }

    public void reinstateCards() {
        guideText.setText("Select a card that best matches the black one.");
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

}
