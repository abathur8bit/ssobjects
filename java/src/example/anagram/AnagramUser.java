package example.anagram;

import example.UserStateEnum;
import ssobjects.Stopwatch;
import ssobjects.telnet.TelnetServerSocket;

public class AnagramUser
{
    public Stopwatch timer;
    public TelnetServerSocket sock;
    public UserStateEnum state;
    public String username;
    public long lastActivity;       //when last command from user came (keep track of how long user has been idle
    public boolean ticking = false;
    public AnagramGame currentGame; //the game user is playing, or null if not playing
    
    public AnagramUser(TelnetServerSocket s) {
        this(s,null);
    }
    
    public AnagramUser(TelnetServerSocket s,String u) {
        sock = s;
        username = u;
        state=UserStateEnum.LOGIN;
        timer = new Stopwatch();    //creates and starts stopwatch

        updateIdle();
    }

    public void updateIdle() {
        lastActivity = System.currentTimeMillis();
        timer.start();
    }

    public long idleTime() {
        return System.currentTimeMillis()-lastActivity;
    }
    
    public boolean isLoggedIn()
    {
        return state == UserStateEnum.ACTIVE;
    }
    
    public boolean isTicking()
    {
        return ticking;
    }
    
    public void setTicking(boolean b) {
        ticking = b;
    }
    
    public String userNameHost() {
        if(username == null)
            return sock.getHostAddress();
        return username;
    }
}
