#UDP server program
import sys,argparse
import MySQLdb as mdb
from socket import *
from threading import Thread
from time import sleep

def sender(db,serverSocket,dbName):
	print "waiting for messages"
	while True:
		sleep(1)
		db.execute('SELECT C.current_ip,M.content,M.from_user_id,M.id FROM messages M,users C WHERE M.Sent=\''+str(0)+'\' AND C.id=M.to_user_id;')
		rows = db.fetchall()
		if rows!=():
			for row in rows:
				IP = row[0].strip()
				message = row[1]
				uid = row[2]
				mid = row[3]
				print IP
				if IP!="web" and IP!="offline":
					serverSocket.sendto("msg "+str(uid)+" "+message,(IP,50000))
				db.execute('UPDATE messages SET Sent=\''+str(1)+'\' WHERE id='+str(mid)+';')
				print "Sent newly arrived messages"

parser = argparse.ArgumentParser()
parser.add_argument("-a","--hostadd", help="Enter the host address ",default="0.0.0.0")


args = parser.parse_args()
serverAddr =  args.hostadd 
serverPort =  50001 

try:
	serverSocket = socket(AF_INET, SOCK_DGRAM)
	serverSocket.setsockopt(SOL_SOCKET, SO_REUSEADDR, 1)
except Exception as e:
	print "Could not create the socket ",e
	sys.exit(0)
try:
	serverSocket.bind((serverAddr, serverPort))
except:
	print "Could not bind the socket to the given address and port"

print "Server has started...."

data = ""
try:
	f = open("CONFIG",'r')
	data = f.readline().split("\n")[0].split(" ")
	f.close()
except Exception as e:
	print "Unable to open the file"+e
	sys.exit(0)

hostname = data[0]
username = data[1]
password = data[2]
dbName = data[3]

try:
	con = mdb.connect(hostname,username,password)
except:
	print "Invalid Credentials"
	sys.exit(0)
try:
	db = con.cursor()
	db.execute('USE '+dbName)
	con.autocommit(True)
except Exception as e:
	print "Could not access the database"+str(e)
	sys.exit(0)

print "Starting sender"
sender(db,serverSocket,dbName)
