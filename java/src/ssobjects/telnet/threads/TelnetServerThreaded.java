package ssobjects.telnet.threads;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

import ssobjects.telnet.TelnetServerSocket;

public abstract class TelnetServerThreaded extends Thread 
{
	static Logger log = Logger.getLogger(TelnetServerThreaded.class.getName());
	private AtomicBoolean isRunning=new AtomicBoolean(false);
	private HashMap<Socket,TelnetServerHandler> handlerMap = new HashMap<Socket,TelnetServerHandler>();
    protected InetAddress hostAddress;
    protected int hostPort;
    protected ServerSocket listen;

    public void connectionAccepted(TelnetServerSocket sock) {}
    public void connectionClosed(TelnetServerSocket sock,IOException e) {}
    public void idle(long deltaTime) {}

    public TelnetServerThreaded(InetAddress host,int port,long idle) 
    {
        this.hostAddress = host;
        this.hostPort = port;
    }

	public TelnetServerThreaded()
	{
	}

	private synchronized void addHandler(TelnetServerHandler h)
	{
//		handlerMap.put(h.socket(),h);
	}
	
	synchronized public void removeHandler(Socket s)
	{
		handlerMap.remove(s);
	}
	
	public void bind() throws IOException
	{
		InetSocketAddress isa = new InetSocketAddress(hostAddress, hostPort);
		listen = new ServerSocket();
		listen.bind(isa);
	}
	
	public void run()
	{
		isRunning.set(true);
		try
		{
			while(isRunning.get())
			{
				Socket sock = listen.accept();
				TelnetSocket telnetSock = new TelnetSocket(sock);
				TelnetServerHandler handler = createHandler(telnetSock);
				addHandler(handler);
				handler.start();
			}
		} 
		catch(IOException e)
		{
			e.printStackTrace();
		}
		
		log.info("Server finished");
	}
	
	synchronized public void shutdown() 
	{
		isRunning.set(false);
		try
		{
			listen.close();
		} 
		catch(IOException e)
		{
			//ignore
		}
		
		String[] keys = handlerMap.keySet().toArray(new String[0]);
		for(String key : keys)
		{
			TelnetServerHandler handler = handlerMap.get(key);
			if(handler != null)
			{
//				log.info("Shutting down handler for ["+handler.telnetSocket().getHostAddress()+"]");
//				handler.close();
			}
			else
			{
//				log.info("Shutting down handler for ["+handler.telnetSocket().getHostAddress()+"] already gone from map");
			}
		}
		while(handlerMap.size()>0)
		{
		}
	}
	
	/** Sleep and ignore any exceptions. */
	public void sleepy(long ms)
	{
		try {
			sleep(ms);
		} catch(InterruptedException e) {}
	}
	
	//override to implement your own handler
	abstract public TelnetServerHandler createHandler(TelnetSocket s);
	
	//TODO Handle parsing for entire list of connected sockets
	//TODO Handle closing server
}
