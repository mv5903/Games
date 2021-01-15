package main;import java.awt.BorderLayout;
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

import java.nio.file.FileSystems;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.text.DefaultCaret;

public class Test extends JFrame implements Constants {
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	String uname;
	JTextArea taMessages, cardView, centerView, potView, balanceView, currentPlayer;
	JTextField tfInput;
	JButton btnSend, btnExit;
	Font font = new Font("Segoe UI Symbol", Font.PLAIN, 20);
	boolean isAdmin;
	
	public static void main(String[] args) {
		new Test();
	}
	
	public Test() {
		build();
	}
	
	public void build() {
		btnSend = new JButton("Send");
		btnSend.setBackground(Color.black);
		btnSend.setForeground(Color.yellow);
		btnSend.setPreferredSize(new Dimension(80, 46));
		btnSend.setBorder(BorderFactory.createMatteBorder(3, 3, 3, 3, Color.white));
		btnSend.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				//pw.println(tfInput.getText());
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
//		if (!uname.equals("admin")) {
//			tfInput.setEditable(false);
//			tfInput.setBackground(Color.LIGHT_GRAY);
//			tfInput.setText("User input disabled.");
//		}
		tfInput.setPreferredSize(new Dimension(300, 46));
		tfInput.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(3, 3, 3, 3, Color.white), BorderFactory.createEmptyBorder(5, 5, 5, 5)));
		tfInput.requestFocusInWindow();
		tfInput.addKeyListener(new KeyListener() {
			@Override
			public void keyTyped(KeyEvent e) {


			}

			@Override
			public void keyPressed(KeyEvent e) {
				int key = e.getKeyCode();
				if (key == KeyEvent.VK_ENTER) {
					//pw.println(tfInput.getText());
					tfInput.setText("");
				}

			}

			@Override
			public void keyReleased(KeyEvent e) {
	

			}
		});
		
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

	

	
	
	
	
	
	
	
	
	
	
	
}
