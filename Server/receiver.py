#UDP server program
import sys,argparse
import MySQLdb as mdb
from socket import *
from threading import Thread

def receiver(db,serverSocket,dbName):
	while True:
		print "Waiting for clients"
		message, clientAddr = serverSocket.recvfrom(2048)
		print "Received data: ", message," from ",clientAddr
		if "register"==message.split(" ")[0]:
			msg = message.split(" ")
			name = msg[1]
			ip = clientAddr[0]
			password = msg[2]
			db.execute('SELECT * FROM users WHERE username=\''+name+'\';')
			rows = db.fetchall()
			if rows==():
				db.execute('INSERT INTO users (username,password,pwdmob,current_ip) VALUES(\''+name+'\',\''+" "+'\',\''+password+'\',\''+ip+'\');')
				db.execute('UPDATE users SET current_ip=\'' '\' WHERE username!=\''+name+'\' AND current_ip=\''+ip+'\';')
				serverSocket.sendto("Successfully Registered", clientAddr)
				print "Registered"
			else:
				print "Username Taken"
				serverSocket.sendto("Username Taken. Try a different Username.", clientAddr)
		elif message=="list clients":
			ip = clientAddr[0]
			db.execute("SELECT username,id FROM users WHERE current_ip!=\'" "\';")
			rows = db.fetchall()
			clientList = []
			for row in rows:
				temp = ''
				for ele in row:
					temp+=str(ele)+" "
				clientList.append(temp[:-1])
			print clientList
			serverSocket.sendto("clients "+str(clientList),clientAddr)
		elif "login"==message.split(" ")[0]:
			msg = message.split(" ")
			name = msg[1]
			password = msg[2]
			ip = clientAddr[0]
			db.execute('SELECT username FROM users WHERE username=\''+name+'\';')
			row = db.fetchall()
			if row!=():
				db.execute('SELECT username,pwdmob,id FROM users WHERE username=\''+name+'\' AND pwdmob=\''+password+'\';')
				rows = db.fetchall()
				if rows!=():
					db.execute('UPDATE users SET current_ip=\''+ip+'\' WHERE username=\''+name+'\';')
					db.execute('UPDATE users SET current_ip=\'' '\' WHERE username!=\''+name+'\' AND current_ip=\''+ip+'\';')
					serverSocket.sendto("Login Successful "+str(rows[0][2]),clientAddr)
					print str(rows[0][2])+" logged in"
				else:
					serverSocket.sendto("Invalid Password. Try Again or Register as a new user",clientAddr)
					print "log in failed"
			else:
				serverSocket.sendto("Invalid Username. Try Again or Register as a new user",clientAddr)
				print "log in failed"
		elif "msg"==message[:3]:
			msg = message.split(" ")
			fromUID = int(msg[1])
			toUID = int(msg[2])
			data = message[(4+len(msg[1])+1+len(msg[2])+1):]
			print fromUID,toUID,data
			db.execute('SELECT current_ip FROM users WHERE id=\''+msg[2]+'\';')
			rows = db.fetchall()
			temp = " "
			for row in rows:
				temp = row[0]
			if temp!="web" and temp!="offline":
				db.execute('INSERT INTO messages(from_user_id,to_user_id,content,Sent) VALUES(\''+str(fromUID)+'\',\''+str(toUID)+'\',\''+data+'\',\''+str(0)+'\');')
			else:
				db.execute('INSERT INTO messages(from_user_id,to_user_id,content,Sent) VALUES(\''+str(fromUID)+'\',\''+str(toUID)+'\',\''+data+'\',\''+str(1)+'\');')




parser = argparse.ArgumentParser()
parser.add_argument("-a","--hostadd", help="Enter the host address ",default="localhost")

#parsing the arguements
args = parser.parse_args()
serverAddr =  args.hostadd
serverPort =  50000 # holds the host port number

try:
	serverSocket = socket(AF_INET, SOCK_DGRAM)
	serverSocket.setsockopt(SOL_SOCKET, SO_REUSEADDR, 1)
except:
	print "Could not create the socket"
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

print "Starting receiver"
receiver(db,serverSocket,dbName)