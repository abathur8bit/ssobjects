#include <stdio.h>

int main(int argc,char* argv[])
{
    int buttonStates = 0;
    buttonStates |= (1&1);
    buttonStates |= (digitalRead(SWITCH_YELLOW)&1)<<1;
    buttonStates |= (digitalRead(SWITCH_YELLOW)&1)<<2;
}

