import java.io.InputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;


public class SocketTest 
{
    public static void help()
    {
        System.out.println("Usage: SocketTest [-s|-c] -p PORT [-h HOST]");
        System.out.println("  HOST defaults to localhost");
        System.out.println("  PORT defaults to 9999");
        System.exit(0);
    }
    public static void main(String[] args) 
    {
        boolean isServer = false;
        boolean isClient = false;
        String host = "localhost";
        int port = 9999;
        
        if(args.length == 0)
            help();
        
        for(int i=0; i<args.length; i++)
        {
            if(args[i].equals("-s"))
            {
                isServer = true;
            }
            else if(args[i].equals("-c"))
            {
                isClient = true;
            }
            else if(args[i].equals("-p"))
            {
                port = Integer.parseInt(args[++i]);
            }
            else if(args[i].equals("-s"))
            {
                host = args[++i];
            }
        }
        
        if(isServer && isClient)
            help();
        
        if(isServer)
        {
            runServer(port);
        }
        else if(isClient)
        {
            runClient(host,port);
        }
    }
    
    public static void runClient(String host,int port)
    {
        try
        {
            System.out.println("Opening connection to "+host+":"+port+".");
            Socket s = new Socket(host,port);
            System.out.println("Got connection, closing");
            s.close();
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }
    
    public static void runServer(int port) 
    {
        try 
        {
            ServerSocket listen = new ServerSocket(port);
//            System.out.println("Sleeping before accept, connect and close client now.");
//            Thread.sleep(5000);
            while(true)
            {
                //what happens if there is a reset instead of a graceful close?
                //Is there a way to find the status of the socket to set if it has been reset?
                System.out.println("Server listening on port "+port);
                Socket s = listen.accept();
                System.out.println("Got connection from ["+s.getInetAddress().getHostAddress()+"]");
//                System.out.println("Sleeping to simulate error");
//                Thread.sleep(5000);
                try 
                {
                    InputStream in = s.getInputStream();
                    System.out.println("Got the inputstream");
                    int c = in.read();
                    System.out.println("read returned ["+c+"]");
//                    PrintWriter out = new PrintWriter(s.getOutputStream());
//                    out.println(""+new Date());
//                    out.flush();
//                    out.close();
                    s.close();
                }
                catch(Exception e)
                {
                    System.err.println("Error trying to read data ["+e.getMessage()+"]");
                    e.printStackTrace();
                }
            }        
        } 
        catch(Exception e)
        {
            System.err.println("Error of something else ["+e.getMessage()+"]");
            e.printStackTrace();
        }
    }
}
