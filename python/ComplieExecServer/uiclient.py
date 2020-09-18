from PyQt5.QtWidgets import *
from PyQt5 import *
from PyQt5.QtCore import Qt
import threading
import sys
from queue import Queue

#make this because there is no default way to stop a thread in python besides deamon
class SocketThread(threading.Thread):
	def __init__(self, *args, **kwargs):
		super(SocketThread, self).__init__(*args, **kwargs)
		self._stop = threading.Event()

	def stop(self):
		self._stop.set()


class CLAppWin(QMainWindow):
	def __init__(self, clientsocketTF, isusersys, ctt):
		super().__init__()

		self.comm = Queue()
		self.clientsocketTF = clientsocketTF
		self.CCSocketThread = None
		self.isusersys = isusersys
		self.lock = threading.Lock()
		self.lock.acquire()
		self.threadlock = threading.Lock()
		self.ctt = ctt
		self.texchangec = 0

		self.setWindowTitle("GUI Client - ServerEnigne Systems")
		self.width = 640
		self.height = 480
		self.setFixedWidth(self.width)
		self.setFixedHeight(self.height)

		self.TitleFont = QtGui.QFont()
		self.TitleFont.setBold(True)


		self.ConnectLabel = QLabel(self)
		self.ConnectLabel.setText("Host and Port: ")
		self.ConnectLabel.move(10, 20)
		self.ConnectLabel.setFont(self.TitleFont)

		self.hostIN = QLineEdit(self)
		self.hostIN.move(10, 50)
		self.hostIN.resize(200, 32)

		self.portIN = QLineEdit(self)
		self.portIN.move(10, 100)
		self.portIN.resize(200, 32)

		self.ConnectButton = QPushButton("Connect", self)
		self.ConnectButton.clicked.connect(self.SSocketThread)
		self.ConnectButton.resize(200, 32)
		self.ConnectButton.move(10, 150)
		self.ConnectButton.setStyleSheet("QPushButton""{""background-color : lightgreen;""}")

		self.DisconnectButton = QPushButton("Disconnect", self)
		self.DisconnectButton.clicked.connect(self.DFSocketThread)
		self.DisconnectButton.resize(200, 32)
		self.DisconnectButton.move(10, 190)
		self.DisconnectButton.setStyleSheet("QPushButton""{""background-color : lightblue;""}")

		self.SocketRecvOutB = QListWidget(self)
		self.SocketRecvOutB.move(245, 5)
		self.SocketRecvOutB.resize(390, 320)
		#self.SocketRecvOutB.setStyleSheet("border: 1px solid black;")
		#self.SocketRecvOutB.setAlignment(Qt.AlignLeft)
		self.SocketRecvOutB.addItem(QListWidgetItem("ClientServer Connection Output"))
		self.SocketRecvOutB.addItem(QListWidgetItem(""))

		self.scrollbar = QScrollBar(self)
		#self.scrollbar.setStyleSheet("background: lightgreen")
		self.SocketRecvOutB.addScrollBarWidget(self.scrollbar, Qt.AlignLeft)
		
		self.SendServerLabel = QLabel(self)
		self.SendServerLabel.setText("Send to Server: ")
		self.SendServerLabel.move(390, 340)
		self.SendServerLabel.setFont(self.TitleFont)

		self.SendServerIN = QLineEdit(self)
		self.SendServerIN.move(340, 370)
		self.SendServerIN.resize(200, 32)

		self.SendServerButton = QPushButton("->", self)
		self.SendServerButton.clicked.connect(self.SendDTS)
		self.SendServerButton.resize(32, 34)
		self.SendServerButton.move(540, 369)


		self.show()	

	def Get(self):
		return self

	def DFSocketThread(self):
		if self.CCSocketThread != None:
			if self.CCSocketThread.is_alive():
				self.SocketRecvOutB.addItem(QListWidgetItem("Disconnected from server"))
				self.CCSocketThread.stop()
				self.CCSocketThread = None

			else:
				self.SocketRecvOutB.addItem(QListWidgetItem("Can't Disconnected from a server that isn't connected to"))
		else:
			self.SocketRecvOutB.addItem(QListWidgetItem("Can't Disconnected from a server that isn't connected to"))

	def SendDTS(self):
		if self.CCSocketThread != None:
			if self.CCSocketThread.is_alive():
				self.comm.put(self.SendServerIN.text())
				self.lock.release()

			else:
				self.SocketRecvOutB.addItem(QListWidgetItem("Please connect to a server to send data"))

		else:
			self.SocketRecvOutB.addItem(QListWidgetItem("Please connect to a server to send data"))

		with self.threadlock:
			self.lock.acquire()
			self.GetResultFromSocketThread()

	def SSocketThread(self):
		host = self.hostIN.text()
		port = int(self.portIN.text())
		if self.CCSocketThread != None:
			if self.CCSocketThread.is_alive():
				#for previous thread to stop
				self.CCSocketThread.stop()
				self.StartNSocketThread(host, port)
				self.GetResultFromSocketThread()
				pass

			else:
				self.StartNSocketThread(host, port)
				self.GetResultFromSocketThread()
				pass

		else:
			self.StartNSocketThread(host, port)
			self.GetResultFromSocketThread()

	def StartNSocketThread(self, host, port):
		self.resetlocks()
		self.texchangec = 0
		self.CCSocketThread = SocketThread(target= self.clientsocketTF, args=(host, port, self.comm, self.isusersys, self.lock, self.threadlock, self.ctt, ))
		self.CCSocketThread.daemon = True
		self.CCSocketThread.start()

	def GetResultFromSocketThread(self):
		RD = self.comm.get()
		self.comm.task_done()
		self.texchangec += 1
		for x in range(len(RD)):
			self.SocketRecvOutB.addItem(QListWidgetItem(RD[x]))

	def resetlocks(self):
		self.lock = threading.Lock()
		self.lock.acquire()
		self.threadlock = threading.Lock()

