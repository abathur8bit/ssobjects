package example.anagram;

import java.io.IOException;
import java.util.List;

import example.UserStateEnum;
import ssobjects.telnet.TelnetMessage;
import ssobjects.telnet.TelnetServerSocket;
import ssobjects.telnet.threads.TelnetServerHandler;

/**
 * Created by lbpatterson on 11/21/2014.
 */
public class AnagramHandler extends TelnetServerHandler {
	AnagramServer server;

	public AnagramHandler(AnagramServer server, String name) {
		super(server, name);
		this.server = server;
	}

	@Override
	public void processMessage(TelnetMessage msg) throws IOException {
		AnagramUser u = server.getUser(msg.sock);
		if (null != u) {
			u.updateIdle();
			processUser(u, msg.text);
		} else {
			server.warn("Ignoring telnet message from " + msg.sock.getHostAddress());
			server.close(msg.sock);
		}
	}

	@Override
	public void handleError(Exception e, TelnetServerSocket sock) {
		AnagramUser u = server.getUser(sock);
		if (u != null) {
			cleanupUser(u);
		}
	}

	public void cleanupUser(AnagramUser u) {
		server.close(u); // clean up the user
	}

	protected void processUser(AnagramUser u, String s) throws IOException {
		if (u.state == UserStateEnum.LOGIN) {
			u.username = s;
			u.state = UserStateEnum.ACTIVE;
			server.printlnAll("Welcome " + u.username);
		} else if (u.state.ordinal() >= UserStateEnum.ACTIVE.ordinal()) {
			if (s.equalsIgnoreCase("help")) {
				u.sock.println("Welcome to Anagrams.");
				u.sock.println("");
				u.sock.println("Commands: cls, who, tick, busy, logout, shutdown, new <gameName>, join <gameName>, games, leave");
				u.sock.println("Chat by starting a line with a quote '");
			} else if(s.startsWith("'")) {
				if(u.currentGame != null) {
					String msg = s.substring(1);
					u.currentGame.println(u.username+":"+msg);
				}
				else {
					u.sock.println("Silence, infadel! Join a game to chat.");
				}
			} else if (s.startsWith("new ")) {
				newGame(u, s.substring("new ".length()));
			} else if (s.startsWith("join ")) {
				joinGame(u, s.substring("join ".length()));
			} else if (s.startsWith("leave")) {
				leaveGame(u);
			} else if (s.equalsIgnoreCase("games")) {
				listGames(u);
			} else if (s.equalsIgnoreCase("quit") || s.equalsIgnoreCase("exit")
					|| s.equalsIgnoreCase("logout")
					|| s.equalsIgnoreCase("logoff")) {
				u.sock.println("Good bye");
				server.close(u);
			} else if (s.equalsIgnoreCase("cls")) {
				u.sock.cls();
			} else if (s.equalsIgnoreCase("who")) {
				who(u.sock);
			} else if (s.equalsIgnoreCase("busy")) {
				u.sock.println("Very busy...");
				u.state = UserStateEnum.BUSY;
				busy(u);
				u.state = UserStateEnum.ACTIVE;
				u.sock.println("Done busy thing");
			} else if (s.equalsIgnoreCase("tick")) {
				u.setTicking(!u.isTicking());
				u.sock.println("Ticking is "
						+ (u.isTicking() ? "set" : "stopped"));
			} else if (s.equalsIgnoreCase("shutdown")) {
				shutdown(u);
			} else if (s.startsWith("'")) {
				server.printlnAll(u.username + ":" + s.substring(1));
			} else {
				u.sock.println("Invalid command. Try 'help'");
			}
		} else {
			u.sock.println("Invalid command");
		}

		u.sock.print("> ");
	}

	protected void newGame(AnagramUser u, String gameName) throws IOException {
		String name = gameName.trim();
		AnagramGame game = server.createGame(name);
		if (game == null) {
			u.sock.println("Sorry, that game already exists");
		} else {
			game.addUser(u);
			u.sock.println("Game " + name + " has been created");
		}
	}

	protected void leaveGame(AnagramUser u) throws IOException {
		if (u.currentGame == null) {
			u.sock.println("You are not in a game");
		} else {
			AnagramGame game = u.currentGame;
			game.removeUser(u);
			u.sock.println("You have been removed from " + game.gameName);
			if (game.userCount() == 0) {
				server.removeGame(game.gameName);
				u.sock.println("The game has been deleted");
			} else {
				game.println("User " + u.username + " left.");
			}
		}
	}

	protected void joinGame(AnagramUser u, String gameName) throws IOException {
		String name = gameName.trim();
		AnagramGame game = server.findGame(name);
		if (game == null) {
			u.sock.println("That game does not exist");
		} else {
			game.addUser(u);
		}

	}

	protected void listGames(AnagramUser u) throws IOException {
		List<AnagramGame> games = server.getGames();
		if (games.size() > 0) {
			u.sock.println("Current Games:");
			for (AnagramGame game : games) {
				u.sock.println(game.gameName);
			}
		} else {
			u.sock.println("No games");
		}
	}

	protected void shutdown(AnagramUser u) throws IOException {
		boolean canShutdown = true;
		for (AnagramUser user : server.getUserList()) {
			if (user.state == UserStateEnum.BUSY) {
				canShutdown = false;
				break;
			}
		}
		if (canShutdown) {
			server.printlnAll("Server shutting down now");
			server.stopServer();
		} else {
			u.sock.println("Sorry, busy users, cant' shutdown");
		}
	}

	protected void who(TelnetServerSocket sock) throws IOException {
		String format = "%-20s %-20s %-20s";
		sock.println(String.format(format, "USER", "STATUS", "IDLE"));
		sock.println(String.format(format, "----", "------", "----"));
		List<AnagramUser> users = server.getUserList();
		for (AnagramUser u : users) {
			String state = u.state.name();
			if (u.state == UserStateEnum.PLAYING) {
				state = state + " " + u.currentGame.gameName;
			}
			sock.println(String.format(format, u.userNameHost(), state,
					u.timer.toString()));
		}
	}

	protected void busy(AnagramUser u) {
		for (long i = 0; i < 1000000000; i++) {
			double j = i * 123.2313;
			j = j / 2;
		}
		try {
			sleep(10000); // sleep for some time (10 seconds)
		} catch (InterruptedException e) {
		}
	}
}
