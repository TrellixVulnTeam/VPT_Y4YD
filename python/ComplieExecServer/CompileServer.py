import ServerEngine
import os

def run(selfobject, conn, addr):
	fex = selfobject.recv(conn, False)
	fname = selfobject.recv(conn, False)
	selfobject.recv_file(fname + fex, conn)
	f = fname + fex
	if fex == ".py":
		os.system("python3 " + f)

def prethread_bootup(selfobject):
	print("<prethread_bootup init sequence>")

tcpServerCCE = ServerEngine.NewServer("172.18.0.2", 4562, run, "CompileServerKeys/", True)
tcpServerCCE.Get().HMaxthread = False
tcpServerCCE.Get().HPrethread_bootup = True
tcpServerCCE.Get().Prethread_bottupfunc = prethread_bootup
tcpServerCCE.start_server()
