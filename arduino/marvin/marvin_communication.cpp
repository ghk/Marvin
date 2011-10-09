#include "WProgram.h"
#include "marvin.h"

#define MESSAGE_HEADER_1 89
#define MESSAGE_HEADER_2 25
#define MESSAGE_MAX_LENGTH 128
#define MESSAGE_TIMEOUT 6000

#define STATE_DISCONNECTED 0
#define STATE_WAIT_HEADER 1
#define STATE_WAIT_MESSAGE 2

#define ERR_SUCCESS 0

#define ERR_PRO_NON_HEADER 1
#define ERR_PRO_TIMED_OUT 2
#define ERR_PRO_LENGTH_EXCEEDED 3

#define ERR_REQ_NOT_SUPPORTED 17
#define ERR_REQ_LENGTH_INVALID 18

#define ERR_CMD_INVALID 33
#define ERR_CMD_NOT_SUPPORTED 34
#define ERR_CMD_LENGTH_INVALID 35
#define ERR_CMD_TARGET_INVALID 36
#define ERR_CMD_ARG_INVALID 37

#define TYPE_PING 0
#define TYPE_CONFIGURE 1
#define TYPE_FETCH 2
#define TYPE_COMMAND 3

#define COMMAND_TYPE_SWITCH 1
#define COMMAND_TYPE_BUZZER 2
#define COMMAND_TYPE_MOTOR 3
#define COMMAND_TYPE_SERVO 4

static uint8_t timeout=0;
static uint8_t state=0;

static uint8_t message_length = -1;
static uint8_t message_type = -1;
static uint32_t message_timeout = 0;


/*
   Serial helpers
*/

static void serial_write_int(uint16_t i){
    Serial.write((uint8_t) (i >> 8));
    Serial.write((uint8_t) i);
}

static void serial_write_long(uint32_t l){
    Serial.write((uint8_t) (l >> 24));
    Serial.write((uint8_t) (l >> 16));
    Serial.write((uint8_t) (l >> 8));
    Serial.write((uint8_t) l);
}

static void serial_discard_bytes(uint8_t length){
    for(uint8_t i = 0; i < length; i++)
        Serial.read();
}

/*
   Serial helpers
*/

static void reset(){
    state = STATE_WAIT_HEADER;
    message_length = -1;
    message_type = -1;
}

static void send_error(uint8_t errorCode, uint8_t errorData){
    Serial.write(errorCode);
    Serial.write(errorData);
}

static void protocol_error(uint8_t errorCode, uint8_t errorData){
    reset();
    Serial.flush();
    send_error(errorCode, errorData);
}

static void request_error(uint8_t errorCode, uint8_t errorData, uint8_t remainingMessageLength){
    reset();
    serial_discard_bytes(remainingMessageLength);
    send_error(errorCode, errorData);
}

/*
   process_command_* = process one command
   processor must eat all commandLength byte
*/

static uint8_t process_command_switch(uint8_t commandLength){
    if(commandLength != 2){
        serial_discard_bytes(commandLength);
        return ERR_CMD_LENGTH_INVALID;
    }
    uint8_t index = Serial.read();
    uint8_t value = Serial.read();
    if(index >= SWITCH_COUNT){
        return ERR_CMD_TARGET_INVALID;
    }
    marvin_commands.switches[index] = value;
    return ERR_SUCCESS;
}

static uint8_t process_command_motor(uint8_t commandLength){
    if(commandLength != 5){
        serial_discard_bytes(commandLength);
        return ERR_CMD_LENGTH_INVALID;
    }
    uint8_t index = Serial.read();
    uint8_t direction = Serial.read();
    uint8_t speed = Serial.read();
    uint32_t time = 0;
    time |= Serial.read();
    time <<= 8;
    time |= Serial.read();
    if(index >= MOTOR_COUNT){
        return ERR_CMD_TARGET_INVALID;
    }
    if(direction != MOTOR_FORWARD && direction != MOTOR_BACKWARD){
        return ERR_CMD_ARG_INVALID;
    }
    marvin_motor_command_t* cmd = &(marvin_commands.motors[index]);
    (*cmd).speed = speed;
    (*cmd).direction = direction;
    (*cmd).expired_time = loop_time + time;
    (*cmd).executed = false;
    return ERR_SUCCESS;
}


static uint8_t process_command_servo(uint8_t commandLength){
    if(commandLength != 2){
        serial_discard_bytes(commandLength);
        return ERR_CMD_LENGTH_INVALID;
    }
    uint8_t index = Serial.read();
    uint8_t value = Serial.read();
    if(index >= SERVO_COUNT){ return ERR_CMD_TARGET_INVALID; }
    if(value > 180){
        return ERR_CMD_ARG_INVALID;
    }
    marvin_commands.servos[index].target = value;
    return ERR_SUCCESS;
}


static uint8_t process_command_not_supported(uint8_t commandLength){
    serial_discard_bytes(commandLength);
    return ERR_CMD_NOT_SUPPORTED;
}


/*
   process_* = process request message
   request_message is message_length all is already available in Serial buffer
   processor must eat all those bytes
*/

static void process_ping(){
  if(message_length != 0){
    request_error(ERR_REQ_LENGTH_INVALID, message_length, message_length);
    return;
  }
  Serial.write((uint8_t) ERR_SUCCESS);
  Serial.write((uint8_t) MESSAGE_HEADER_1);
  Serial.write((uint8_t) MESSAGE_HEADER_2);
}

