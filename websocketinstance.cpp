#include <arpa/inet.h>
#include <stdlib.h>

#include "websocketinstance.h"

WebSocketInstance::WebSocketInstance(const SocketInstance &socket, const SockAddr &sa, const unsigned32 nBuffSize, const int iTimeout)
    : TelnetServerSocket(socket,sa,nBuffSize,iTimeout),m_state(0),m_payloadSize(0),m_masked(false)
{
}

// the spec calls for you to send the close, and wait for the client to response.
// I would think if you are closing, let the client know and just close the damn thing.
void WebSocketInstance::closeFrame()
{
    if(isWebSocket())
    {
        unsigned16* frame = (unsigned16*)m_pOutPtr;
        unsigned16 header = 0x8800;                                        //FIN set and Opcode = 0x8 in high byte, length 0
        *frame = htons(header);
        m_pOutPtr   += sizeof(unsigned16);
        m_nBytesOut += sizeof(unsigned16);
        sendBuffer();
    }
}

void WebSocketInstance::ping()
{
    if(isWebSocket())
    {
        unsigned16* frame = (unsigned16*)m_pOutPtr;
        unsigned16 header = 0x8900;                                        //FIN set and Opcode = 0x9 in high byte, length 0
        *frame = htons(header);
        m_pOutPtr   += sizeof(unsigned16);
        m_nBytesOut += sizeof(unsigned16);
        sendBuffer();
    }
}

void WebSocketInstance::pong()
{
    if(isWebSocket())
    {
        unsigned16* frame = (unsigned16*)m_pOutPtr;
        unsigned16 header = 0x8A00;                                        //FIN set and Opcode = 0xA in high byte, length 0
        *frame = htons(header);
        m_pOutPtr   += sizeof(unsigned16);
        m_nBytesOut += sizeof(unsigned16);
        sendBuffer();
    }
}

/** \brief
 * Processes adding a string to the WebSocket buffer. Note that the server is required to
 * NOT mask data sent to the client, according to section 5.1 of RFC6455.
 **/
void WebSocketInstance::addPacketBuffer(const char* pszString)
{
    if(isWebSocket())
    {
        const unsigned long long longlen = strlen(pszString);
        if(longlen > 0xFFFF)
        {

            throwSocketInstanceException("Longer then 0xFFFF bytes not yet supported");  //TODO need to support larger data set
        }

        const unsigned16 len = (unsigned16)longlen;

        unsigned16 header = 0x8100;                             //FIN set and Opcode = 0x1 in high byte, length 0 till we know how big it is
        unsigned8* p = (unsigned8*)m_pOutPtr;
        unsigned32 nSize = sizeof(header)+len;
        if(len<=125)
        {
//            DLOG("sending with 2 byte header len is %d strlen is %d",len,strlen(pszString));
            header += len;
            *((unsigned16*)p) = htons(header);                      //store the header in network order
            p += sizeof(header);                                    //point past the header
            memcpy(p,pszString,len);                                //store the string without encoding it
        }
        else
        {
//            DLOG("sending with 4 byte header len is %d strlen is %d",len,strlen(pszString));
            header += 126;
            *((unsigned16*)p) = htons(header);                      //store the header in network order
            p += sizeof(header);                                    //point past the header
            *((unsigned16*)p) = htons(len);                         //put length in next 2 bytes
            p += sizeof(len);                                       //past length
            memcpy(p,pszString,len);                                //store the string without encoding it
            nSize+=2;                                               //we need to add the 2 bytes for header size
        }


        //adjust buffer position
        m_pOutPtr   += nSize;
        m_nBytesOut += nSize;
    }
    else
    {
        TelnetServerSocket::addPacketBuffer(pszString);
    }
}

PacketBuffer* WebSocketInstance::extractPacket()
{
//    LOG("WebSocketInstance called");

    PacketBuffer*   pPacket = NULL;
    if(m_nBytesIn)
    {
        if(isWebSocket())
            pPacket = extractWebSocketPacket();
        else
            pPacket = TelnetServerSocket::extractPacket();
    }
    return pPacket;
}

PacketBuffer* WebSocketInstance::extractWebSocketPacket()
{
    if(m_nBytesIn<2)
        return NULL;    //can't do anything till we read at least the first 2 bytes

    char*       phead   = m_pInBuff;
//        char*       ptail   = phead+m_nBytesIn;
    char*       p       = phead;
    byte        opcode  = (*p & 0x0F);  //take last 4 bits
    //TODO opcode needs to be checked for text mode
    ++p;
    m_masked = (*p & 0x80) ? true:false;
    m_payloadSize = *p & 0x7F;
    ++p;
//    LOG("Payload size is %d",m_payloadSize);

    if(m_payloadSize == 126)
    {
        if(m_nBytesIn < 4)
            return NULL;    //can't process payload size, as we don't have enough data

        //payload is in next 2 bytes as a 16-bit unsigned
        unsigned16* psize = (unsigned16*)p;
        m_payloadSize = ntohs(*psize);
        p+=2;   //point past payload size
    }
    else if(m_payloadSize == 127)
    {
        //payload is in next 8 bytes as a 64-bit unsigned
        LOG("Unsupported payload size");
        cleanup();
        throwSocketInstanceException("Unsupported payload length");
    }

    //figure out how many bytes we are suppose to get with the frame info, mask, and payload size
    unsigned16 expectedBytesIn = 0;
    if(m_payloadSize<126)
        expectedBytesIn = 2+m_payloadSize;
    if(m_payloadSize == 126)
        expectedBytesIn = 4+m_payloadSize;
    if(isMasked())
        expectedBytesIn += 4;

    if(m_nBytesIn < expectedBytesIn)
        return NULL;    //we don't have the frame and full payload yet

    if(isMasked())
    {
        //get the masking key
        int i=0;
        m_mask[i++] = *(p++);
        m_mask[i++] = *(p++);
        m_mask[i++] = *(p++);
        m_mask[i++] = *(p++);
    }

    //payload comes next, decode using the XOR mask
    PacketBuffer* pPacket = new PacketBuffer(PacketBuffer::pcUser+opcode,m_payloadSize+1); //payload plus null terminator
    unsigned8* pDecoded = pPacket->getBuffer();
    unsigned8* pEncoded = (unsigned8*)p;

    if(isMasked())
        decodeXOR(pDecoded,pEncoded,m_payloadSize);
    *(pDecoded+m_payloadSize) = '\0';                       //null terminate the string

    rotateBuffer(getInBuffer(),getInBufferSize(),expectedBytesIn);
    m_nBytesIn -= expectedBytesIn;
    m_pInPtr   -= expectedBytesIn;

    return pPacket;
}

/**
 * \brief
 * Decode a sequence of bytes using XOR.
 *
 * See
 *
 *  https://developer.mozilla.org/en-US/docs/Web/API/WebSockets_API/Writing_WebSocket_servers#Reading_and_Unmasking_the_Data
 *
 * for more details.
 **/
void WebSocketInstance::decodeXOR(unsigned8* pdest,const unsigned8* psource,const unsigned16 size)
{
    for(unsigned16 i = 0; i<size; ++i)
    {
        *pdest = *psource ^ m_mask[i % 4];
        ++pdest;
        ++psource;
    }
}

//void WebSocketInstance::setMask(unsigned8* mask)
//{
//    m_mask[0] = *(mask);    //we could use a for loop, or for speed, just unwrap it
//    m_mask[1] = *(mask+1);
//    m_mask[2] = *(mask+2);
//    m_mask[3] = *(mask+3);
//}
