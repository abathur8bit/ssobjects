package example.anagram;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import example.UserStateEnum;
import ssobjects.Stopwatch;
import ssobjects.telnet.TelnetServerSocket;

public class AnagramGame {
	private Map<TelnetServerSocket, AnagramUser> users = new HashMap<TelnetServerSocket, AnagramUser>();
	public String gameName = "";
	public long lastTick = 0;
	private AnagramServer server;
	public GameState state = GameState.WAITING_FOR_USERS;
	public Stopwatch timer;
	
	public enum GameState {
		WAITING_FOR_USERS,
		PLAYING,
		END,
	};

	public AnagramGame(String name,AnagramServer server) {
		this.gameName = name;
		this.server = server;
		timer.start();
	}

	public synchronized int userCount() {
		return users.size();
	}

	public synchronized int addUser(AnagramUser newUser) throws IOException {
		if (users.get(newUser.sock) != null) {
			newUser.sock.println("You already joined");
		} else {
			users.put(newUser.sock, newUser);
			newUser.currentGame = this;
			newUser.state = UserStateEnum.PLAYING;
		}
		return users.size();
	}

	public synchronized int removeUser(AnagramUser remove) {
		remove.currentGame = null;
		users.remove(remove.sock);
		return users.size();
	}

	public synchronized void println(String msg) {
		Iterator<AnagramUser> it = users.values().iterator();
		while (it.hasNext()) {
			AnagramUser u = it.next();
			try {
				u.sock.println(msg);
			} catch (IOException e) {
				u.state = UserStateEnum.ERROR;	//this user will need to be removed from the game
			}
		}
	}

	public void tick(AnagramServer server) {
		 cleanupDeadUsers();
		 gameLogic();
	}
	
	protected void cleanupDeadUsers() {
		Iterator<AnagramUser> it = users.values().iterator();
		while (it.hasNext()) {
			AnagramUser u = it.next();
			if(u.state == UserStateEnum.ERROR) {
				server.close(u);
			}
		}
	}
	
	protected void gameLogic() {
		if(state == GameState.WAITING_FOR_USERS) {
			if(userCount() > 1) {
				timer.start();
				state = GameState.PLAYING;
				println("Game starting");
			}
		}
		else if(state == GameState.PLAYING) {
			if(timer.getMilliSeconds() > 10000) {
				println("What a great game!");
				state = GameState.END;
			}
		}
	}
}
