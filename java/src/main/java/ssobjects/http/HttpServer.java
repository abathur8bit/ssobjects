package ssobjects.http;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.StringTokenizer;

/**
 * Created by lee on 2/11/17.
 */
public class HttpServer
{
    static final public String DATE_FORMAT = "EEE, d MMM yyyy HH:mm:ss z";
    static final public String GET = "GET";
    static final public String PATH = "PATH";
    static final public String HTTP_VER = "HTTP";

    protected InetAddress hostAddress;
    protected int hostPort;
    protected ServerSocket listen;

    public HttpServer(InetAddress host, int port) throws IOException
    {
        this.hostAddress = host;
        this.hostPort = port;

    }

    public void server() throws IOException
    {
        listen = new ServerSocket(hostPort);
        System.out.println("Server listening on port "+hostPort);
        Socket sock = listen.accept();
        System.out.println("Socket accepted");

        System.out.println("Waiting for data");
        BufferedReader in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
        PrintWriter out = new PrintWriter(sock.getOutputStream());
        String s;
        while((s = in.readLine()) != null)
        {
            System.out.format("%s\n", s);

//            if(s.length() == 0)
//            {
//                DateFormat df = new SimpleDateFormat(DATE_FORMAT);
//                System.out.println("using date "+df.format(new Date()));
//                StringBuffer response = new StringBuffer();
//                String html = "<html>Hello</html>";
//                response.append("HTTP/1.1 200 OK\n");
//                response.append("Date: "+df.format(new Date())+"\n");
//                response.append("Expires: -1\n");
//                response.append("Content-Type: text/html;charset=ISO-8859-1\n");
//                response.append("Content-Length: "+html.length()+"\n");
//                response.append("\n");
//                response.append(html);
//                System.out.println("Response: ["+response.toString()+"]");
//                out.println(response.toString());
//                out.flush();
//            }
        }
        out.close();
        in.close();
        sock.close();
        listen.close();
    }

    public void client() throws IOException
    {
        String host="www.axorion.com";
        //String url = "/";
        String url = "/cgi-bin/findweather/getForecast?apiref=6b8d7d4184746eff&query=43.91806,-70.883362";
        System.out.println("Opening connection");
        Socket sock = new Socket(host,80);
        PrintWriter out = new PrintWriter(sock.getOutputStream());
        BufferedReader in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
        System.out.println("Connected. Sending request");
        out.println("GET "+url+" HTTP/1.1");
        out.println("Host: "+host);
        out.println("");
        out.flush();
        System.out.println("Reading reply");
        String s;
        while((s = in.readLine()) != null)
        {
            System.out.format("Read [%s]\n", s);
        }
        out.close();
        in.close();
        sock.close();
    }

    public void parseParams(HashMap<String,String> parameters,String input)
    {
        StringTokenizer tok = new StringTokenizer(input," ?&");
        while(tok.hasMoreTokens())
        {
            String s = tok.nextToken();
//            System.out.format("Token [%s]\n",s);
            if(s.equals(GET))
                continue;   //ignore GET token
            else if(s.startsWith(HTTP_VER))
            {
                int i=s.indexOf('/');
                if(i != -1)
                {
                    String value = s.substring(i+1,s.length());
                    parameters.put(HTTP_VER, value);
//                    System.out.format("HTTP [%s]\n",value);
                }
            }
            else if(s.indexOf("/") != -1)
            {
                if(!parameters.containsKey(PATH))
                {
//                    System.out.format("PATH [%s]\n", s);
                    parameters.put(PATH, s);
                }
                else
                {
                    continue;
                }
            }
            else
            {
//                System.out.format("token [%s] ", s);
                StringTokenizer paramTok = new StringTokenizer(s, "=");
                while (paramTok.hasMoreTokens())
                {
                    String key = paramTok.nextToken();
                    String value = paramTok.nextToken();
//                    System.out.format("key [%s] value [%s]", key, value);
                    parameters.put(key, value);
                }
//                System.out.println();
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

    //http://zergling.com/cgi-bin/findweather/getForecast?apiref=6b8d7d4184746eff&query=43.91806,-70.883362
    static public void main(String[] args) throws Exception
    {
        HttpServer serv = new HttpServer(null,9000);
        serv.server();

//        serv.client();
    }
}
