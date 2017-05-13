#include <stdio.h>
#ifndef _WIN32
#include <signal.h>
#include <sys/types.h>
#include <sys/stat.h>
#endif 

#include <simpleserver.h>
#include <tsleep.h>
#include <telnetserver.h>
#include <telnetserversocket.h>

class ArthurServer : public TelnetServer
{
	public:
		enum {FREQUENCY = 5000}; 
		ArthurServer(SockAddr& saBind) : TelnetServer(saBind,FREQUENCY){};
		void processSingleMsg(PacketMessage* pmsg)
        {
          TelnetServerSocket* psocket = (TelnetServerSocket*)pmsg->socket();
          PacketBuffer* ppacket = pmsg->packet();
          switch(ppacket->getCmd())
          {
            //One way to handle the message. Process and reply within the switch.
            case PacketBuffer::pcNewConnection:   onConnection(pmsg);   break;
            case PacketBuffer::pcClosed:          printf("Connection closed.\n");          break;
            case TelnetServerSocket::pcFullLine:  onFullLine(pmsg);        break;
          }
          DELETE_NULL(ppacket);   //IMPORTANT! The packet is no longer needed. You must delete it.
        }
        void onFullLine(PacketMessage* pmsg)
        {
          TelnetServerSocket* psocket = (TelnetServerSocket*)pmsg->socket();
          char* pszString = (char*)pmsg->packet()->getBuffer();
          printf("Got from client: [%s]\n",pszString);

          psocket->println("Hello!");
          psocket->close();
        }
        void onConnection(PacketMessage* pmsg)
        {
        	TelnetServerSocket* psocket = (TelnetServerSocket*)pmsg->socket();
        	psocket->println("Daytime");
        }
};

int main(int argc, char const *argv[])
{
	try 
	{
			unsigned16 wPort = 9999;

		SockAddr saBind((ULONG)INADDR_ANY,wPort);
		ArthurServer server(saBind);          
    	if(!SimpleServer::canBind(saBind))  // check if we can bind to this port
      		printf("Can't bind\n");       // should not throw from main after server constructed
        else
        {

          printf("Server on port %d\n",9999);
          printf("Run 'pingclient' in another terminal to connect and test.\n");

          server.startServer();               // server will now listen for connections

          printf("server is finished.\n");

	    }
	} 
	catch (GeneralException& e)
	{
		printf("Got an error: %s", e.getErrorMsg());
	}

}
