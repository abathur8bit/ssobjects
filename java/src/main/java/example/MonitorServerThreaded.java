package example;

import ssobjects.Stopwatch;
import ssobjects.telnet.TelnetServerSocket;
import ssobjects.telnet.TelnetServerThreaded;
import ssobjects.telnet.threads.TelnetServerHandler;

import java.io.IOException;
import java.util.*;

/**
 * Example server that provides the ability to login, query who else is connected
 * to the server, perform a couple operations, and shut down the server.
 *
 * Three important things to note:
 *
 * 1) The server is able to send continuous updates to one or more connections.
 * 2) The server is able to handle long running operations, by utilizing handlers on a different thread.
 * 3) The server is able to cleanly shutdown.
 *
 * @author lbpatterson
 */
public class MonitorServerThreaded extends TelnetServerThreaded {
    protected Map<TelnetServerSocket,MonitorUser> userMap = new HashMap<TelnetServerSocket,MonitorUser>();
    protected Stopwatch timer = new Stopwatch();

    public static void main(String[] args) throws Exception {
        MonitorServerThreaded server = new MonitorServerThreaded(4002);
        server.info("Monitor Server running on port 4002");
        server.runServer();	//TODO I should be calling a startServer() method instead, as run immplies a java Thread object
    }

    public MonitorServerThreaded(int port) throws IOException {
        super(null,port);
    }

    @Override
    protected TelnetServerHandler createHandler(int id) {
        return new MonitorHandlerThreaded(this,"thread handler "+id);
    }

    @Override
    public void connectionAccepted(TelnetServerSocket sock) {
        try
        {
            MonitorUser u = new MonitorUser(sock);
            addUser(sock,u);

            info("MonitorServerThreaded New connection from [" + sock.getHostAddress() + "]");
            sock.println("Login:");
        }
        catch(Exception e)
        {
            e.printStackTrace();
            close(sock);
        }
    }

    @Override
    public void connectionClosed(TelnetServerSocket sock,IOException e) {
        info("Connection from [" + sock.getHostAddress() + "] closed");
        sock.close();
        removeUser(sock);
    }

    @Override
    public void idle(long deltaTime) {
//        info("MonitorServerThreaded Idle ["+deltaTime+"]");
        long now = System.currentTimeMillis();
        printlnAllTickingUsers("tick [" + now + "]");
    }

    public synchronized void printlnAllTickingUsers(String s)
    {
        Iterator<MonitorUser> it = userMap.values().iterator();
        while(it.hasNext())
        {
            MonitorUser u = it.next();
            if(u.isLoggedIn() && u.isTicking())
            {
                try
                {
                    u.sock.println(s);
                }
                catch(IOException e)
                {
                    close(u);
                }
            }
        }
    }

    public void close(MonitorUser u)
    {
        userMap.remove(u.sock);
        u.sock.close();
    }

    protected synchronized void addUser(TelnetServerSocket sock,MonitorUser u) {
        userMap.put(sock,u);
    }

    protected synchronized void removeUser(TelnetServerSocket sock) {
        userMap.remove(sock);
    }

    protected synchronized MonitorUser getUser(TelnetServerSocket sock) {
        return userMap.get(sock);
    }

    /**
     * Grabs a copy of the user list. Since it is not save to iterate through a list
     * in multiple threads, grab a copy and work with that list.
     *
     * @return user list, or empty list if no users.
     */
    protected synchronized List<MonitorUser> getUserList() {
        List<MonitorUser> users = new ArrayList<MonitorUser>();
        Iterator<MonitorUser> it = userMap.values().iterator();
        while(it.hasNext()) {
            users.add(it.next());
        }
        return users;
    }
}