static void process_configure(){
  if(message_length != 0){
    request_error(ERR_REQ_LENGTH_INVALID, message_length, message_length);
    return;
  }
  Serial.write((uint8_t) ERR_SUCCESS);
}

static void process_fetch(){
    if(message_length != 6){
      request_error(ERR_REQ_LENGTH_INVALID, message_length, message_length);
      return;
    }
    Serial.write((uint8_t) ERR_SUCCESS);

    //loop states
    serial_write_long(loop_count);
    serial_write_long(loop_time);

    //sensors

    //  digital
    for(uint8_t i = 0; i < 4; i++){
        Serial.write(marvin_state.digitals_active[i]);
    }
    for(uint8_t i = 0; i < 4; i++){
        uint8_t digital_flag = Serial.read();
        uint8_t result = digital_flag;
        result &= marvin_state.digitals_active[i];
        result &= marvin_state.digitals[i];
        Serial.write(result);
    }

    //  analog
    Serial.read();
    Serial.read();
    Serial.write((uint8_t)0);

    //output state

    //  switches
    for(uint8_t i = 0; i < SWITCH_COUNT; i++)
        Serial.write(marvin_state.switches[i]);
    //  buzzers
    for(uint8_t i = 0; i < BUZZER_COUNT; i++)
        Serial.write(marvin_state.buzzers[i]);
    //  motors
    for(uint8_t i = 0; i < MOTOR_COUNT; i++)
        Serial.write(marvin_state.motors[i]);
    //  servos
    for(uint8_t i = 0; i < SERVO_COUNT; i++)
        Serial.write(marvin_state.servos[i]);
}

static void process_command(){
    if(message_length < 1){
      request_error(ERR_REQ_LENGTH_INVALID, message_length, message_length);
      return;
    }
    uint8_t commandsRemaining = Serial.read();
    uint8_t bytesRemaining = message_length - 1; // has read one for command length

    Serial.write((uint8_t) ERR_SUCCESS);
    Serial.write(commandsRemaining);
    while(commandsRemaining > 0){
        commandsRemaining--;
        if(bytesRemaining < 0){
            Serial.write((uint8_t) ERR_CMD_INVALID);
            continue;
        }
        uint8_t commandType = Serial.read();
        bytesRemaining--;

        if(bytesRemaining < 0){
            Serial.write((uint8_t) ERR_CMD_INVALID);
            continue;
        }
        uint8_t commandLength = Serial.read();
        bytesRemaining--;

        if(commandLength > bytesRemaining)
            commandLength = bytesRemaining;

        uint8_t commandResult = 0;
        switch(commandType){
        case COMMAND_TYPE_SWITCH:
            commandResult = process_command_switch(commandLength);
            break;
        case COMMAND_TYPE_BUZZER:
            commandResult = process_command_not_supported(commandLength);
            break;
        case COMMAND_TYPE_MOTOR:
            commandResult = process_command_motor(commandLength);
            break;
        case COMMAND_TYPE_SERVO:
            commandResult = process_command_servo(commandLength);
            break;
        default:
            commandResult = process_command_not_supported(commandLength);
        }
        bytesRemaining -= commandLength;
        Serial.write(commandResult);
    }

    if(bytesRemaining > 0)
        serial_discard_bytes(bytesRemaining);
}

/*
   read_* = read request parts
   header -> message -> header -> message ...
   must change state after finishing
*/

static void read_header(){
    //4 is 2 byte header + 1 byte length + 1 byte type
    if(Serial.available() >= 4){
        int read;

        read = Serial.read();
        if(read != MESSAGE_HEADER_1){
          protocol_error(ERR_PRO_NON_HEADER, read);
          return;
        }

        read = Serial.read();
        if(read != MESSAGE_HEADER_2){
          protocol_error(ERR_PRO_NON_HEADER, read);
          return;
        }

        message_type = Serial.read();

        message_length = Serial.read();
        if(message_length > MESSAGE_MAX_LENGTH){
          protocol_error(ERR_PRO_LENGTH_EXCEEDED, message_length);
          return;
        }

        state = STATE_WAIT_MESSAGE;
        message_timeout = loop_time + MESSAGE_TIMEOUT;
    }
}

static void read_message(){
    if(Serial.available() < message_length){
        if(loop_time > message_timeout){
          protocol_error(ERR_PRO_TIMED_OUT, message_length - Serial.available());
        }
        return;
    }
    switch(message_type){
    case TYPE_PING:
        process_ping();
        break;
    case TYPE_CONFIGURE:
        request_error(ERR_REQ_NOT_SUPPORTED, message_type, message_length);
        break;
    case TYPE_FETCH:
        process_fetch();
        break;
    case TYPE_COMMAND:
        process_command();
        break;
    default:
        request_error(ERR_REQ_NOT_SUPPORTED, message_type, message_length);
        break;
    }
    reset();
}

void communication_init(){
  Serial.begin(9600);
  state = STATE_WAIT_HEADER;
}

void communicate()
{
  switch(state)
  {
  case STATE_DISCONNECTED:
    digitalWrite(13,LOW);
    break;
  case STATE_WAIT_HEADER:
    digitalWrite(13,HIGH);
    read_header();
    break;
  case STATE_WAIT_MESSAGE:
    digitalWrite(13,HIGH);
    read_message();
    break;
  }
}
