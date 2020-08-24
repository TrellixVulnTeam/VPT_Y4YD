import socket
import ssl
HOST, PORT ='172.18.0.2', 4562

# CREATE SOCKET
sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
sock.settimeout(10)
# WRAP SOCKET
wrappedSocket = ssl.wrap_socket(sock, ssl_version=ssl.PROTOCOL_TLSv1)
# CONNECT AND PRINT REPLY
wrappedSocket.connect((HOST, PORT))

wrappedSocket.send(".py".encode())
wrappedSocket.send("test".encode())
wrappedSocket.send("print('go fuck your self')".encode())


# CLOSE SOCKET CONNECTION
wrappedSocket.close()

