#ifndef WEBSOCKETINSTANCE_H
#define WEBSOCKETINSTANCE_H

#include <ssobjects/socketinstance.h>
#include <ssobjects/telnetserversocket.h>

//TODO implement encrypting from server
//TODO implement > 125 (16 bit) payloads
//TODO implement ping/pong (on server). Does it need to go through the message pump?

/**
 * \brief
 * Web Socket implementation that unmasks data.
 *
 * Starts out as a regular telnet/http connection,
 * then once the handshake is complete, the masked data from the client is
 * translated back into text.
 **/
class WebSocketInstance : public TelnetServerSocket
{
public:
    WebSocketInstance(const unsigned32 nBufferSize=18000,const int iTimeout=DEFAULT_SOCKET_TIMEOUT) : TelnetServerSocket(nBufferSize,iTimeout),m_state(0),m_payloadSize(0),m_masked(false),m_maskKey(0) {}
    WebSocketInstance(const SocketInstance& socket,const SockAddr& sa,const unsigned32 nBuffSize,const int iTimeout);
    PacketBuffer* extractPacket();
    PacketBuffer* extractWebSocketPacket();         ///< Extract packet from WebSocket frames.
    void closeFrame();                              ///< Send the close frame.
    void ping();                                    ///< Send a ping frame.
    void pong();                                    ///< Send a pong frame.
    void addPacketBuffer(const char* pszString);    ///< Override this so we can put data in websocket structure if socket is in websocket mode.
    void decodeXOR(unsigned8* pdest,const unsigned8* psource,const unsigned16 size); ///< Decodes a sequence of bytes using XOR and m_mask.
    void setState(int s) {m_state = s;}             ///< Set if this is in HTTP or WebSocket mode.
    int  state() {return m_state;}
    bool isWebSocket() {return m_state?true:false;}
    bool isMasked() {return m_masked;}
    unsigned16 payloadSize() {return m_payloadSize;}
    unsigned32 maskKey() {return m_maskKey;}
    unsigned8 mask(int i) {return m_mask[i];}
    void setMask(unsigned8* mask);              ///< Set the 4 byte mask value.

protected:
    int m_state;        ///< 0 indicates regular telnet/http, 1 indicates WebSocket
    unsigned16 m_payloadSize;
    bool m_masked;                                  ///< Is data from client masked?
    unsigned32 m_maskKey;
    unsigned8 m_mask[4];
};

#endif // WEBSOCKETINSTANCE_H
