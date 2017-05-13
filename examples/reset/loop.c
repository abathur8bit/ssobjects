#include <stdio.h>
#include <sys/time.h>
#include <unistd.h>

//make sure that we are getting a true timer value
void testtime()
{
    struct timeval tv;

    if(gettimeofday(&tv,NULL)!=0)
        perror("gettimeofday failed");
   
   unsigned long ms1 = (long)tv.tv_sec*1000+(long)tv.tv_usec/1000;

    printf("current %lu\n",ms1);

    usleep(2000*1000); //sleep for 2 seconds

    if(gettimeofday(&tv,NULL)!=0)
        perror("gettimeofday failed");
   
   unsigned long ms2 = (long)tv.tv_sec*1000+(long)tv.tv_usec/1000;

    printf("current %lu\n",ms2);
    printf("Difference %lu\n",ms2-ms1);//should be 2000ish
}

void looptest()
{
   int a=10;
   int b=20;
   int c=a/b;
}

unsigned long millis()
{
    struct timeval tv;

    if(gettimeofday(&tv,NULL)!=0)
        perror("gettimeofday failed");
   
   unsigned long ms1 = (long)tv.tv_sec*1000+(long)tv.tv_usec/1000;
   return ms1;
}

int main(int arc, char* argv[])
{
    testtime();
    unsigned long timer=millis();
    unsigned long count=0;
    unsigned long now=0;

    //just sit in a busy loop, counting how many times we can get through
    //it in a second.
    while(1)
    {
        count++;
        looptest();
        now = millis();
        if((long)(now-timer)>=0)
        {
            timer = now+1000;
            printf("loops: %lu\n",count);
            count=0;
        }
    }
}
