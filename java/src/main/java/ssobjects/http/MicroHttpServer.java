package ssobjects.http;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by lee on 2/11/17.
 */
public class MicroHttpServer
{
    static final public String DATE_FORMAT = "EEE, d MMM yyyy HH:mm:ss z";
    static final public String GET = "GET";
    static final public String PATH = "PATH";
    static final public String HTTP_VER = "HTTP";
    static final public String CONNECTIONS = "CONNECTIONS";
    static final public int    MAX_THREADS = 50;

    protected InetAddress hostAddress;
    protected int hostPort;
    protected ServerSocket listen;
    protected boolean running = false;
    protected HashMap<Socket,MicroHandler> handlers = new HashMap<>();
    protected HashMap<String,Long> metrics = new HashMap<>();
    protected AtomicLong maxConnections = new AtomicLong(0);        //the number of connections ever made
    protected AtomicLong activeConnections = new AtomicLong(0);     //the number of connections currently active
    protected AtomicLong maxActiveConnections = new AtomicLong(0);     //the max number of active connections
    protected static final ExecutorService exec = Executors.newFixedThreadPool(MAX_THREADS);
//    protected static final Executor exec = new ssobjects.http.SingleThreadExecutor();

    public MicroHttpServer(int port)
    {
        this.hostPort = port;
    }

    /** Override to return your own handler. */
    public MicroHandler getHandler(Socket sock)
    {
        return new MicroHandler(this,sock);
    }

    public long handlerCount() { return handlers.size(); }

    public void stop(boolean b)
    {
        running = false;
        try
        {
            System.out.println("Stopping "+handlers.size()+" handlers.");
            for(Socket sock : handlers.keySet())
            {
                System.out.println("Closing socket "+sock.getPort());
                MicroHandler h = handlers.get(sock);
                h.stop();
            }
            System.out.println("Stopping server.");
            listen.close();
        }
        catch(Exception e)
        {
            if(!listen.isClosed())
                e.printStackTrace();
        }
    }

    public void addHandler(MicroHandler h)
    {
        handlers.put(h.getSocket(),h);

        long count = 0;
        if(metrics.containsKey(CONNECTIONS))
        {
            count = metrics.get(CONNECTIONS);
        }
        ++count;

        metrics.put(CONNECTIONS,count);
    }

    public void incrementActiveThreads()
    {
        long a = activeConnections.incrementAndGet();
        if(a>maxActiveConnections.get())
            maxActiveConnections.incrementAndGet();
    }
    public void decrementActiveThreads()
    {
        activeConnections.decrementAndGet();
    }
    public void removeSocket(Socket sock)
    {
        if(handlers.containsKey(sock))
            handlers.remove(sock);
    }

    public void run()
    {
        try
        {
            running = true;
            listen = new ServerSocket(hostPort);
            System.out.println("Listening on port " + hostPort);
            while (running)
            {
                Socket sock = listen.accept();

                long n = maxConnections.incrementAndGet();
//                System.out.println("Got connection, max ["+n+"] max active ["+maxActiveConnections.get()+"] connections");
                MicroHandler handler = getHandler(sock);
//                handlers.put(sock,handler);
                exec.execute(handler);
            }
        }
        catch(IOException e)
        {
            if(listen != null && listen.isClosed())
                System.out.println("Server has been stopped");
            else
                e.printStackTrace();
        }
    }

    public static void main(String[] args) throws Exception
    {
        final MicroHttpServer server = new MicroHttpServer(9000);
        System.out.println("Creating thread");
        Thread t = new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                server.run();
            }
        });
        System.out.println("Starting thread");
        t.start();
        System.out.println("Thread running");
        while(true)
        {
            Thread.sleep(3000);
            System.out.println("Server still running with active ["+server.activeConnections.get()+"] max ["+server.maxConnections.get()+"] max active ["+server.maxActiveConnections.get()+"] connections");
        }
//        server.stop(false);
    }
}
