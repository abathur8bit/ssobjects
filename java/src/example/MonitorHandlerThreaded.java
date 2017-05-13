package example;

import ssobjects.telnet.TelnetMessage;
import ssobjects.telnet.TelnetServerSocket;
import ssobjects.telnet.TelnetServerThreaded;
import ssobjects.telnet.threads.TelnetServerHandler;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

/**
 * Created by lbpatterson on 11/21/2014.
 */
public class MonitorHandlerThreaded extends TelnetServerHandler {
    MonitorServerThreaded server;

    public MonitorHandlerThreaded(MonitorServerThreaded server,String name) {
        super(server,name);
        this.server = server;
    }

    @Override
    public void processMessage(TelnetMessage msg) throws IOException {
        MonitorUser u = server.getUser(msg.sock);
        if(null != u) 
        {
            u.updateIdle();
            processUser(u,msg.text);
        }
        else
        {
            System.out.println("Ignoring telnet message from "+msg.sock.getHostAddress());
            server.close(msg.sock);
        }
    }
    
    @Override
    public void handleError(Exception e,TelnetServerSocket sock) {} 

    protected void processUser(MonitorUser u,String s) throws IOException {
        if(u.state == UserStateEnum.LOGIN) {
            u.username = s;
            u.state = UserStateEnum.ACTIVE;
            server.printlnAll("Welcome " + u.username);
        }
        else if(u.state == UserStateEnum.ACTIVE) {
            if(s.equalsIgnoreCase("help")) {
                u.sock.println("Commands: cls, who, tick, busy, logout, shutdown");
                u.sock.println("Chat by starting a line with a single quote '");
            }
            else if(s.equalsIgnoreCase("quit") ||
                    s.equalsIgnoreCase("exit") ||
                    s.equalsIgnoreCase("logout") ||
                    s.equalsIgnoreCase("logoff"))
            {
                u.sock.println("Good bye");
                close(u);
            }
            else if(s.equalsIgnoreCase("cls")) {
                u.sock.cls();
            }
            else if(s.equalsIgnoreCase("who")) {
                who(u.sock);
            }
            else if(s.equalsIgnoreCase("busy")) {
                u.sock.println("Very busy...Nothing else can be done on this thread.");
                u.state = UserStateEnum.BUSY;
                busy(u);
                u.state = UserStateEnum.ACTIVE;
                u.sock.println("Done busy thing");
            }
            else if(s.equalsIgnoreCase("tick")) {
                u.setTicking(!u.isTicking());
                u.sock.println("Ticking is "+(u.isTicking() ? "set":"stopped"));
            }
            else if(s.equalsIgnoreCase("shutdown")) {
                shutdown(u);
            }
            else if(s.startsWith("'")) {
                server.printlnAll(u.username+":"+s.substring(1));
            }
            else {
                u.sock.println("Invalid command. Try 'help'");
            }
        }
        else {
            u.sock.println("Invalid command");
        }

        u.sock.print("> ");
    }

    protected void shutdown(MonitorUser u) throws IOException {
        boolean canShutdown = true;
        for(MonitorUser user : server.getUserList()) {
            if(user.state == UserStateEnum.BUSY) {
                canShutdown = false;
                break;
            }
        }
        if(canShutdown) {
            server.printlnAll("Server shutting down now");
            server.stopServer();
        }
        else {
            u.sock.println("Sorry, busy users, cant' shutdown");
        }
    }

    protected void who(TelnetServerSocket sock) throws IOException
    {
        String format = "%-20s %-20s %-20s";
        sock.println(String.format(format,"USER","STATUS","IDLE"));
        sock.println(String.format(format,"----","------","----"));
        List<MonitorUser> users = server.getUserList();
        for(MonitorUser u : users)
        {
            sock.println(String.format(format,u.userNameHost(),u.state.name(),u.timer.toString()));
        }
    }

    public void close(MonitorUser u)
    {
        server.removeUser(u.sock);
        u.sock.close();
    }

    protected void busy(MonitorUser u) {
        for(long i=0; i<1000000000; i++)
        {
            double j=i*123.2313;
            j=j/2;
        }
        try
        {
            sleep(5000);
        } catch(InterruptedException e) {}
    }
}