#get
#send

class CSTT:
	def __init__(self):
		pass

	def Get(self):
		return self

	def GSD(self, process, lock, thislock, in_queue, args):
		data = None
		with lock:
			data = in_queue.get()
			SD = process(data, args)
			in_queue.put(SD)

		thislock.release()

		while lock.locked() == False:
			pass

		thislock.acquire()

		return data

#socket with while loop
def SocketClientThread(host, port, in_queue, isusersys, lock, thislock, ctt):
	
	#always first segment of code
	thislock.acquire()
	isrunning = True
	username = None
	password = None
	#always first segment of code

	#connect to server

	#connect to server

	if isusersys:
		#result to send back to server you always need a result
		SD = []
		SD.append("server> You have connected to server")
		SD.append("server> Please Input your username")
		in_queue.put(SD)

		#get username and password from main thread

		def GUProcess(data, args):
			SMSG = []
			SMSG.append("server> Successfully registerd user: " + str(data))
			SMSG.append("server> Please Input your password")
			return SMSG

		username = ctt.GSD(GUProcess, lock, thislock, in_queue, {})

		#get password and send username and password
		def GPASUPProcess(data, args):
			#send data to server

			SMSG = []
			SMSG.append("server> Successfully registerd password: " + str(data))
			return SMSG

		password = ctt.GSD(GPASUPProcess, lock, thislock, in_queue, {})


		pass

	else:
		SD = []
		SD.append("server> You have connected to server")
		in_queue.put(SD)

	def lP(data, username):
		SD = []
		SD.append(str(username) + "> " + str(data)) 
		return SD

	while isrunning:
		ctt.GSD(lP, lock, thislock, in_queue, username)
		pass




def ApplicationMThread():
	app = QApplication([])

	clientsockettools = CSTT()

	appwin = CLAppWin(SocketClientThread, True, clientsockettools)

	app.exec_()

	sys.exit()




ApplicationMThread()