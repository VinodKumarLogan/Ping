import MySQLdb as mdb
import sys,getpass

host = raw_input("Enter the host name : ")
username = raw_input("Enter the database username : ")
#print "Enter the password : ",
password = getpass.getpass()
try:
	con = mdb.connect(host,username,password)
except Exception as e:
	print "Invalid Credentials"+str(e)
	sys.exit(0)
dbName = raw_input("Enter the database name : ")
try:
	db = con.cursor()
	db.execute('CREATE DATABASE '+dbName+";")
	db.execute('USE '+dbName+';')
	db.execute('GRANT ALL PRIVILEGES ON *.* TO \''+username+'\'@\''+host+'\' IDENTIFIED BY \''+password+'\';')
	print "Databse created"
except Exception as e:
	print "Could not create the database "+str(e)
	sys.exit(0)
try:
	db.execute('CREATE TABLE users (username VARCHAR(40) NOT NULL , email VARCHAR(100), password VARCHAR(40) NOT NULL ,pwdmob VARCHAR(40) NOT NULL, mobilenum INTEGER,id INTEGER AUTO_INCREMENT, current_ip VARCHAR(20) NOT NULL, image VARCHAR(255),role ENUM("admin","user"),now VARCHAR(200),online INTEGER , PRIMARY KEY (id,username) );')
	db.execute('CREATE TABLE messages (content VARCHAR(160) ,timestamp TIMESTAMP NOT NULL,to_user_id INTEGER NOT NULL,from_user_id INTEGER NOT NULL,id INTEGER AUTO_INCREMENT, Sent VARCHAR(1) NOT NULL , PRIMARY KEY (id) );')

	print "Tables created"
except Exception as e:
	print "Could not create the tables "+str(e)
	sys.exit(0)
f = open("CONFIG",'w')
f.write(host+" "+username+" "+password+" "+dbName)
f.close()
print "Database created"