#include <sys/types.h>
#include <ctype.h>
#include <stdio.h>
#include <arpa/inet.h>
#include <sys/time.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <unistd.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <netdb.h>

typedef struct sockaddr        SOCKADDR;

typedef struct sockaddr_in     SOCKADDR_IN;
typedef struct sockaddr_in*    LPSOCKADDR_IN;

void errsys(char* msg)
{
    perror(msg);
    printf("[%s]\n",msg);
    exit(-1);
}

void one()
{
    int n;
    int sockfd;
    char recvline[255];
    int port=3333;
    char host[255] = "localhost";
    struct sockaddr_in servaddr;

    if((sockfd = socket(AF_INET,SOCK_STREAM,0)) < 0)
        errsys("socket error");
    printf("host:port %s:%d\n",host,port);
    memset(&servaddr,0,sizeof servaddr);
    servaddr.sin_family = AF_INET;
    servaddr.sin_port = htons(port);
    if(inet_pton(AF_INET,host,&servaddr.sin_addr) <= 0)
        errsys("inet_pton");
    if(connect(sockfd,(SOCKADDR*)&servaddr,sizeof servaddr) < 0)
        errsys("connect error");
    while((n=read(sockfd,recvline,255))>0)
    {
        recvline[n]=0;
        if(fputs(recvline,stdout) == EOF)
            errsys("fputs error");
    }
    if(n<0)
        errsys("read error");

}

void two()
{
    int sockfd;
    int n;
    char recvline[255];
    int port=3333;
    char host[255] = "localhost";
    struct sockaddr_in sa;
    struct hostent* hen;
    
    printf("connecting to host:port %s:%d\n",host,port);
    hen = gethostbyname(host);
    if(!hen)
        errsys("gethostbyname error");

    memset(&sa,0,sizeof sa);
    sa.sin_family = AF_INET;
    sa.sin_port = htons(port);
    memcpy(&sa.sin_addr.s_addr, hen->h_addr_list[0], hen->h_length);

    if((sockfd = socket(AF_INET,SOCK_STREAM,0)) < 0)
        errsys("socket error");

    if(connect(sockfd,(SOCKADDR*)&sa,sizeof sa) < 0)
        errsys("connect error");
    while((n=read(sockfd,recvline,255))>0)
    {
        printf("Read %d bytes\n",n);
        recvline[n]=0;
        if(fputs(recvline,stdout) == EOF)
            errsys("fputs error");
    }
    if(n<0)
        errsys("read error");

}

void three()
{
    int sockfd;
    int n;
    char recvline[255];
    int port=3333;
    char host[255] = "localhost";
    struct sockaddr_in sa;
    struct hostent* hen;
    
    printf("connecting to host:port %s:%d\n",host,port);
    hen = gethostbyname(host);
    if(!hen)
        errsys("gethostbyname error");

    memset(&sa,0,sizeof sa);
    sa.sin_family = AF_INET;
    sa.sin_port = htons(port);
    memcpy(&sa.sin_addr.s_addr, hen->h_addr_list[0], hen->h_length);

    if((sockfd = socket(AF_INET,SOCK_STREAM,0)) < 0)
        errsys("socket error");

    if(connect(sockfd,(SOCKADDR*)&sa,sizeof sa) < 0)
        errsys("connect error");
    while((n=read(sockfd,recvline,255))>0)
    {
        printf("Read %d bytes\n",n);
    }
    if(n<0)
        errsys("read error");

}

int main(int argc,char* argv[])
{
//    one();
    two();
    //three();
}

