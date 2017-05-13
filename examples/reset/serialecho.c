//See http://www.easysw.com/~mike/serial/serial.html#2_5_2

//read from a socket, write to the serial port
//
#include <unistd.h>
#include <stdio.h>
#include <netinet/in.h>
#include <sys/time.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <stdlib.h>
#include <time.h>
#include <string.h>
#include <fcntl.h>   /* File control definitions */
#include <errno.h>   /* Error number definitions */
#include <termios.h> /* POSIX terminal control definitions */


#define SA struct sockaddr
#define MAXLINE     4096
#define DEF_PORT    3333

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

void mssleep(unsigned long millis)
{
    struct timespec tim, tim2;
    tim.tv_sec  = 0;
    tim.tv_nsec = millis;
    if(nanosleep(&tim,NULL) < 0)
    {
        perror("Nano sleep system call failed -");
    }
}

void sleepTest()
{
    struct timeval tp;
    if(gettimeofday(&tp,NULL)!=0)
        perror("gettimeofday failed");
    //1 second = 1000 millisecond
    //1 millisecond = 1000 microseconds
    //1 second = 1000000 microseconds
    long milliseconds = (long)tp.tv_sec*1000+(long)tp.tv_usec/1000;
    printf("time is %ldsec and %ldmicrosec = %ldmillisec\n",(long)tp.tv_sec,(long)tp.tv_usec,milliseconds);
    usleep(500*1000);
    if(gettimeofday(&tp,NULL)!=0)
        perror("gettimeofday failed");
    //1 second = 1000 millisecond
    //1 millisecond = 1000 microseconds
    //1 second = 1000000 microseconds
    long now = (long)tp.tv_sec*1000+(long)tp.tv_usec/1000;
    long diff = now - milliseconds;
    printf("slept for %ld milliseconds\n",diff);

    exit(0);
}

void usage()
{
    printf("Reads from a socket connection, and sends it to the serial port.\n");
    printf("Usage: serialecho [-p 2000] [-s /dev/cu.usbserial-A600e4IJ] [--delay 100] [--buffer 1]\n");
    printf(" -p        Socket port to listen on\n");
    printf(" -s        Serial port to send data to\n");
    printf(" --delay   How many milliseconds to wait before sending each byte over serial port\n"); 
    printf(" --buffer  How many bytes to read from the socket at a time\n");
    exit(0);
}

int main(int argc,char* argv[])
{
    int maxBuffer = 1;
    long delay = 50*1000;  //convert 100 milliseconds to microseconds
    int n;
    int i;
  int     listenfd,connfd;
  int     serialfd;
  struct  sockaddr_in servaddr;
  char*    buff = NULL;
  time_t  ticks;
  unsigned short wPortNum = DEF_PORT;
  char serialPortName[255] = "/dev/cu.usbserial-A600e4IJ";

    if(argc<2) usage();

    for(i=0; i<argc; i++)
    {
        if(!strcmp(argv[i],"-p"))
            wPortNum = atoi(argv[++i]);
        else if(!strcmp(argv[i],"-s"))
            strcpy(serialPortName,argv[++i]);
        else if(!strcmp(argv[i],"--delay"))
            delay = atoi(argv[++i])*1000;
        else if(!strcmp(argv[i],"--buffer"))
            maxBuffer = atoi(argv[++i]);
        else if(!strcmp(argv[i],"--help"))
            usage();
    }
    buff = (char*)malloc(maxBuffer);
    printf("Using delay of %ld milliseconds between full buffers, buffer size %d.\n",delay/1000,maxBuffer);
    printf("Connecting to serial port %s\n",serialPortName);
    if(-1==(serialfd = openPort(serialPortName))) err_quit("Serial port error");
    if(-1==(listenfd = socket(AF_INET,SOCK_STREAM,0))) err_quit("socket error");

    int optval;

    // set SO_REUSEADDR on a socket to true (1):
    optval = 1;
    setsockopt(listenfd, SOL_SOCKET, SO_REUSEADDR, &optval, sizeof optval);



    memset(&servaddr,0,sizeof(servaddr));
    servaddr.sin_family = AF_INET;
    servaddr.sin_addr.s_addr = htonl(INADDR_ANY);
    servaddr.sin_port = htons(wPortNum);

    if(-1==bind(listenfd,(SA*)&servaddr,sizeof(servaddr))) err_quit("bind error");
    if(-1==listen(listenfd,5)) err_quit("listen error");
        

  printf("server listening on port %u\n",wPortNum);
  for(;;)
  {
    connfd = accept(listenfd,(SA*)NULL,NULL);
    if(-1==connfd) err_quit("accept error");
    printf("Got a connection on socket %d waiting for data\n",connfd);

    while((n = read(connfd,buff,maxBuffer))>0)
    {
//        printf("Read %d bytes\n",n);
        for(i=0; i<n; i++)
        {
            printf("%02X | %03d",(unsigned char)buff[i],(unsigned char)buff[i],(char)buff[i]);
            if(iscntrl(buff[i]))
            {
                printf(" | [.]\n"); //masked
            }
            else
            {
               printf(" | [%c]\n",(char)buff[i]);
            }
        }
//        printf("Sending to serial port...");
        if(write(serialfd,buff,n) < 0) err_quit("Error writing to serial port");
//        printf("done sending %d bytes.\n",n);
        if(delay)
            usleep(delay);
    }
    close(connfd);
    puts("connection closed");
  }
  close(serialfd);
  free(buff);
}

