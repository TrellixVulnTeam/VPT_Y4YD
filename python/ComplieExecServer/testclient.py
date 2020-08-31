import socket
import ssl
HOST, PORT ='172.18.0.2', 4562

# CREATE SOCKET
sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
sock.settimeout(10)
# WRAP SOCKET
wrappedSocket = ssl.wrap_socket(sock, ssl_version=ssl.PROTOCOL_TLSv1)
wrappedSocket.connect((HOST, PORT))

username = str(input("username -> "))
password = str(input("password -> "))

wrappedSocket.send(username.encode())
wrappedSocket.send(password.encode())
ures = wrappedSocket.recv(1024).decode()
if ures == "RU":
	op = str(input("Do you want to register this as a new user[y/n] -> "))
	wrappedSocket.send(op.encode())


# CLOSE SOCKET CONNECTION
wrappedSocket.close()

