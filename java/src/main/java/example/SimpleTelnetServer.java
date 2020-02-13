
package example;

import java.io.IOException;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import ssobjects.Stopwatch;

import ssobjects.telnet.TelnetMessage;
import ssobjects.telnet.TelnetServer;
import ssobjects.telnet.TelnetServerSocket;

public class SimpleTelnetServer extends TelnetServer
{
    public final static char ESCAPE = 27;
    public final static int IDLE_TIMEOUT = 60000;      //milliseconds
    public final static int DELAY = 500;
    protected Map<TelnetServerSocket,User> userMap = new HashMap<TelnetServerSocket,User>();
    protected long lastIdle = 0;
    protected Stopwatch timer = new Stopwatch();
    
    public SimpleTelnetServer(InetAddress host,int port,long idle) throws Exception {
        super(host,port,idle);
        lastIdle=System.currentTimeMillis();
    }

    @Override
    public void connectionAccepted(TelnetServerSocket sock) {
        System.out.println("New connection from ["+sock.getHostAddress()+"]");
        User u=new User(sock);
        userMap.put(sock,u);
        try {
            sock.sendString("What name will you go by?"+CRLF); //sending crlf so junit test will work, as it uses readline (just simplifying things here)
        } catch(IOException e) {
            close(u);
        }
    }
    
    @Override
    public void connectionClosed(TelnetServerSocket sock,IOException e) {
        User u = userMap.get(sock);
        userMap.remove(sock);
        String name = u==null?"":"-"+u.username;
        System.out.println("Closed connection from ["+sock.getHostAddress()+"]"+name);
    }

    @Override
    public void processSingleMessage(TelnetMessage msg) {
        try {
            User u = userMap.get(msg.sock);
            if(null != u) {
                u.updateIdle();
                processUser(u,msg.text);
            }
        } catch(IOException e) {
            System.out.println("ERROR got exception when sending data");
        }
    }

    @Override
    public void idle(long deltaTime) {
        long now = System.currentTimeMillis();
        lastIdle = now;
         System.out.println("Server tick to show server is able to process while waiting on users.");
         
//        if(timer.getMilliSeconds() > DELAY)
//        {
//            try
//            {
//                printlnAll("tick");
//            }
//            catch(IOException e) 
//            {
//            }
//        }
//        Iterator<User> it = userMap.values().iterator();
//        while(it.hasNext()) {
//            User u = it.next();
//            if(u.idleTime() > IDLE_TIMEOUT) {
//                System.out.println("Closing connection for "+u.username);
//                try {
//                    u.sock.println("Idle time exceeded, closing connection");
//                } catch(IOException e) {
//                    //can safely igore exception since we are closing his connection anyway
//                }
//                u.sock.close();
//                it.remove();
//            }
//        }
    }

    public void close(User u) {
        userMap.remove(u.sock);
        u.sock.close();        
    }
    
    protected void processUser(User u,String s) throws IOException {
        if(u.state == UserStateEnum.LOGIN) {
            u.username = s;
            u.state = UserStateEnum.ACTIVE;
            printlnAll("Welcome "+u.username);
            u.sock.println("Type a message and hit Enter. It will be broadcast to all other users.");
        }
        else if(u.state == UserStateEnum.ACTIVE) {
            if(s.equalsIgnoreCase("cls"))
            {
                cls(u);
            }
            else if(s.equalsIgnoreCase("help"))
            {
                u.sock.print("Type a message and hit return, and it will be broadcast to other users.\n");
            }
            else
            {
                printlnAll(u.username+":"+s);
            }
        }
    }
    
    public void cls(User u) throws IOException
    {
        u.sock.print(ESCAPE+"[2J");
    }
    
    public static void main(String[] args) throws Exception 
    {
        int hostPort = 4002;
        SimpleTelnetServer server = new SimpleTelnetServer(null,hostPort,5000);
        System.out.println("Running on port "+hostPort+".");
        System.out.println("This is a simple chat server. When someone connects via telnet, any text");
        System.out.println("they type will be sent to other users that are connected.");
        System.out.println("To try, bring up another console and type telnet localhost "+hostPort);
        System.out.println("");
        server.run();   //running as single thread
    }
}
