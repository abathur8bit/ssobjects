package ssobjects.http;

import java.io.*;
import java.net.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.StringTokenizer;

/**
 * Created by lee on 2/11/17.
 */
public class MicroHandler implements Runnable
{
    public boolean dumpBytes = true;
    static final public String DATE_FORMAT = "EEE, d MMM yyyy HH:mm:ss z";
    static final public String GET = "GET";
    static final public String PATH = "PATH";
    static final public String HTTP_VER = "HTTP";

    protected MicroHttpServer server;
    protected Request request;
    protected Socket sock;
    protected boolean running = false;

    public MicroHandler(MicroHttpServer serv,Socket sock)
    {
        this.server = serv;
        this.sock = sock;
    }

    public Socket getSocket() {return sock;}

    public void stop()
    {
        try
        {
            running = false;
            server.removeSocket(sock);
            server.decrementActiveThreads();
            sock.close();
        }
        catch(IOException e)
        {
            if(!sock.isClosed())
                e.printStackTrace();
        }
    }

    public void run()
    {
        try
        {
            server.incrementActiveThreads();
            running = true;
            while (running)
            {
                BufferedReader in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
                PrintWriter out = new PrintWriter(sock.getOutputStream());

//                System.out.println("Thread ["+Thread.currentThread().getId()+"] Simulating work");
                Thread.sleep(700);
                HashMap<String,String> parameters = new HashMap<>();
//                System.out.println("Waiting for data");
                String s;
                while((s = in.readLine()) != null)
                {
                    System.out.println(s);
//                    request.parseLine(s);

                    if(s.length() == 0 && request.getContentLength()>-1)
                    {
                        request.parseContent(in);
                    }
                    else if(s.length() == 0)
                    {
                        DateFormat df = new SimpleDateFormat(DATE_FORMAT);
                        Date now = new Date();
//                        System.out.println("using date "+df.format(now));
                        StringBuffer response = new StringBuffer();
                        String html = "<html><body>Hello %s, I see you are %s. %s</body></html>";
                        String htmlOut = String.format(html,parameters.get("name"),parameters.get("state"),df.format(now));
                        response.append("HTTP/1.1 200 OK\n");
                        response.append("Date: "+df.format(now)+"\n");
                        response.append("Expires: -1\n");
                        response.append("Content-Type: text/html;charset=ISO-8859-1\n");
                        response.append("Content-Length: "+htmlOut.length()+"\n");
                        response.append("\n");
                        response.append(htmlOut);
                        System.out.println("Response: ["+response.toString()+"]");
                        out.print(response.toString());
                        out.flush();
                        break;
                    }
                }
                running = false;
                out.close();
                in.close();
                stop();
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public void parseParams(HashMap<String,String> parameters,String input)
    {
        StringTokenizer tok = new StringTokenizer(input," ?&");
        while(tok.hasMoreTokens())
        {
            String s = tok.nextToken();
            if(s.equals(GET))
                continue;   //ignore GET token
            else if(s.startsWith(HTTP_VER))
            {
                int i=s.indexOf('/');
                if(i != -1)
                {
                    String value = s.substring(i+1,s.length());
                    parameters.put(HTTP_VER, value);
                }
            }
            else if(s.indexOf("/") != -1)
            {
                if(!parameters.containsKey(PATH))
                {
                    parameters.put(PATH, s);
                }
                else
                {
                    continue;
                }
            }
            else
            {
                StringTokenizer paramTok = new StringTokenizer(s, "=");
                while (paramTok.hasMoreTokens())
                {
                    String key = paramTok.nextToken();
                    String value = paramTok.nextToken();
                    parameters.put(key, value);
                }
            }
        }
    }

    public void showParams(HashMap<String,String> parameters)
    {
        for(String key : parameters.keySet())
        {
            String value = parameters.get(key);
            System.out.println("key ["+key+"] value ["+value+"]");
        }
    }

}
