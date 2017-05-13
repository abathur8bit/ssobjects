package ssobjects.http;

import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;

/**
 * Contains all the request info, header, parameters, and payload of the REST request.
 * Created by lee on 2/13/17.
 */
public class Request
{
    public static final String HOST = "Host:";
    public enum Type {POST,GET};

    HashMap<String,String> parameters;
    HashMap<String,String> headers = new HashMap<>();
    Type requestType;
    int contentLength = 0;
    String url;
    String content;

    public String getHeader(String key)
    {
        return headers.get(key);
    }
    public String getParameter(String key)
    {
        return parameters.get(key);
    }
    public String[] getParameterKeys()
    {
        return parameters.keySet().toArray(new String[0]);
    }
    public Type getRequestType()
    {
        return requestType;
    }
    public void setRequestType(Type requestType)
    {
        this.requestType = requestType;
    }
    public int getContentLength()
    {
        return contentLength;
    }
    public void setContentLength(int contentLength)
    {
        this.contentLength = contentLength;
    }
    public String getUrl()
    {
        return url;
    }
    public void setUrl(String url)
    {
        this.url = url;
    }
    public void setContent(String s)
    {
        content = s;
    }
    public String getContent()
    {
        return content;
    }
    public void parseRequest(Reader reader) throws IOException
    {
        BufferedReader in = new BufferedReader(reader);
        String line;
        try
        {
            while ((line = in.readLine()) != null)
            {
                System.out.println("Processing line ["+line+"]");
                if (line.startsWith(Type.POST.toString()))
                {
                    setRequestType(Type.POST);
                    StringTokenizer tok = new StringTokenizer(line, " ");
                    tok.nextElement();  //skip type
                    setUrl(tok.nextToken());
                    String version = tok.nextToken();

                    System.out.println("POST url ["+getUrl()+"] version ["+version+"]");
                }
                else if(line.startsWith(Type.GET.toString()))
                {
                    setRequestType(Type.GET);
                    StringTokenizer tok = new StringTokenizer(line, " ");
                    tok.nextElement();  //skip type
                    setUrl(tok.nextToken());
                    String version = tok.nextToken();
                    headers.put(Type.GET.toString(),getUrl());

                    System.out.println("GET url ["+getUrl()+"] version ["+version+"]");
                }
                else if(line.startsWith(HOST))
                {
                    int start = line.indexOf(' ');
                    if(start == -1) throwInvalidHeader("Host invalid");
                    String host = line.substring(start).trim();
                    start = host.indexOf(':');
                    if(start != -1)
                    {
                        //get the host and port
                        String port = host.substring(start+1,host.length());
                        host = host.substring(0,start);
                        headers.put("PORT",port);
                    }
                    headers.put(HOST,host);

                    System.out.println("Setting host to ["+host+"]");
                }
                else if(line.length() == 0)
                {
                    parseContent(reader);
                }
                else
                {
                    int idx = line.indexOf(':');
                    if(idx == -1) throwInvalidHeader("Unknown header ["+line+"]");
                    String key = line.substring(0,idx).trim();
                    String value = line.substring(idx,line.length()).trim();
                    headers.put(key,value);
                }
            }
        }
        catch(NoSuchElementException e)
        {
            System.out.println("ERROR: Unable to parse header");
            throw new IOException(e);
        }
    }
    private void throwInvalidHeader(String msg) throws IOException
    {
        throw new IOException(msg);
    }
    public void parseContent(Reader reader) throws IOException
    {
        BufferedReader in = new BufferedReader(reader);
        StringBuffer contentBuffer = new StringBuffer(getContentLength());
        int count = 0;
        String s;
        while((s = in.readLine()) != null && count+s.length() < getContentLength())
        {
            if(contentBuffer.length() > 0)
                contentBuffer.append("\n");
            contentBuffer.append(s);
            count += s.length();
        }
        setContent(contentBuffer.toString());
    }
}
