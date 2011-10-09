import serial
import threading
import time


ser = None
fetch_count = 0
read_count = 0

loop_count = 0

read_thread_alive = False
read_thread = None

fetch_count_thread = None
fetch_count_thread_alive = False


class ReadThread ( threading.Thread ):
   stop = False
   def run ( self ):
       global read_thread_alive
       global read_count
       global loop_count
       read_thread_alive = True
       lc = 0
       try:
           while(not self.stop):
               read = ser.read(size=1)
               if(len(read) == 1):
                   print ord(read[0])
                   """
                   if(read_count == 1):
                       lc = ord(read[0])
                   elif(read_count == 2):
                       lc = (256 * lc) + ord(read[0])
                   elif(read_count == 3):
                       lc = (256 * lc) + ord(read[0])
                   elif(read_count == 4):
                       lc = (256 * lc) + ord(read[0])
                       loop_count = lc
                   """
                   read_count = read_count + 1
               elif(len(read) > 1):
                   print read 
       finally:
           print "exiting read thread"
           read_thread_alive = False

class FetchCountThread ( threading.Thread ):
   stop = False
   def run ( self ):
       global fetch_count_thread_alive
       global fetch_count
       fetch_count_thread_alive = True
       try:
           while(not self.stop):
               #print "%d - %d - %d" % (fetch_count, read_count, loop_count)
               fetch_count = 0
               time.sleep(1)
       finally:
           print "exiting fetch counting thread"
           fetch_count_thread_alive = False


def connect():
    global ser
    global read_thread
    global fetch_count_thread
    ser = serial.Serial('/dev/rfcomm0', 9600, timeout=1)
    if(ser.isOpen()):
        if(read_thread_alive):
            read_thread.stop = True
        if(fetch_count_thread_alive):
            fetch_count_thread.stop = True
        read_thread = ReadThread()
        read_thread.start()
        fetch_count_thread = FetchCountThread()
        fetch_count_thread.start()
        print "init success"
    else:
        print "init failed"

def disconnect():
    read_thread.stop = True
    fetch_count_thread.stop = True
    ser.close()

def ping():
    ser.write(bytearray([89,25,0,0]))

def turnleft(value, time1, time2):
    ser.write(bytearray([89,25,3,15,2,3,5, 0, 1,value,time1, time2, 3, 5, 1, 2,
        value, time1, time2]))
def forward(value, time):
    ser.write(bytearray([89,25,3,13,2,3,4, 0, 1,value,time, 3, 4, 1, 1, value, time]))
def motors(left, right, time1, time2):
    ser.write(bytearray([89,25,3,15,2,3,5, 0, 1,left,time1, time2, 3, 5, 1, 1,right, time1, time2]))

def backward(value, time):
    ser.write(bytearray([89,25,3,13,2,3,4, 0, 2,value,time, 3, 4, 1, 2, value, time]))

def motor(index, direction, value, time):
    ser.write(bytearray([89,25,3,7,1,3,4,index, direction,value,time]))

def servo(index, value):
    ser.write(bytearray([89,25,3,5,1,4,2,index,value]))

def servos(value1, value2):
    ser.write(bytearray([89,25,3,9, 2, 4,2,0,value1, 4,2,1,value2]))

def servo_switch(value):
    switch(1, value)

def switch(index, value):
    ser.write(bytearray([89,25,3,5,1,1,2,index,value]))

def invalid_header():
    ser.write(bytearray([89,26,0,0]))

def not_supported():
    ser.write(bytearray([89,25,5,0]))

def timed_out():
    ser.write(bytearray([89,25,2,8]))

def fetch():
    ser.write(bytearray([89,25,2,6,234,214,221,234,0,0]))

def fetch_loop():
    global read_count
    global fetch_count
    while(True):
        fetch_count = fetch_count + 1
        fetch()
        next = False
        while(not next):
            if(read_count >= 20):
                read_count = 0
                next = True





