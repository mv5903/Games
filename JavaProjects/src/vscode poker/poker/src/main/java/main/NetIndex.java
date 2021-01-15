package main;

import java.util.Scanner;

//import org.json.*;

public class NetIndex {
	static Scanner kbd = new Scanner(System.in);


	public static void main(String[] args) {
		System.out.println("Welcome to Poker - Internet Version. Are you hosting?");
		if (kbd.next().equalsIgnoreCase("yes")) {
			System.out.println("Ok. Please follow the instructions on GitHub to learn how to create a server.");
			try {
				Server.main((String[])null);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			Client.main(null);
		}
	}
}
