#include "WProgram.h"
#ifndef MARVIN_H
#define MARVIN_H

#define SWITCH_COUNT 8
#define BUZZER_COUNT 2
#define SERVO_COUNT 2
#define MOTOR_COUNT 2

#define MOTOR_FORWARD 1
#define MOTOR_BACKWARD 2

typedef struct{
    uint8_t servos[SERVO_COUNT]; 
    uint8_t motors[MOTOR_COUNT]; 
    uint8_t switches[SWITCH_COUNT];
    uint8_t buzzers[BUZZER_COUNT];
    //14

    uint8_t digitals_active[4];
    uint8_t digitals[4];
    uint8_t analogs_active;
    uint16_t analogs[4];
    //13
} marvin_state_t;
//27

typedef struct {
    int8_t speed;
    int8_t direction;
    uint32_t expired_time;
    boolean executed;
} marvin_motor_command_t;
//7

typedef struct {
    uint8_t frequency;
    uint32_t expired_time;
} marvin_buzzer_command_t;
//5

typedef struct {
    uint8_t target;
    uint32_t switch_off_time;
} marvin_servo_command_t;
//5

typedef struct {
    uint8_t switches[SWITCH_COUNT];
    uint8_t servo_switch;
    marvin_buzzer_command_t buzzers[BUZZER_COUNT];
    marvin_servo_command_t servos[SERVO_COUNT];
    marvin_motor_command_t motors[MOTOR_COUNT];
} marvin_commands_t;
//8 + 1 + 5x2 +  5x2 + 7x2
// 43

extern marvin_state_t marvin_state;
extern marvin_commands_t marvin_commands;
extern uint32_t loop_time;
extern uint32_t loop_count;

void motoric_init();
void act();

void sensoric_init();
void sense();

void communication_init();
void communicate();

#endif
