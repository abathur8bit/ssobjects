package example;

import ssobjects.Stopwatch;

import ssobjects.*;
import ssobjects.telnet.TelnetServerSocket;

public class User {
    public TelnetServerSocket sock;
    public UserStateEnum state;
    public String username;
    public String password;
    public String passwordVerify;
    public long lastActivity;       //when last command from user came (keep track of how long user has been idle
    public Stopwatch timer;
    public int userFlags;

    public User(TelnetServerSocket s) {
        this(s,null);
    }
    public User(TelnetServerSocket s,String u) {
        sock = s;
        username = u;
        userFlags = 0;
        state=UserStateEnum.LOGIN;
        timer = new Stopwatch();
        timer.start();
        updateIdle();
    }

    public void updateIdle() {
    	timer.reset();
    	timer.start();
    }

    public long idleTime() {
        return timer.getMilliSeconds();
    }
    
    /** Returns the username if available and socket address. */
    public String getInfo()
    {
    	if(username != null)
    		return "["+username+"] ["+sock.getHostAddress()+"]";
    	return "["+sock.getHostAddress()+"]";
    }
}
