package cardsAgainstHumanity;

import java.io.File;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.Vector;

public class Server implements Serializable, Constants {

	private static final long serialVersionUID = -4355868356116139539L;
	Vector<HandleClient> clients = new Vector<HandleClient>();
	Vector<String> users = new Vector<String>();
	static CardDeck questions = new CardDeck("questions");
	static CardDeck answers = new CardDeck("answers");
	ArrayList<Player> players = new ArrayList<Player>();
	Message messageReceived;
	String cardReceived = "";
	boolean canContinue = false;

	@SuppressWarnings("resource")
	public static void main(String[] args) throws Exception {
		String temp = Server.class.getResource("questions.txt").toString();
		temp = temp.substring(temp.indexOf("/") + 1);
		temp = temp.replaceAll("%20", " ");
		File questionsFile = new File(temp);
		Scanner fileReader = new Scanner(questionsFile);
		ArrayList<String> questionList = new ArrayList<String>();
		while (fileReader.hasNextLine()) {
			questionList.add(fileReader.nextLine());
		}
		questions.init(questionList);
		temp = Server.class.getResource("answers.txt").toString();
		temp = temp.substring(temp.indexOf("/") + 1);
		temp = temp.replaceAll("%20", " ");
		File answersFile = new File(temp);
		Scanner reader = new Scanner(answersFile);
		ArrayList<String> answerList = new ArrayList<String>();
		while (reader.hasNextLine()) {
			answerList.add(reader.nextLine());
		}
		answers.init(answerList);
		new Server().process();
	}

	public void process() throws Exception {
		@SuppressWarnings("resource")
		ServerSocket server = new ServerSocket(PORT, 10);
		System.out.println("Server started.");
		while (true) {
			Socket client = server.accept();
			HandleClient c;
			try {
				c = new HandleClient(client);
				clients.add(c);
			} catch (Exception e) {
				if (e.getMessage().equals("Duplicate User")) {
					continue;
				}
			}
		}
	}

	public void broadcast(Message m) {
		for (HandleClient c : clients) {
			c.sendMessage(m);
		}
	}

	class HandleClient extends Thread {
		String username;
		ObjectInputStream input;
		ObjectOutputStream output;
		Socket client;

		public HandleClient(Socket client) throws Exception {
			this.client = client;
			input = new ObjectInputStream(client.getInputStream());
			output = new ObjectOutputStream(this.client.getOutputStream());
			output.flush();

			Message init = (Message) input.readObject();
			username = init.message;
			if (users.size() > 0 && users.contains(username)) {
				sendMessage(new Message("Duplicate", username));
				throw new Exception("Duplicate User");
			} else {
				users.add(username);
				System.out.println(users);
				new Input().start();
			}
		}

		public void sendMessage(Message m) {
			try {
				output.writeObject(m);
				output.flush();
			} catch (Exception e) {
				System.out.println("Couldn't send message: " + e.getStackTrace());
			}
		}

		class Input extends Thread {
			public void run() {
				try {
					while (true) {
						messageReceived = (Message) input.readObject();
						if (messageReceived.subject.equals("Start")) {
							new Game().start();
						} else if (messageReceived.subject.equals("Request Totals")) {
							ArrayList<String> totals = new ArrayList<String>();
							totals.add(messageReceived.message);
							for (Player p : players) {
								totals.add(p.name + ": " + p.wins + " black cards.");
							}
							for (HandleClient h : clients) {
								if (h.username.equals(messageReceived.message)) {
									broadcast(new Message("Totals", totals));
								}
							}
						} else if (messageReceived.subject.equals("White card from player")) {
							cardReceived = messageReceived.message;
						}
					}
				} catch (Exception e) {
					System.out.println("Something went wrong.\n" + e.getStackTrace().toString());
				}
			}
		}

		class Game extends Thread {
			HashMap<String, String> responses = new HashMap<String, String>();
			Card question = questions.getNextCard();
			int dealer = 0;

