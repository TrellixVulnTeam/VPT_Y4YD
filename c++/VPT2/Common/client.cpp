#include "client.h"


//this works
client::client(string ip)
{
	cout << "init winsock" << endl;
	WSADATA wsaData;
	// error checking and shit
	if (WSAStartup(MAKEWORD(2, 2), &wsaData) != 0) {
		cout << "winsock init didnt work" << endl;
	}
	ip_m = ip;
	port_m = 1;
}

void client::client_init()
{
	client_socket = socket(AF_INET, SOCK_STREAM, 0);
	if (client_socket == INVALID_SOCKET) {
		cout << "error" << endl;
	}
	hint.sin_port = htons(port_m);
	hint.sin_family = AF_INET;
	//converts string to bytes and puts it as sin address
	inet_pton(AF_INET,ip_m.c_str(), &hint.sin_addr);

}

int client::connect_to_server()
{
	cout << "connecting or something" << endl;
	cout << "this will take up to a minute" << endl;
	//will make this process a lot more effecient later
	int conn_result = SOCKET_ERROR;
	while (conn_result == SOCKET_ERROR)
	{
		conn_result = connect(client_socket, (sockaddr *)&hint, sizeof(hint));
		if (conn_result == SOCKET_ERROR) {
			port_m++;
 			hint.sin_port = htons(port_m);
		}
		
	}
	
	//error checking
	cout << port_m << endl;
	return conn_result;
}

void client::sendpacket()
{
}

void client::set_buf()
{
	ZeroMemory(buf, 4096);
}


void client::recivepacket(int recivedbytes)
{
}

void client::sendmessage(string message)
{
	send(client_socket, message.c_str(), message.size() + 1, 0);
}

string client::convert_bytes_to_string(int recivedbytes)
{
	return string(buf, 0, recivedbytes);
}

int client::recivemessage()
{
	int bytes_r = recv(client_socket, buf, 4096, 0);
	return bytes_r;
}

void client::cleanup()
{
	//close cleintsocket
	closesocket(client_socket);

	//cleanup winsock
	WSACleanup();
}

client::~client()
{
}
