import ServerEngine
import os

def compileR(f, lang):
	os.system("cp " + f + " rcode/" + lang + "/" + f)
	os.system("rm " + f)
	os.system("python3 rcode/" + lang + "/" + f)
	os.system("rm rcode/" + lang + "/" + f)

def run(selfobject, conn, addr):
	fex = selfobject.recv(conn, False)
	fname = selfobject.recv(conn, False)
	selfobject.recv_file(fname + fex, conn)
	f = fname + fex
	if fex == ".py":
		compileR(f, "python")
	if fex == ".cpp":
		compileR(f, "c++")
	print("")


def prethread_bootup(selfobject):
	print("<prethread_bootup init sequence>")

keycontainer = ServerEngine.KeyPairContainer()
tcpServerCCE = ServerEngine.NewServer("172.18.0.2", 4562, run, keycontainer.RunKeyPair("CCE"), True)
tcpServerCCE.Get().HMaxthread = False
tcpServerCCE.Get().HPrethread_bootup = True
tcpServerCCE.Get().Prethread_bootupfunc = prethread_bootup
tcpServerCCE.start_server()
