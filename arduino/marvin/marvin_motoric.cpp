#include "WProgram.h"
#include "marvin.h"
#include <Servo.h>

static int8_t switch_pins[] = {3, 8, -1, -1, -1, -1, -1, -1};
static int8_t servo_pins[] = {9, 10};
static int8_t motor_dir_pins[] = {7,4};
static int8_t motor_speed_pins[] = {6,5};

static uint8_t servo_defaults[] = {170, 90};
static uint8_t servo_max[] = {180, 180};
static uint8_t servo_min[] = {80, 0};

static Servo servos[SERVO_COUNT];

static void act_switches(){
    for(int8_t i = 0; i < SWITCH_COUNT; i++){
        int8_t pin = switch_pins[i];
        if(pin != -1){
            digitalWrite(pin, marvin_commands.switches[i]);
            marvin_state.switches[i] = marvin_commands.switches[i];
        }
    }
}

static void init_switches(){
    for(int8_t i = 0; i < SWITCH_COUNT; i++){
        int8_t pin = switch_pins[i];
        if(pin != -1){
            pinMode(pin, OUTPUT);
        }
    }
}

static void init_servos(){
    marvin_commands.servo_switch = LOW;
    for(int8_t i = 0; i < SERVO_COUNT; i++){
        int8_t pin = servo_pins[i];
        if(pin != -1){
            servos[i].attach(pin);
            marvin_commands.servos[i].target = servo_defaults[i];
        }
    }
}


static void act_servos(){
    for(int8_t i = 0; i < SERVO_COUNT; i++){
        int8_t pin = servo_pins[i];
        if(pin != -1){
            marvin_servo_command_t* cmd = &(marvin_commands.servos[i]);
            uint8_t target = (*cmd).target;
            if(target != 181){
                if(target > servo_max[i]){
                    target = servo_max[i];
                }
                if(target < servo_min[i]){
                    Serial.write((uint8_t) 78);
                    target = servo_min[i];
                }
                servos[i].write(target);
                marvin_state.servos[i] = target;
            }
            (*cmd).target = 181;
        }
    }
}

static void act_motors(){
    for(int8_t i = 0; i < MOTOR_COUNT; i++){
        int8_t dir_pin = motor_dir_pins[i]; //4
        int8_t speed_pin = motor_speed_pins[i]; //5
        if(dir_pin != -1 && speed_pin != -1){
            marvin_motor_command_t* cmd = &(marvin_commands.motors[i]);
            if((*cmd).expired_time > loop_time && !(*cmd).executed){
                digitalWrite(speed_pin,HIGH); //5
                if((*cmd).direction == MOTOR_FORWARD){
                    digitalWrite(dir_pin,LOW); //4
                }
                else{
                    digitalWrite(dir_pin,HIGH); //4
                }
                analogWrite(speed_pin, (*cmd).speed); //5
                (*cmd).executed = true;
            }
            if((*cmd).expired_time <= loop_time){
                digitalWrite(dir_pin,LOW);
                digitalWrite(speed_pin,LOW);
                analogWrite(speed_pin, 0);
            }
        }
    }
}

static void init_motors(){
    for(int8_t i = 0; i < MOTOR_COUNT; i++){
        int8_t dir_pin = motor_dir_pins[i];
        int8_t speed_pin = motor_speed_pins[i];
        pinMode(dir_pin, OUTPUT);
        pinMode(speed_pin, OUTPUT);
        if(dir_pin != -1 && speed_pin != -1){
            marvin_commands.motors[i].expired_time = 0;
        }
    }
}

void motoric_init(){
    init_switches();
    init_servos();
    init_motors();
}


void act(){
    act_switches();
    act_servos();
    act_motors();
}

