#include "marvin.h"

static uint8_t timeout=0;

void setup() {
    sensoric_init();
    motoric_init();
    communication_init();
}

void loop()
{
    loop_count++;
    loop_time = millis();
    sense();
    communicate();
    act();
}



