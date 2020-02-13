package example;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import ssobjects.telnet.TelnetMessage;
import ssobjects.telnet.TelnetServer;
import ssobjects.telnet.TelnetServerSocket;

import ssobjects.Stopwatch;


public class MonitorServer extends TelnetServer
{
	public final static int HOST_PORT = 4002;
	public final static int IDLE_DELAY = 500;	//milliseconds
	public final static int MAX_USER_IDLE = 5000;	//millis
    protected Map<TelnetServerSocket,MonitorUser> userMap = new HashMap<TelnetServerSocket,MonitorUser>();
    protected Stopwatch timer = new Stopwatch();

	public MonitorServer(int port,long idle) throws Exception
	{
		super(null,port,idle);
	}

	@Override
	public void connectionAccepted(TelnetServerSocket sock)
	{
		try
		{
			MonitorUser u = new MonitorUser(sock);
			userMap.put(sock,u);

			System.out.println("Connection from ["+sock.getHostAddress()+"]");
			sock.println("Login:");
		} 
		catch(Exception e)
		{
			e.printStackTrace();
			close(sock);
		}
	}


	@Override
    /** Server saying that a connection was closed. */
	public void connectionClosed(TelnetServerSocket sock,IOException e) 
	{
		System.out.println("Connection from ["+sock.getHostAddress()+"] closed");
		sock.close();
		userMap.remove(sock);
	}

	@Override
    /**
     * A connected socket sent some text. Grab the socket and text from the TelnetMessage.
     * The text is gauranteed to be a full line of text. CRLF has been stripped from
     * the text.
     *
     *@param msg The socket and text that was sent.
     */
	public void processSingleMessage(TelnetMessage msg) {
		dumpMessage(msg);
		try {
			MonitorUser u = userMap.get(msg.sock);
            if(null != u) {
                u.updateIdle();
                processUser(u,msg.text);
            }
            else
            {
            	System.out.println("Ignoring telnet message from "+msg.sock.getHostAddress());
            	close(msg.sock);
            }
        } catch(IOException e) {
            System.out.println("ERROR got exception when sending data");
        }
	}

	protected void dumpMessage(TelnetMessage msg)
	{
		MonitorUser u = userMap.get(msg.sock);
		String user = msg.sock.getHostAddress();
		if (u != null && u.username != null)
			user = u.username;
		System.out.println("["+user+"] : ["+msg.text+"]");
	}

    protected void processUser(MonitorUser u,String s) throws IOException {
        if(u.state == UserStateEnum.LOGIN) {
            u.username = s;
            u.state = UserStateEnum.ACTIVE;
            printlnAll("Welcome "+u.username);
        }
        else if(u.state == UserStateEnum.ACTIVE) {
            if(s.equalsIgnoreCase("help")) {
                u.sock.println("Commands: cls, who, tick, logout, shutdown");
            }
            else if(s.equalsIgnoreCase("quit") || s.equalsIgnoreCase("exit") || s.equalsIgnoreCase("logout") || s.equalsIgnoreCase("logoff")) {
                u.sock.println("Good bye");
                close(u);
            }
            else if(s.equalsIgnoreCase("cls")) {
                u.sock.cls();
            }
        	else if(s.equalsIgnoreCase("who")) {
        		who(u.sock);
        	}
        	else if(s.equalsIgnoreCase("tick")) {
        		u.setTicking(!u.isTicking());
        		u.sock.println("Ticking is "+(u.isTicking() ? "set":"stopped"));
        	}
            else if(s.equalsIgnoreCase("shutdown"))
            {
                shutdown(u);
            }
        	else {
        		printlnAll(u.username+":"+s);
        	}
        }
    }

    protected void shutdown(MonitorUser u) throws IOException {
        printlnAll("Server shutting down now");
        stopServer();
    }

    @Override
    public void idle(long deltaTime) {
        long now = System.currentTimeMillis();
        printlnAllUsers("tick ["+now+"]");
    }

    public void printlnAllUsers(String s)
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
    
    protected void who(TelnetServerSocket sock) throws IOException
    {
		String format = "%-20s %-20s %-20s";
		sock.println(String.format(format,"USER","STATUS","IDLE"));
		sock.println(String.format(format,"----","------","----"));
    	Iterator<MonitorUser> it = userMap.values().iterator();
    	while(it.hasNext()) 
    	{
    		MonitorUser u = it.next();
    		sock.println(String.format(format,u.userNameHost(),u.state.name(),u.timer.toString()));
    	}
    }

    public void close(MonitorUser u)
    {
        userMap.remove(u.sock);
        u.sock.close();        
    }

	public static void main(String[] args) throws Exception 
	{
		int hostPort = HOST_PORT;
		MonitorServer server = new MonitorServer(hostPort,IDLE_DELAY);
		System.out.println("Running on port "+hostPort+"...");
		server.run();
	}

}
