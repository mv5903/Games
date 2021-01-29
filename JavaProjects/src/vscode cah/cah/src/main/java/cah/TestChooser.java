package cah;

import java.awt.*;
import java.awt.event.*;

import java.util.*;

import javax.swing.*;

public class TestChooser {
    public static void main(String[] args) {
        new TestChooser();
    }

    public TestChooser() {
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
    ArrayList<String> cardsToDisplay = new ArrayList<String>();

    public void build() {
        frame = new JFrame("Choose the Winning White Card!");
        mainPanel = new JPanel(new BorderLayout(5, 5));
        bottomPanel = new JPanel(new FlowLayout());
        topPanel = new JPanel();
        dropdown = new JComboBox<String>();
        guideText = new JTextArea("Choose the winner by using the dropdown on the left.");
        whiteCardText = new JTextArea("The white cards will be displayed here.");
        blackCardText = new JTextArea("The black card will be displayed here.");
        confirmButton = new JButton();
        middlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 20));

        guideText.setFont(headingFont);
        guideText.setVisible(true);
        guideText.setEditable(false);
        guideText.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(3, 3, 3, 3, Color.black),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)));

        cardsToDisplay.add("Card 1");
        cardsToDisplay.add("Card 2");
        cardsToDisplay.add("Card 3");

        for (String s : cardsToDisplay) {
            dropdown.addItem(s);
        }
        dropdown.setFont(cardFont);
        dropdown.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(3, 3, 3, 3, Color.black),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        dropdown.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JComboBox<String> combo = (JComboBox<String>) e.getSource();
                String selectedItem = (String) combo.getSelectedItem();
                whiteCardText.setText(selectedItem);
            }
        });

        whiteCardText.setFont(cardFont);
        whiteCardText.setPreferredSize(new Dimension(400, 400));
        whiteCardText.setWrapStyleWord(true);
        whiteCardText.setLineWrap(true);
        whiteCardText.setEditable(false);
        whiteCardText.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(3, 3, 3, 3, Color.black), BorderFactory.createEmptyBorder(5, 5, 5, 5)));

        blackCardText.setEditable(false);
        blackCardText.setFont(cardFont);
        blackCardText.setPreferredSize(new Dimension(400, 400));
        blackCardText.setWrapStyleWord(true);
        blackCardText.setLineWrap(true);
        blackCardText.setBackground(Color.BLACK);
        blackCardText.setForeground(Color.WHITE);
        blackCardText.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(3, 3, 3, 3, Color.white), BorderFactory.createEmptyBorder(5, 5, 5, 5)));

        confirmButton.setPreferredSize(new Dimension(500, 100));
        confirmButton.setText("Confirm Selection");
        confirmButton.setFont(headingFont);
        confirmButton.setBackground(Color.LIGHT_GRAY);
        confirmButton.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(3, 3, 3, 3, Color.black), BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        confirmButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println(whiteCardText.getText());
                System.exit(0);
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
    }

}
