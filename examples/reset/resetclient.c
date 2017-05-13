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

int main(int argc,char* argv[])
{
    int sockfd;
    int n;
    char recvline[255];
    int port=9999;
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

    //cause RST to be sent on close
    struct linger ling;
    ling.l_onoff = 1;   //cause RST to be send on close
    ling.l_linger = 0;
    if(-1==setsockopt(sockfd,SOL_SOCKET,SO_LINGER,&ling,sizeof ling))
        errsys("setsockopt error");
    close(sockfd);
}

