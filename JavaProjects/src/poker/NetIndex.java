package poker;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class NetIndex {
	static String IPAddress;
	final static int port = 8080;
	static Scanner kbd = new Scanner(System.in);
	static PrintStream charStream;

	public static void main(String[] args) {
		System.out.println("Welcome to Poker - Internet Version. Are you hosting?");
		if (kbd.next().equalsIgnoreCase("yes")) {
			System.out.println("Ok. Please follow the instructions on GitHub to learn how to create a server.");
			server();
		} else {
			System.out.println("Please enter the IP Address of whom you would like to connect to.");
			IPAddress = kbd.next();
			getPersonalInformation();
		}
	}
	
	public static void server() {
		String[] args = {"69"};
		Server.main(args);
	}
	
	public static void menu() {
		
	}

	public static void getPersonalInformation() {
		System.out.println("Please enter your name: ");
		String name = kbd.next();
		DELETE(name);
		
	}
	
	public static void DELETE(String name) {
		try {
			URL url = new URL(IPAddress + ":5000/user/" + name);
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setDoOutput(true);
			con.setRequestProperty(
			    "Content-Type", "application/x-www-form-urlencoded" );
			con.setRequestMethod("DELETE");
			con.connect();
			int status = con.getResponseCode();
			System.out.println("Delete request succeeded with status code " + status);
			return;
		} catch (MalformedURLException e) {
			System.out.println("IP Address Invalid.");
		} catch (ProtocolException e) {
			System.out.println("Can't find a REST API here.");
		} catch (IOException e) {
			System.out.println("Something went wrong. Check the API and try again.");
		}
		System.out.println("Delete request could not succeed.");
		return;
	}
	
	public static void PUT(String name, String paramToUpdate, String paramValue) {
		try {
			URL url = new URL(IPAddress + ":5000/user/" + name);
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setRequestMethod("PUT");
			Map<String, String> params = new HashMap<String, String>();
			params.put(paramToUpdate, paramValue);
			con.setDoOutput(true);
			DataOutputStream out = new DataOutputStream(con.getOutputStream());
			out.writeBytes(ParameterStringBuilder.getParamsString(params));
			out.flush();
			out.close();
			int status = con.getResponseCode();
			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			String inputLine;
			StringBuffer content = new StringBuffer();
			while ((inputLine = in.readLine()) != null) {
				content.append(inputLine);
			}
			in.close();
			con.disconnect();
			System.out.println("Succeeded with status code " + status);
			return;
		} catch (MalformedURLException e) {
			System.out.println("IP Address Invalid.");
		} catch (ProtocolException e) {
			System.out.println("Can't find a REST API here.");
		} catch (IOException e) {
			System.out.println("Something went wrong. Check the API and try again.");
		}
		System.out.println("Put request could not succeed.");
		return;
	}

	public static void POST(String name) {
		try {
			URL url = new URL(IPAddress + ":5000/user/" + name + "?name=" + name);
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setRequestMethod("POST");
			Map<String, String> params = new HashMap<String, String>();
			params.put("name", name);
			con.setDoOutput(true);
			DataOutputStream out = new DataOutputStream(con.getOutputStream());
			out.writeBytes(ParameterStringBuilder.getParamsString(params));
			out.flush();
			out.close();
			int status = con.getResponseCode();
			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			String inputLine;
			StringBuffer content = new StringBuffer();
			while ((inputLine = in.readLine()) != null) {
				content.append(inputLine);
			}
			in.close();
			con.disconnect();
			System.out.println("Succeeded with status code " + status);
			return;
		} catch (MalformedURLException e) {
			System.out.println("IP Address Invalid.");
		} catch (ProtocolException e) {
			System.out.println("Can't find a REST API here.");
		} catch (IOException e) {
			System.out.println("Something went wrong. Check the API and try again.");
		}
		System.out.println("Post request could not succeed.");
		return;
	}

	public static String GET(String name, String key) {
		try {
			URL url = new URL(IPAddress + ":5000/user/" + name);
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setRequestMethod("GET");
			con.setRequestProperty("Content-Type", "application/json");
			int status = con.getResponseCode();
			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			String inputLine = "";
			StringBuffer content = new StringBuffer();
			while ((inputLine = in.readLine()) != null) {
				content.append(inputLine);
			}
			String data = content.toString();
			in.close();
			con.disconnect();
			if (key.equalsIgnoreCase("all")) {
				return data;
			}
			System.out.println("Operation succeeded with status code " + status);
			// Find specific data
			if (!data.contains(key)) {
				return "Key not found.";
			}
			if (key.equals("hand")) {
				return data.substring(data.indexOf("["), data.indexOf("]") + 1);
			} else if (key.equals("handCombinedWithCenter")) {
				String newData = data.substring(data.indexOf("]") + 1);
				return newData.substring(newData.indexOf("["), newData.indexOf("]") + 1);
			} else {
				int startValue = data.indexOf(key) + key.length() + 3;
				String beginning = data.substring(0, startValue);
				String remainder = data.substring(startValue);
				int endValue = remainder.indexOf(",") + beginning.length();
				if (key.equals("finishedRound")) {
					return data.substring(startValue, remainder.indexOf("}") + beginning.length());
				} else {
					return data.substring(startValue, endValue);
				}
			}

		} catch (MalformedURLException e) {
			System.out.println("IP Address Invalid.");
		} catch (ProtocolException e) {
			System.out.println("Can't find a REST API here.");
		} catch (IOException e) {
			System.out.println("Something went wrong. Check the API and try again.");
		}
		return "Get request couldn't succeed.";
	}

	public static String[] getArrayOfString(String array) {
		String spacesRemoved = removeUneccesarySpaces(array);
		// remove [ and ]
		String noBrackets = "";
		for (char c : spacesRemoved.toCharArray()) {
			if (c != '[' && c != ']') {
				noBrackets += c;
			}
		}
		return noBrackets.split(",");
	}

	public static String removeUneccesarySpaces(String str) {
		String spacesRemoved = "";
		for (char c : str.toCharArray()) {
			if (c != ' ') {
				spacesRemoved += c;
			}
		}
		return spacesRemoved;
	}
}
