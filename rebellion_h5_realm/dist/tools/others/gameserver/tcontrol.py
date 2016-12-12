#!/usr/bin/python

import sys
import getopt
import telnetlib

HOST = "localhost"
PORT = "11133"
tn = telnetlib.Telnet(HOST, PORT)
tn.read_until("Password:")
tn.write("123456\n")
tn.read_until("Type 'help' to see all available commands.")

def usage():
    print 'tcontrol.py -c <status>'
    print 'tcontrol.py -c <restart> -t <time>'
    print 'tcontrol.py -c <abort>'

def main(argv):
   command = ''
   time = ''
   rcmd = ''
   try:
      opts, args = getopt.getopt(argv,"hc:t:",["command=","time="])
   except getopt.GetoptError:
       usage()
       sys.exit(2)
   for opt, arg in opts:
      if (opt == '-h'):
         print 'tcontrol.py -c <status>'
         print 'tcontrol.py -c <restart> -t <time>'
         print 'tcontrol.py -c <abort>'
         sys.exit()
      elif (opt == ''):
         print 'empty 2'
      elif opt in ("-c", "--command"):
         command = arg
      elif opt in ("-t", "--time"):
         time = arg

   if command == "status":
      tn.write("status\n")
      tn.write("exit\n")
      print tn.read_all()
   if (command == "shutdown" and time == ""):
      print 'Cannot shutdown server without specifying time (-t xxx)'
   elif (command == "shutdown" and time != ""):
      print 'Shutting down server in:', time + ' seconds'
      rcmd = 'shutdown ' + time
      tn.write(rcmd)
      tn.write("\n")
      tn.write("exit\n")
      print tn.read_all()
   if (command == "restart" and time == ""):
      print 'Cannot restart server without specifying time (-t xxx)'
   elif (command == "restart" and time != ""):
      print 'Restarting server in:', time + ' seconds'
      rcmd = 'restart ' + time
      tn.write(rcmd)
      tn.write("\n")
      tn.write("exit\n")
      print tn.read_all()
   if (command == "abort"):
      print 'Aborting restart'
      tn.write("abort\n")
      tn.write("exit\n")
      print tn.read_all()

if __name__ == "__main__":
   main(sys.argv[1:])