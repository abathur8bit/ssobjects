//#define DUMP

#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include <fcntl.h>   /* File control definitions */
#include <errno.h>   /* Error number definitions */
#include <termios.h> /* POSIX terminal control definitions */

#include "packet.h"

int serialfd;
unsigned char buffer[MAX_BUFFER_SIZE];// = {0xFF,0xFF, 1, 2, 3,0, 0xAA, 0xBB, 0xCC};
int buffPos=0;

struct PACKET* peekPacket = (PACKET*)buffer;
struct PACKET* ppacket = NULL;

void initHeader(struct PACKET_HEADER* p,int cmd,int length)
{
    p->cookie=0xFFFF;
    p->deviceId = 1;
    p->command = cmd;
    p->length = length;
}

void checkSerialPort()
{
    fd_set rset,wset,*pwset=NULL;
    FD_ZERO(&rset);
    FD_ZERO(&wset);
  
    FD_SET(serialfd,&rset);
  
    struct timeval tv = {0,0}; //don't sleep, just poll
    int iReady;
    
    //read as much as posible into buffer
    while((iReady = select(serialfd+1,&rset,pwset,NULL,&tv)) > 0)
    {
        int n=read(serialfd,buff,sizeof buff);
        if(n == -1)
            err_quit("Error reading"):
        buffer[buffPos] = read(serialfd,buff,sizeof(buff)-buffPos);
        buffPos++;
    }
}

//returns true if there is a full packet to be processed
//false otherwise.
//If the data doesn't start with a valid cookie, 
//just toss the data. A valid cookie is 0xFFFF.
struct PACKET* parsePacket()
{
    checkSerialPort();

    if(buffPos < sizeof(unsigned short))
        return NULL;   //can't check for cookie without enough data

    if(0xFF != buffer[0] || 0xFF != buffer[1])
    {
        //this is not a valid cookie, just toss a bytes, maybe next will be valid
        memmove(buffer,buffer+1,buffPos-1);
        buffPos -= 1;
        return NULL;
    }
        
    //ignore data till we have a full header
    if(buffPos<sizeof(PACKET_HEADER))
        return NULL;

    if(peekPacket->header.length>0 && buffPos<sizeof(PACKET_HEADER)+peekPacket->header.length)
        return NULL;

    if(peekPacket->header.length>MAX_BUFFER_SIZE - sizeof(PACKET_HEADER))
    {
        #ifdef AVR
        Serial.print("WARNING, got a packet length too large:");
        Serial.println(peekPacket->header.length);
        hardStop(0);
        #else
        printf("Warning, got a packet length too large\n");
        #endif
    }

    //at this point, we have a valid packet header and enough data read for a full packet with buffer
    ppacket = (PACKET*)malloc(sizeof(PACKET_HEADER)+peekPacket->header.length);
    memcpy(ppacket,buffer,sizeof(PACKET_HEADER)+peekPacket->header.length);
    
    //adjust buffer 
    memmove(buffer,buffer+sizeof(PACKET_HEADER)+peekPacket->header.length,buffPos-(sizeof(PACKET_HEADER)+peekPacket->header.length));
    buffPos -= sizeof(PACKET_HEADER)+peekPacket->header.length;

    return ppacket;
}

void err_quit(const char* pszMsg)
{
  puts(pszMsg);
  exit(0);
}

int openPort(char* portname)
{
    int fd = open(portname, O_RDWR | O_NOCTTY | O_NDELAY);
    if (fd == -1)
    {
    /*
	* Could not open the port.
	*/

	    perror("open_port: Unable to open /dev/ttyS0 - ");
    }
    else
	    fcntl(fd, F_SETFL, 0);

    return (fd);
}

void usage()
{
    printf("Reads from a serial port and echos to the screen. Good for monitoring what is coming in on serial port.\n");
    printf("Usage: serialread [--in /dev/cu.usbserial-A600e4IJ]\n");
    printf(" --in  Serial port to listen to\n");
    exit(0);
}

int main(int argc, char* argv[])
{
    int n,i;
    char serialPortName[255] = "/dev/cu.usbserial-A600e4IJ";

    for(i=0; i<argc; i++)
    {
        if(strcmp(argv[i],"--in")==0)
            strcpy(serialPortName,argv[++i]);
        else if(!strcmp(argv[i],"--help"))
            usage();
    }

    printf("Connecting to serial port %s\n",serialPortName);
    if(-1==(serialfd = openPort(serialPortName))) err_quit("Serial port error");

    char buff[1024];
    PACKET* ppkt;
    
    while((n=read(serialfd,buff,sizeof buff)) > 0)
    {
        for(i=0; i<n; i++)
        {
        #ifdef DUMP
            printf("%02X | %03d",(unsigned char)buff[i],(unsigned char)buff[i],(char)buff[i]);
            if(iscntrl(buff[i]))
            {
                printf(" | [.]\n"); //masked
            }
            else
            {
               printf(" | [%c]\n",(char)buff[i]);
            }
        #else
            printf("%c",buff[i]);
        #endif
        }
    }
}

// int main(int argc, char* argv[])
// {
    // int n,i;
    // char serialPortName[255] = "/dev/cu.usbserial-A600e4IJ";
// 
    // for(i=0; i<argc; i++)
    // {
        // if(strcmp(argv[i],"--in")==0)
            // strcpy(serialPortName,argv[++i]);
        // else if(!strcmp(argv[i],"--help"))
            // usage();
    // }
// 
    // printf("Connecting to serial port %s\n",serialPortName);
    // if(-1==(serialfd = openPort(serialPortName))) err_quit("Serial port error");
// 
    // char buff[1024];
    // while((n=read(serialfd,buff,sizeof buff)) > 0)
    // {
        // for(i=0; i<n; i++)
        // {
        // #ifdef DUMP
            // printf("%02X | %03d",(unsigned char)buff[i],(unsigned char)buff[i],(char)buff[i]);
            // if(iscntrl(buff[i]))
            // {
                // printf(" | [.]\n"); //masked
            // }
            // else
            // {
               // printf(" | [%c]\n",(char)buff[i]);
            // }
        // #else
            // printf("%c",buff[i]);
        // #endif
        // }
    // }
// }

