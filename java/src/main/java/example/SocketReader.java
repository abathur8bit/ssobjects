package example;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

public class SocketReader
{

	public SocketReader()
	{
	}

	public static void main(String[] args) throws Exception
	{
		int port = 4002;
		String host = "localhost";
		
		for(int i=0; i<args.length; i++)
		{
			if("--host".equalsIgnoreCase(args[i]))
				host = args[++i];
			else if("--port".equalsIgnoreCase(args[i]))
				port = Integer.parseInt(args[++i]);
		}
			
		Socket sock = new Socket(host,port);
//		BufferedReader in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
//		BufferedWriter out = new BufferedWriter(new OutputStreamWriter(sock.getOutputStream()));
		sock.close();
//		login(out,in);
//		String line=null;
//		while((line = in.readLine()) != null)
//		{
//			System.out.println("["+line+"]");
//		}
		System.out.println("Closed by server");
	}
	
	public static void login(BufferedWriter out,BufferedReader in) throws IOException
	{
		String line = in.readLine();
		System.out.println("line "+line);
		out.write("socketreader\r\n");
		out.flush();
		line = in.readLine();
		System.out.println("line "+line);
		out.write("Hello everyone\r\n");
		out.flush();
		line = in.readLine();
		System.out.println("line "+line);
		out.write("tick\r\n");
		out.flush();
		
	}
}
