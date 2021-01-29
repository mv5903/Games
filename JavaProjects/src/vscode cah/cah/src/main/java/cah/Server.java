package cah;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.Vector;

public class Server {
    final int port = 9999;
    final static int WIN_LIMIT = 10;
    Vector<HandleClient> clients = new Vector<HandleClient>();
    Vector<String> users = new Vector<String>();
    static CardDeck questions = new CardDeck("questions");
    static CardDeck answers = new CardDeck("answers");

    String response;

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
        fileReader.close();
        questions.init(questionList);
        temp = Server.class.getResource("answers.txt").toString();
        temp = temp.substring(temp.indexOf("/") + 1);
        temp = temp.replaceAll("%20", " ");
        File answersFile = new File(temp);
        Scanner reader = new Scanner(answersFile);
        ArrayList<String> answerList = new ArrayList<String>();
        while (fileReader.hasNextLine()) {
            answerList.add(fileReader.nextLine());
        }
        reader.close();
        answers.init(answerList);
        new Server().process();
    }

    public void process() throws Exception {
        @SuppressWarnings("resource")
        ServerSocket server = new ServerSocket(port, 10);
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

    public void broadcast(String message) {
        for (HandleClient c : clients) {
            c.sendMessage(message);
        }
    }

    public void sendPrivately(String user, String message) {
        for (HandleClient c : clients) {
            if (c.username.equals(user)) {
                c.sendMessage(message);
                break;
            }
        }
    }

    public void sendGameData(ArrayList<String> data) {
        String toSend = "";
        for (String s : data) {
            toSend += s + "^";
        }
        sendPrivately(data.get(0).substring(data.get(0).indexOf(" ")), toSend);
    }

    class HandleClient extends Thread {
        String username;
        BufferedReader input;
        PrintWriter output;

        public HandleClient(Socket client) throws Exception {
            input = new BufferedReader(new InputStreamReader(client.getInputStream(), "UTF-8"));
            output = new PrintWriter(client.getOutputStream(), true, Charset.forName("UTF-8"));
            username = input.readLine();
            if (users.size() > 0 && users.contains(username)) {
                sendMessage("!Duplicate");
                throw new Exception("Duplicate User");
            } else {
                users.add(username);
                start();
            }
        }

        public void sendMessage(String message) {
            this.output.println(message);
        }

        public void run() {
            String line;
            try {
                while (true) {
                    line = input.readLine();
                    response = line.substring(line.indexOf(":") + 1);
                    if (response.equals("leave")) {
                        broadcast(username + " has left.");
                    } else if ((username + ": " + line).equals("admin: start")) {
                        Game g = new Game();
                        g.setName("Game Thread");
                        g.start();
                    } else if ((username + ": " + line).equals("admin: stop")) {
                        broadcast("!stopgame");
                    }
                }
            } catch (Exception e) {
                System.out.println("Something went wrong.\n" + e.getStackTrace());
            }
        }

        class Game extends Thread {
            ArrayList<Player> players = new ArrayList<Player>();
            ArrayList<String> gameData = new ArrayList<String>();
            HashMap<String, String> responses = new HashMap<String, String>();
            Card question = questions.getNextCard();
            int dealer = 0;

            public void run() {
                for (String str : users) {
                    if (!str.equals("admin")) {
                        players.add(new Player(str));
                    }
                }
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
                broadcast("Welcome to Cards Against Humanity.");
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
                sendCards();
            }

            public void sendCards() {
                for (Player p: players) {
                    String data = "!data " + question;
                    if (p.cards.size() != 7) {
                        p.cards.add(answers.getNextCard());
                    } else {
                        for (Card c: p.cards) {
                            data += "^" + c.contents;
                        }
                    }
                }
            }

            public void newRound() {
                question = questions.getNextCard();
                sendCards();
                sendPrivately(players.get(dealer).name, "!dealer " + players.get(dealer).name);
                judge();
            }

            public void judge() {
                String dealerName = players.get(dealer).name;
                waitForAllResponses();
                sendJudgeData();
            }

            public void sendJudgeData() {
                String toSend = "!judge ";
                for (Map.Entry<String, String> item : responses.entrySet()) {
                    toSend += item.getValue() + "^";
                }
                sendPrivately(players.get(dealer).name, toSend);
                waitForJudge();
            }

            public void waitForJudge() {
                String winner = "!win ";
                while (true) {
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException e) {
                    }
                    if (response.length() > 0) {
                        String winningCard = response;
                        for (Map.Entry<String, String> item : responses.entrySet()) {
                            if (winningCard.equals(item.getValue())) {
                                winner(item.getKey(), item.getValue());
                                response = "";
                                break;
                            }
                        }
                    }
                }
            }

            public void winner(String winner, String card) {
                broadcast("!win " + winner);
                for (int i = 0; i < players.size(); i++) {
                    if (players.get(i).name.equals(winner)) {
                        players.get(i).wins++;
                        dealer = i;
                    }
                }
                // give a new card to everyone
                for (Player p : players) {
                    p.cards.add(answers.getNextCard());
                }
                sendCards();
            }

            public void waitForAllResponses() {
                responses = new HashMap<String, String>();
                while (true) {
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                    }
                    if (response.length() > 0) {
                        String name = response.substring(response.indexOf("-") + 1);
                        name = name.substring(0, name.indexOf("!"));
                        String cardChosen = name.substring(response.indexOf("~") + 1);
                        for (Player p : players) {
                            if (p.name.equals(name)) {
                                for (int i = 0; i < p.cards.size(); i++) {
                                    if (p.cards.get(i).contents.equals(cardChosen)) {
                                        p.cards.remove(i);
                                    }
                                }
                            }
                        }
                        responses.put(name, cardChosen);
                        response = "";
                    }
                    if (responses.size() == players.size()) {
                        break;
                    }
                }
            }

            public void endGame() {
                for (Player p : players) {
                    if (p.wins == WIN_LIMIT) {
                        broadcast(p.name + " has reached the black card limit. They win.");
                        players.clear();
                        run();
                    }
                }
            }
        }
    }
}