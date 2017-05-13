package ssobjects.telnet.threads;

import ssobjects.telnet.TelnetMessage;
import ssobjects.telnet.TelnetServerSocket;
import ssobjects.telnet.TelnetServerThreaded;

import java.io.IOException;
import java.net.Socket;
import java.nio.channels.ClosedChannelException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class TelnetServerHandler extends Thread
{
//	static Logger log = Logger.getLogger(TelnetServerHandler.class.getName());
	private AtomicBoolean isRunning = new AtomicBoolean(false);
    private AtomicBoolean hasStopped = new AtomicBoolean(false);
    protected TelnetServerThreaded server;

    /**
     * Don't do any i/o on the socket yet, wait till you are running in run()
     * 
     * @param serv Parent server that calls on the handler to perform work.
     * @param name Name of the thread
     */
    public TelnetServerHandler(TelnetServerThreaded serv,String name)
    {
        this.server = serv;
        setName(name);
    }

    /** 
     * Called by the handler when there is data to process. When a line of text comes 
     * in (the user types something and presses ENTER) this method is called. The msg 
     * contains the socket the text came from and the text that was typed.
     * 
     * The socket is in non-blocking mode. You can send data to it freely, but be aware that 
     * data isn't guaranteed to make it in one call. Check the return of send to see if all data
     * was in fact sent. If you require reading more data, you should set a state in a User object, 
     * and wait for processMessage to be called again, rather then reading from the socket. 
     * 
     * You should normally never read directly from the socket, but wait for processMessage instead.
     * 
     * @param msg Contains the socket and text, 
     * @throws IOException Typically if there was a send error.
     */
    abstract public void processMessage(TelnetMessage msg) throws IOException;
    
    /** 
     * Handle an error on the socket. Consider the socket closed, so don't try to 
     * send any data to it. You'll perform any house cleaning, or telling other
     * users that the user disconnected.
     * 
     * @param e Exception that happened.
     * @param sock The socket the had the error, the socket should be considered closed.
     */
    abstract public void handleError(Exception e,TelnetServerSocket sock);
    
    /**
     * Waits for a message to be posted to the queue, then calls processMessage(TelnetMessage).
     * If there is any exception thrown from processMessage, the socket will be closed.
     */
	public void run()
	{
        isRunning.set(true);
        hasStopped.set(false);
        while(isRunning.get())
        {
//            server.info("Handler ["+getName()+"] waiting for message grabbing message");
            TelnetMessage msg = server.getMessage();
//            server.info("Handler ["+getName()+"] got message ["+msg+"]");
            if(msg != null)
            {
                try
                {
                    processMessage(msg);
                }
                catch(ClosedChannelException e)
                {
                	server.info("Socket "+msg.sock.getHostAddress()+" closed");
                    server.close(msg.sock);
                    try {
                        handleError(e,msg.sock);    //ignore any exception
                    } catch(Exception e2) {}
                }
                catch(Exception e)
                {
                    server.error("Got exception handling message [" + e.getLocalizedMessage() + "] closing socket", e);
                    server.close(msg.sock);
                    try {
                        handleError(e,msg.sock);    //ignore any exception
                    } catch(Exception e2) {}
                }
            }
            sleepy(500);    //TODO LBP: taking this out once we have a blocking getMessage()
        }
        server.info("Handler [" + getName() + "] has stopped");
        hasStopped.set(true);
	}

	public void stopHandler()
	{
//        server.warn("Handler ["+getName()+"] trying to stop");
		isRunning.set(false);	//no longer running the thread since the socket is dead
//        server.warn("Handler ["+getName()+"] trying to stop DONE");
	}

    public void waitToFinish() {
        while(!hasStopped.get())
            sleepy(100);
    }

    public void sleepy(long ms)
    {
        try {
            Thread.sleep(ms);
        } catch(InterruptedException e) {}
    }
}
