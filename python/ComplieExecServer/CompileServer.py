import ServerEngine
import os

user_sys = ServerEngine.UserSys("CCE")

def compileR(f, lang):
	os.system("cp " + f + " rcode/" + lang + "/" + f)
	os.system("rm " + f)
	os.system("python3 rcode/" + lang + "/" + f)
	os.system("rm rcode/" + lang + "/" + f)

def run(selfobject, conn, addr):
	print("")
	global user_sys
	userd = user_sys.RegisterUser(selfobject, conn, addr, None)
	if userd != None:
		print(userd["username"] + " has succsefuly logged in")

	if userd == None:
		print(addr[0] + " hasn't logged in correctly")

	conn.close()
	print("")

def prethread_bootup(selfobject, conn, addr):
	pass

keycontainer = ServerEngine.KeyPairContainer()
tcpServerCCE = ServerEngine.NewServer("172.18.0.2", 4562, run, keycontainer.RunKeyPair("CCE"), True)
tcpServerCCE.Get().HMaxthread = False
tcpServerCCE.Get().HPrethread_bootup = True
tcpServerCCE.Get().Prethread_bootupfunc = prethread_bootup
tcpServerCCE.start_server()
