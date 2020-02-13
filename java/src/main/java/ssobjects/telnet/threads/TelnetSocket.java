package ssobjects.telnet.threads;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.logging.Logger;

public class TelnetSocket
{
    Logger log = Logger.getLogger(TelnetSocket.class.getName());
	public static final char ESCAPE = 27;
    public static final String CRLF = "\r\n";
    public static final int CR = 13;
    public static final int LF = 10;
    public static final int NUL = 0;

	private Socket sock;
	private BufferedReader in;
	private PrintWriter out;
	
	public TelnetSocket(Socket s) throws IOException 
	{
		sock = s;
		in = new BufferedReader(new InputStreamReader(s.getInputStream()));
		out = new PrintWriter(s.getOutputStream());
		
		InetAddress addr = s.getInetAddress();
		log.info("localhost "+InetAddress.getLocalHost());
		log.info("getHostName "+addr.getHostName());
		
	}

    public Socket socket() {return sock;}
    public InetAddress getSockAddr() {return socket().getInetAddress();}
    public String getHostAddress() {return getSockAddr().getHostAddress();}

    public void close() {
        try {
            sock.close();
        } catch(IOException e) {
            //can safely ignore exception
        }
    }
    
	public String readLine() throws IOException
	{
		return in.readLine();
	}
	
    public void println(String s) throws IOException 
    {
    	print(s+CRLF);	//using print to ensure that we know what is being sent as newline
    }
    
    public void print(String s) throws IOException 
    {
    	out.print(s);
    	out.flush();
    }
    
	public void cls() throws IOException
	{
		out.print(ESCAPE+"[2J");
		home();
	}

	public void home() throws IOException
	{
		cursorxy(1,1);
	}
	
	/** Position the cursor. 1,1 is top left of screen.*/
	public void cursorxy(int x,int y) throws IOException
	{
		out.print(ESCAPE+"["+y+";"+x+"H");
	}
}
