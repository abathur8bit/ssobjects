n=1
10 print "N=",n
if n=1 then gosub 20
if n=-1 then gosub 30
nap 500
n=n*-1
goto 10
20 toggleLed 13,0
return
30 toggleLen 13,1
return

n=0
10 print "n=",n
nap 500
n=n+1
goto 10

// use with ArduinoBasic running on ORE (omni robot experiment - the robot)

unsigned long timer = 0;
int state=0;

struct PACKET_HEADER
{
    unsigned short cookie;
    unsigned char  deviceId;
    unsigned char  command;
    unsigned short length;  //length of data after header, 0 if none
};

struct PACKET
{
    struct PACKET_HEADER header;
    unsigned char* buffer;
};

struct PACKET_DRIVE
{
    struct PACKET_HEADER header;
    unsigned char motor1;
    unsigned char motor2;
    unsigned char motor3;
};

void setup()
{
  pinMode(13,OUTPUT);
  Serial.begin(9600);
  Serial2.begin(9600);
  Serial.print("MORE (My Omni Robot Experiment) Ready\n");
  Serial.print("Send packets as a sequence of numbers.\n");
}

