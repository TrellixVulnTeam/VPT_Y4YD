
def SendBEmsg(msg):
	import socket
	import ssl

	HOST, PORT ='172.18.0.2', 7327

	# CREATE SOCKET
	sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
	sock.settimeout(10)

	# WRAP SOCKET
	wrappedSocket = ssl.wrap_socket(sock, ssl_version=ssl.PROTOCOL_TLSv1)

	# CONNECT AND PRINT REPLY
	wrappedSocket.connect((HOST, PORT))
	print(wrappedSocket.recv(1280))

	# CLOSE SOCKET CONNECTION
	wrappedSocket.close()
	pass
