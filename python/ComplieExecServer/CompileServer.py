import ServerEngine
import os

def run(selfobject, conn, addr):
	fex = selfobject.recv(conn, False)
	fname = selfobject.recv(conn, False)
	selfobject.recv_file(fname + fex, conn)
	f = fname + fex
	if fex == ".py":
		os.system("python3 " + f)

tcpServerCCE = ServerEngine.NewServer("172.18.0.2", 4562, run, "CompileServerKeys/")
tcpServerCCE.Get().HMaxthread = False
tcpServerCCE.start_server()


