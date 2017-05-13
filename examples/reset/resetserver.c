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


typedef struct sockaddr        SOCKADDR;

typedef struct sockaddr_in     SOCKADDR_IN;
typedef struct sockaddr_in*    LPSOCKADDR_IN;

int main(int argc,char* argv[])
{
    int socket;
    int port=9999;
    char host[255] = "localhost";
    printf("host:port %s:%d\n",host,port);
}
