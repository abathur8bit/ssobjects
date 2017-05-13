package example.threads;

import java.io.IOException;
import java.util.StringTokenizer;
import java.util.logging.Logger;

import ssobjects.telnet.threads.TelnetServerHandler;
import ssobjects.telnet.TelnetServerThreaded;
import ssobjects.telnet.threads.TelnetSocket;

//public class ExampleThreadedTelnetServer extends TelnetServerThreaded
//{
//	static Logger log = Logger.getLogger(ExampleThreadedTelnetServer.class.getName());
//	public static final int PORT = 4002;
//
//	public class ExampleHandler extends TelnetServerHandler
//	{
//		public ExampleHandler(TelnetSocket sock,ExampleThreadedTelnetServer server)
//		{
//			super(sock,server);
//		}
//
//		@Override
//		public void connectionAccepted() throws IOException
//		{
//			sock.println("Login:");
//		}
//
//		@Override
//		public void processMessage(String text) throws IOException
//		{
//			StringTokenizer tok = new StringTokenizer(text);
//			if(tok.countTokens()>0)
//			{
//				String cmd = tok.nextToken();
//				log.info("process message from ["+sock.getHostAddress()+"] msg ["+text+"]");
//				sock.println("Got message "+text);
//				if("sleep".equalsIgnoreCase(cmd))			doSleep();
//				else if("ticks".equalsIgnoreCase(cmd))		doTicks();
//				else if("say".equalsIgnoreCase(cmd))		doSay(cmd,text);
//				else if("shutdown".equalsIgnoreCase(cmd))	doShutdown();
//			}
//		}
//
//		private void doSleep() throws IOException
//		{
//			log.info("Sleeping for ["+sock.getHostAddress()+"]...");
//			sock.println("Sleeping...");
//			sleepy(5000);
//			sock.println("Sleeping done");
//			log.info("Sleeping for ["+sock.getHostAddress()+"] done");
//		}
//
//		private void doTicks() throws IOException
//		{
//			log.info("ticks for ["+sock.getHostAddress()+"]...");
//			for(int i=0; i<100; i++)
//			{
////				log.info("Tick for ["+sock.getHostAddress()+"]");
//				sock.println("tick "+System.currentTimeMillis());
//				sleepy(1000);
//			}
//		}
//
//		private void doSay(String cmd,String s) throws IOException
//		{
//			sock.println(sock.getHostAddress()+" : "+s.substring(cmd.length()).trim());
//		}
//
//		private void doShutdown() throws IOException
//		{
//			server.shutdown();
//		}
//	}
//
//	public ExampleThreadedTelnetServer()
//	{
//		super(null,4002,1000L);
//	}
//
//	@Override
//	public TelnetServerHandler createHandler(TelnetSocket s)
//	{
//		return new ExampleHandler(s,this);
//	}
//
//	public static void main(String[] args) throws Exception
//	{
//		ExampleThreadedTelnetServer server = new ExampleThreadedTelnetServer();
//		server.bind();
//		System.out.println("Server running on port "+PORT);
//		server.run();
//	}
//}
