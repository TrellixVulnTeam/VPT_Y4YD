#include "pch.h"
#include "server.h"
void Nserver::WSAinit()
{
	cout << "init winsock" << endl;
	WSADATA wsaData;
	// error checking and shit
	if (WSAStartup(MAKEWORD(2, 2), &wsaData) != 0) {
		cout << "winsock init didnt work" << endl;
	}
}

void Nserver::sendmessage(string message, SOCKET c)
{
	send(c, message.c_str(), message.size(), 0);
}

int Nserver::recivemessage(SOCKET c, char buf[4096])
{
	int bytes_r = recv(c, buf, 4096, 0);
	return bytes_r;
}

string Nserver::convert_bytes_to_string(int recivedbytes, char buf[4096])
{
	return string(buf, 0, recivedbytes);
}

void Nserver::cleanup()
{
	//put other things to cleanup
	WSACleanup();
}

Nserver::listening_thread::listening_thread(int port)
{
	port_m = port;
	state = Nserver::port_status::open;
	port_log = new LOG("port_log " + to_string(port));
}


void Nserver::listening_thread::listening_socket_init()
{
	listening_socket = socket(AF_INET, SOCK_STREAM, 0);
	hint.sin_port = htons(port_m);
	hint.sin_family = AF_INET;
	hint.sin_addr.S_un.S_addr = INADDR_ANY;
	bind(listening_socket, (sockaddr*)&hint, sizeof(hint));
}

void Nserver::listening_thread::set_listening_socket_to_listen()
{
	listen(listening_socket, SOMAXCONN);
}


void Nserver::listening_thread::listening()
{
	int clientsize = sizeof(client);
	client_socket = accept(listening_socket, (sockaddr*)&client, &clientsize);
	closesocket(listening_socket);
}

int Nserver::listening_thread::ServerLoop(int(*function)(SOCKET c))
{
	int thread_result;
	while (true)
	{
		listening_socket_init();
		set_listening_socket_to_listen();
		state = port_status::open;
		port_state();
		listening();
		if (client_socket != INVALID_SOCKET) {
			GetClientInfo();
			state = port_status::taken;
			port_state();
		}
		while (true) {
			if (client_socket != INVALID_SOCKET) {
				thread_result = function(client_socket);
			}
			if (thread_result == NULL) {
				state = port_status::disconnected;
				port_state();
				break;
			}
		}
	}
	return thread_result;
}


SOCKET Nserver::listening_thread::GetClientSocket()
{
	return client_socket;
}

void Nserver::listening_thread::socket_cleanup()
{
	closesocket(client_socket);
}

Nserver::port_status Nserver::listening_thread::port_state()
{
	if (state == Nserver::port_status::taken) {
		cout << "port " << port_m << " has been taken" << " by " << host << endl;
	}
	if (state == Nserver::port_status::open){
		cout << "port " << port_m << " is open" << endl;
	}
	if (state == Nserver::port_status::disconnected) {
		cout << host << " has disconnected" << endl;
	}
	return state;
}

void Nserver::listening_thread::GetClientInfo()
{
	ZeroMemory(service, NI_MAXSERV);
	ZeroMemory(host, NI_MAXHOST);
	getnameinfo((sockaddr *)&client,sizeof(client),host, NI_MAXHOST, service, NI_MAXSERV, 0);
}

Nserver::LOG::LOG(string logname)
{
	logfile.open(logname);
}

void Nserver::LOG::cleanup()
{
	logfile.close();
}
