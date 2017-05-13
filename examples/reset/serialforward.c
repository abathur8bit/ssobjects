#define DUMP

#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include <fcntl.h>   /* File control definitions */
#include <errno.h>   /* Error number definitions */
#include <termios.h> /* POSIX terminal control definitions */

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

int main(int argc, char* argv[])
{
    int serialin=0,serialout=0,i,n;
    char serialInPortName[255] = "";
    char serialOutPortName[255] = "";
    int dump = 0;

    for(i=0; i<argc; i++)
    {
        if(strcmp(argv[i],"--in")==0)
            strcpy(serialInPortName,argv[++i]);
        else if(strcmp(argv[i],"--out")==0)
            strcpy(serialOutPortName,argv[++i]);
        else if(strcmp(argv[i],"--dump")==0)
            dump=1;
    }


    if(strlen(serialInPortName))
    {
        printf("Connecting for input  on serial port %s\n",serialInPortName);
        if(-1==(serialin = openPort(serialInPortName))) err_quit("Serial input port error");
    }   
    if(strlen(serialOutPortName))
    {
        printf("Connecting for output on serial port %s\n",serialOutPortName);
        if(-1==(serialout = openPort(serialOutPortName))) err_quit("Serial output port error");
    }

    char buff[1024];
    while((n=read(serialin,buff,sizeof buff)) > 0)
    {
        for(i=0; i<n; i++)
        {
            if(dump)
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
            else
            {
                printf("%c",buff[i]);
            }
            if(serialout)
                write(serialout,buff,n);
        }
    }
}

