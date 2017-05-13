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
    int i;
    char recvline[255];
    int port=3333;
    char host[255] = "localhost";
    struct sockaddr_in sa;
    struct hostent* hen;

    for(i=1; i<argc; i++)
    {
        if(!strcmp(argv[i],"-h"))
        {
            strcpy(host,argv[++i]);
        }
        else if(!strcmp(argv[i],"-p"))
        {
            port = atoi(argv[++i]);
        }
    }

    printf("connecting to %s:%d\n",host,port);
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

//    unsigned char data[] = {255,255,1,1,0,0,123};
    unsigned char data[] = {65,66,67};
    write(sockfd,data,sizeof data);
    close(sockfd);
}

