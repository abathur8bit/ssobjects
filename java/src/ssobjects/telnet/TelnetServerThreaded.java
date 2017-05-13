package ssobjects.telnet;

import ssobjects.telnet.threads.TelnetServerHandler;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

public abstract class TelnetServerThreaded extends Thread
{
//	static Logger log = Logger.getLogger(TelnetServerThreaded.class.getName());
    //TODO if we get an IOException on send methods we should probably close this socket

    static public final String CRLF = "\r\n";
    static public final long DEFAULT_IDLE = 1000;

    protected AtomicBoolean running = new AtomicBoolean();
    protected InetAddress hostAddress;
    protected int hostPort;
    protected Map<SocketChannel,TelnetServerSocket> serverSockMap = new HashMap<SocketChannel,TelnetServerSocket>();
    protected ServerSocketChannel serverChannel;
    protected Selector selector;
    protected List<TelnetMessage> messageQueue = new ArrayList<TelnetMessage>();
    protected long idleTime = DEFAULT_IDLE;  //idle in milliseconds
    protected DateFormat df = new SimpleDateFormat("yyyy/MM/dd kk:mm:ss");
    protected List<TelnetServerHandler> handlers = new ArrayList<TelnetServerHandler>();

    public TelnetServerThreaded(InetAddress host,int port) throws IOException {
        this(host,port,DEFAULT_IDLE);
    }

    abstract protected TelnetServerHandler createHandler(int id);

    //user overrides
    public void connectionAccepted(TelnetServerSocket sock) {}
    public void connectionClosed(TelnetServerSocket sock,IOException e) {}
    public void idle(long deltaTime) {}

    public TelnetServerThreaded(InetAddress host,int port,long idle) throws IOException {
        this.hostAddress = host;
        this.hostPort = port;
        selector = initSelector();
        idleTime = idle;
    }

    public String timestamp() {return df.format(new Date());}

    public void error(String s,Exception e) {
        error(s);
        error(e);
    }

    public void error(Exception e) {
        e.printStackTrace();
    }

    public void error(String s) {
        System.out.println("[ERROR] "+timestamp()+" "+s);
    }

    public void warn(String s) {
        System.out.println("[WARN]  "+timestamp()+" "+s);
    }

    public void info(String s) {
        System.out.println("[INFO]  "+timestamp()+" "+s);
    }

    public void debug(String s) {
        System.out.println("[DEBUG] "+timestamp()+" "+s);
    }

    /** Send message to all connected sockets, with a CRLF appended automatically, except for the one socket. */
    public void printlnAllExcept(String s,TelnetServerSocket except) throws IOException {
        printAllExcept(s+CRLF,except);
    }

    /** Send message to all connected sockets, except for the one socket. */
    public synchronized void printAllExcept(String s,TelnetServerSocket except) throws IOException {
        if(!running.get()) return;

        for(TelnetServerSocket sock : serverSockMap.values()) {
            if(sock != except) {
                try {
                    sock.sendString(s);
                } catch(IOException e) {
                    //TODO need to close socket

                }
            }
        }
    }

    /** Send message to all connected sockets, with a CRLF appended automatically. */
    public synchronized void printlnAll(String s) throws IOException {
        printAll(s+CRLF);
    }

    /** Send message to all connected sockets. */
    public synchronized void printAll(String s) throws IOException {
        if(!running.get()) return;

        for(TelnetServerSocket sock : serverSockMap.values()) {
            try {
                sock.sendString(s);
            } catch(IOException e) {
                //TODO need to close socket
            }
        }
    }

    /** Puts a telnet message on the queue so any handler can grab it. */
    protected synchronized void addMessage(TelnetMessage msg) {
        messageQueue.add(msg);
        //notify
    }

    /** Handlers call this to get the next message. If there are no messages,
     * null is returned. Ideally the handler would block somewhere and wait
     * until a notify is done somewhere.
     *
     * @return null or the next message.
     */
    public synchronized TelnetMessage getMessage() {
        if(!running.get()) return null;

        //wait for a message
        TelnetMessage msg = null;
        if(messageQueue.size() > 0) {
            msg = messageQueue.get(0);
            messageQueue.remove(0);
        }
        return msg;
    }

    /** Returns if we have messages waiting to be processed. */
    public synchronized boolean hasMessage() {
        return messageQueue.size() > 0;
    }

    /**
     * Tells the server to end and exit, but letting the run() method know
     * we need are no longer running.
     */
    public synchronized void stopServer() {
        running.set(false);
    }

