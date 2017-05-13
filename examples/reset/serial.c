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

int main(int argc,char* argv[])
{
//    char portname[255] = "/dev/tty.usbserial-A600e4IJ";
    char portname[255] = "/dev/cu.usbserial-A600e4IJ";
    printf("Opening serial port [%s]\n",portname);
    int fd = openPort(portname);

    printf("About to write data\n");
    char* data = "123";
    int n = write(fd,data,strlen(data));
    if(n<0)
        printf("Error writing to port\n");
    close(fd);
    printf("Done\n");
}

