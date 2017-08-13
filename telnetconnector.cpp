#include <string.h>

#include "telnetconnector.h"

TelnetConnector::TelnetConnector(int socketTimeout)
    : m_socket(TELNET_CONNECTOR_MAXBUFFER),m_bytesRead(0),m_socketTimeout(socketTimeout)
{

}

void TelnetConnector::connect(const char *host, word port)
{
    SockAddr saServer(host,port);
    m_socket.create();
    m_socket.connect(saServer);
    m_bytesRead=0;
}

void TelnetConnector::close()
{
    m_socket.close();
}



/**
 * Returns nullptr if there is not enough data for a full line.
 * Returns a pointer to the buffer passed in if a line was read.
 *
 * **Note**
 * NULL is returned if the max is smaller then what has been read. This might cause issues down
 * the road. I'm working on making it throw an exception or toss out the excess data like recv() would do.
 *
 * Another thing, I should perhaps think about returning the number of bytes instead of a pointer. The pointer
 * makes it easy to reference the readLine as a string.
 */
char* TelnetConnector::readLine(char* dest,int max)
{
    char* parsed = nullptr;
    do
    {
        read(m_buffer,m_bytesRead,sizeof(m_buffer)-m_bytesRead);
        parsed = parseLine(dest,max);
    } while(!parsed);
    return parsed;
}

char* TelnetConnector::parseLine(char* dest,int max)
{
    if(m_bytesRead<2)
        return nullptr;

    int watermark = max<m_bytesRead ? max:m_bytesRead;   //the smaller of the max bytes to check

    //check for a blank line in the buffer
    if('\r'==m_buffer[0] && '\n'==m_buffer[1])
    {
        *dest='\0';                                                     //blank line
        m_bytesRead-=2;                                                 //buffer size reduced by # bytes copied plus the \r\n chars
        if(m_bytesRead)
        {
            memmove(m_buffer,(const char*)m_buffer+2,m_bytesRead);    //move unread data to beginning of buffer
        }
        return dest;
    }

    //parse till we get a \r\n, and return everything before the \r\n ...
    //...and remove the \r\n from the buffer.
    for(int i=1; i<watermark; ++i)  //we know that [0] will not be a \r, so skip that byte
    {
        if('\r'==m_buffer[i] && i+1 < watermark && '\n'==m_buffer[i+1])
        {
            memcpy(dest,m_buffer,i);
            dest[i]='\0';                                                   //null terminate dest
            m_bytesRead-=(i+2);                                             //buffer size reduced by # bytes copied plus the \r\n chars
            if(m_bytesRead)
            {
                memmove(m_buffer,(const char*)m_buffer+i+2,m_bytesRead);    //move unread data to beginning of buffer
            }
            return dest;
        }
    }

    return nullptr;
}

int TelnetConnector::read(char *buffer, int offset, int len)
{
    int n = m_socket.recv(buffer+offset,len,0); //read and don't wait if there is no data.
    m_bytesRead+=n;
    return n;
}

/**
   Writes a line of text to the socket, and addes CR & LF.

   \param fmt printf formatting string.
   \param ... printf parameters.

   \return The number of bytes sent.

   \throw SocketInstanceException If there is a socket error.
**/
int TelnetConnector::println(const char* fmt,...)
{
  char buffer[TELNET_CONNECTOR_MAXBUFFER];
  va_list ap;

  va_start(ap,fmt);
  vsnprintf(buffer,sizeof(buffer)-2,fmt,ap);
  va_end(ap);
  NULL_TERMINATE(buffer,sizeof buffer);

  strcat(buffer,"\r\n");
  return sendString(buffer);
}

/**
   Writes a line of text to socket.

   \param fmt printf formatting string.
   \param ... printf parameters.

   \return The number of bytes sent.

   \throw SocketInstanceException If there is a socket error.
**/
int TelnetConnector::print(const char* fmt,...)
{
  char buffer[TELNET_CONNECTOR_MAXBUFFER];
  va_list ap;

  va_start(ap,fmt);
  vsnprintf(buffer,sizeof buffer,fmt,ap);
  va_end(ap);
  NULL_TERMINATE(buffer,sizeof buffer);

  return sendString(buffer);
}

int TelnetConnector::sendString(const char *buffer)
{
    int len = strlen(buffer);
    int sent = 0;
    while(sent < len)   //make sure all data is sent, since this is done as a single call
    {
        sent += m_socket.send(buffer,strlen(buffer),m_socketTimeout);
    }
    return sent;
}