    /**
     * Main processing thread that accepts new connections, reads from sockets.
     * Also figures out when to call idle() to do timed processing.
     */
    public void runServer()
    {
        running.set(true);
        long now = System.currentTimeMillis();
        long waitUntil = now+idleTime;
        long timeout=idleTime;
        long lastIdleCall=now;
        long idleCalledAt = now;

        createHandlers();

        while(running.get())
        {
            try
            {
                boolean needProcessing = false;
                Set<SelectionKey> keys = selector.keys();
                //System.out.println("select: num keys="+keys.size());
                int readyKeyCount = selector.select(timeout);   //sleep or if there is data or new connection return right away when that happens
//                int readyKeyCount = selector.select();
                //System.out.println("readyKeyCount="+readyKeyCount);
                if(readyKeyCount > 0)
                {
                    Iterator<SelectionKey> selectedKeys = selector.selectedKeys().iterator();
                    while(selectedKeys.hasNext())
                    {
                        SelectionKey key = selectedKeys.next();
                        selectedKeys.remove();
                        if(!key.isValid())
                            continue;

                        if(key.isAcceptable())
                            accept(key);
                        else if(key.isReadable()) {
                            needProcessing |= read(key);    //reads data, and posts to the message queue if we have a line of data
                        }
                    }
                }
                now = System.currentTimeMillis();
                if(now >= waitUntil) {
                    timeout = idleTime;     //max idle time
                    idleCalledAt = now;
                    idle(now-lastIdleCall);
                    lastIdleCall = now;

                    //take into account how long the idle call took, and subtract that from out wait time
                    now = System.currentTimeMillis();
                    timeout -= now-idleCalledAt;
                    if(timeout < 1)
                        timeout=1;  //minimum wait is 1 ms

                    waitUntil = now+timeout;
                }
                else {
                    timeout = waitUntil-now;  //still have some idle time left, next call to select will sleep it off
                }
            }
            catch(Exception e)
            {
                e.printStackTrace();
                running.set(false);
            }
        }
        warn("Server stopping, stopping handlers");
        for(TelnetServerHandler handler : handlers) {
            handler.stopHandler();
        }
        for(TelnetServerHandler handler : handlers) {
            warn("Waiting for handler ["+handler.getName()+"] to finish");
            handler.waitToFinish();
        }
        warn("Server stopped all handlers, server stopped");
    }

    /**
     * Create handlers to process messages. Each handler is a separate thread.
     *
     * @return the number of handlers created
     */
    protected int createHandlers() {
        int count=0;
        for(int i=0; i<2; i++) {    //just creating two for testing
            TelnetServerHandler handler = createHandler(i);
            handler.start();
            handlers.add(handler);
            count++;
        }
        return count;
    }

    /** Accepts a new connection, and does any house keeping to add the new connection to
     * known connections.
     *
     * @param key The socket that we got a connection on.
     * @throws IOException
     */
    protected synchronized void accept(SelectionKey key) throws IOException
    {
        //System.out.println("accepting key");
        // For an accept to be pending the channel must be a server socket channel.
        ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();

        // Accept the connection and make it non-blocking
        SocketChannel socketChannel = serverSocketChannel.accept();
        //Socket socket = socketChannel.socket();
        socketChannel.configureBlocking(false);

        // Register the new SocketChannel with our Selector, indicating
        // we'd like to be notified when there's data waiting to be read
        socketChannel.register(this.selector, SelectionKey.OP_READ);

        TelnetServerSocket serverSock = new TelnetServerSocket(socketChannel);
        serverSockMap.put(socketChannel,serverSock);

        //TODO LBP: create thread here
        connectionAccepted(serverSock);
    }

    /**
     * Read from the given socket. Reads in as many bytes as are available.
     * If we have read a full line, post a message to the queue.
     *
     * @param key Socket we are reading from.
     * @return
     * @throws IOException There was an error closing the socket. By this point we have already detected a read error, and closed the socket.
     */
    protected boolean read(SelectionKey key) throws IOException
    {
        //System.out.println("reading key");
        SocketChannel socketChannel = (SocketChannel) key.channel();
        TelnetServerSocket serverSock = serverSockMap.get(socketChannel);
        if(null != serverSock) {
            int numRead = -1;
            IOException readException = null;	//if there was an exception, we can pass it to the server
            try
            {
                numRead = serverSock.readData();
                //System.out.println("read: ["+numRead+"] bytes");
            }
            catch(IOException e)
            {
                numRead = -1;	//we just got an error, so closing down socket
                readException = e;
            }

            if (numRead == -1) {
                // Remote entity shut the socket down cleanly. Do the
                // same from our end and cancel the channel.
                key.cancel();
                socketChannel.close();
                postCloseMsg(serverSock,readException);
            }
            else {

                //extract as many packets as we can from the socket
                TelnetMessage msg = null;
                while((msg = serverSock.extractPacket()) != null)
                    addMessage(msg);

                //System.out.println("read: Done processing");
            }
        }
        return messageQueue.size() > 0;
    }

    public void postCloseMsg(TelnetServerSocket sock,IOException e) {
        connectionClosed(sock,e);
    }

    public synchronized void close(TelnetServerSocket sock) {
//        try {
//            sock.key.channel().close();
//        } catch(IOException e) {}   //safely ignore since we are closing it anyway
//        sock.key.cancel();
        sock.close();
        serverSockMap.remove(sock.sock);
    }

    protected Selector initSelector() throws IOException
    {
        Selector socketSelector = SelectorProvider.provider().openSelector();
        serverChannel = ServerSocketChannel.open();
        serverChannel.configureBlocking(false);

        InetSocketAddress isa = new InetSocketAddress(hostAddress, hostPort);
        serverChannel.socket().bind(isa);
        serverChannel.register(socketSelector, SelectionKey.OP_ACCEPT);

        return socketSelector;
    }
}