			public void run() {
				for (String str : users) {
					if (!str.equals("admin")) {
						players.add(new Player(str));
					}
				}
				broadcast(new Message("Player Count", Integer.toString(players.size())));
				go();
			}

			public int getHighestWinCount() {
				int highest = players.get(0).wins;
				for (Player p : players) {
					if (p.wins > highest) {
						highest = p.wins;
					}
				}
				return highest;
			}

			public void go() {
				broadcast(new Message("Guide", "Welcome to Cards Against Humanity."));
				init();
				while (getHighestWinCount() < WIN_LIMIT) {
					newRound();
				}
				endGame();
			}

			public void init() {
				for (Player p : players) {
					for (int i = 0; i < 7; i++) {
						p.cards.add(answers.getNextCard());
					}
				}
			}

			public void sendCards() {
				for (Player p : players) {
					if (p.cards.size() != 7) {
						p.cards.add(answers.getNextCard());
					} else {
						p.cards.add(answers.getNextCard());
					}
					ArrayList<String> toSend = new ArrayList<String>();
					toSend.add(p.name);
					for (Card c : p.cards) {
						toSend.add(c.contents);
					}
					broadcast(new Message("White Cards", toSend));
					broadcast(new Message("Black Card", question.contents));
				}
			}

			public void newRound() {
				question = questions.getNextCard();
				sendCards();
				broadcast(new Message("You are judge", players.get(dealer).name + "$" + question.contents));
				judge();
			}

			public void judge() {
				waitForAllResponses();
				sendJudgeData();
			}

			public void sendJudgeData() {
				ArrayList<String> cards = new ArrayList<String>();
				for (Map.Entry<String, String> item : responses.entrySet()) {
					cards.add(item.getValue());
				}
				broadcast(new Message(players.get(dealer).name, cards));
				waitForJudge();
			}

			public void waitForJudge() {
				while (true) {
					try {
						Thread.sleep(50);
					} catch (InterruptedException e) {
					}
					if (messageReceived.subject.equals("Selected winner")) {
						System.out.println("winner selected: " + messageReceived.message);
						String winningCard = messageReceived.message;
						for (Player p : players) {
							for (Card s : p.cards) {
								if (s.contents.equals(winningCard)) {
									winner(p.name, winningCard);
									return;
								}
							}
						}
					}
				}
			}

			public void winner(String winner, String card) {
				broadcast(new Message("You win", winner));
				for (int i = 0; i < players.size(); i++) {
					for (Map.Entry<String, String> item: responses.entrySet()) {
						for (Card c: players.get(i).cards) {
							if (item.getValue().equals(c.contents)) {
								players.get(i).cards.remove(c);
								break;
							}
						}
					}
					if (players.get(i).name.equals(winner)) {
						players.get(i).wins++;
						dealer = i;
					}
				}
			}

			public void waitForAllResponses() {
				responses = new HashMap<String, String>();
				while (true) {
					try {
						Thread.sleep(1);
					} catch (InterruptedException e) {
					}
					if (cardReceived.length() > 0) {
						String card = cardReceived;
						cardReceived = "";
						responses.put(card.substring(0, card.indexOf("?")), card.substring(card.indexOf("?") + 1));
						System.out.println(responses.size() + "\t" + players.size());
					}
					if (responses.size() == players.size() - 1) {
						ArrayList<String> temp = new ArrayList<String>();
						for (Map.Entry<String, String> e : responses.entrySet()) {
							temp.add(e.getValue());
						}
						System.out.println("sending cards to judge");
						broadcast(new Message("Cards to judge", temp));
						break;
					}
				}
			}

			public void endGame() {
				for (Player p : players) {
					if (p.wins == WIN_LIMIT) {
						broadcast(new Message("Final winner", p.name));
						players.clear();
						run();
					}
				}
			}
		}
	}
}