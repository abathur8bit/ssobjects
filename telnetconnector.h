#ifndef TELNETCONNECTOR_H
#define TELNETCONNECTOR_H

#include "socketinstance.h"

using namespace ssobjects;


#define TELNET_CONNECTOR_MAXBUFFER 5120

namespace ssobjects
{

class TelnetConnector
{
public:
    TelnetConnector(int socketTimeout);

    void connect(const char* host,word port);   ///< Connects to the given process.
    void close();                               ///< Close the connection.

    char* readLine(char* buffer,int max);   ///< Read a full line of text, but only read max-1 bytes. String is always null terminated.
    int print(const char* fmt,...);         ///< Send a string.
    int println(const char* fmt,...);       ///< Send a string terminated with \r\n.

protected:
    char* parseLine(char* dest,int max);    ///< Does the actual parsing from the internal buffer
    int read(char* buffer,int offset,int len);  ///< Read len bytes into buffer, buf start writing to buffer at offset.
    int sendString(const char* buffer);     ///< Send a null terminated string of data.

    SocketInstance m_socket;                    ///< The connection.
    char m_buffer[TELNET_CONNECTOR_MAXBUFFER];  ///< Where we store data we read until we parse it out.
    int m_bytesRead;
    int m_socketTimeout;                        ///< How long we should wait for blocked socket operations
};

}

#endif // TELNETCONNECTOR_H
