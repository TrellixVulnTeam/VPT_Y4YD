#include "pch.h"
#include "servers.h"

int Mainclient_thread(SOCKET c)
{
	char buf[4096];
	int bytes_r;
	ZeroMemory(buf, 4096);
	//do shit here
	bytes_r = Nserver::recivemessage(c, buf);
	if (Nserver::convert_bytes_to_string(bytes_r, buf) == "quit") {
		return NULL;
	}
	return 1;
}

void Mainport_thread(int port)
{
	Nserver::listening_thread* l1 = new Nserver::listening_thread(port);
	l1->ServerLoop(Mainclient_thread);
	l1->socket_cleanup();
}

void MainServerRun()
{
	Nserver::WSAinit();
	ThreadManager::ServerThreadManager* tmanager = new ThreadManager::ServerThreadManager(Mainport_thread, 3);
	tmanager->jointhreads();
	Nserver::cleanup();
}

SelectServer::SelectServer()
{
	server_names_m.push_back("MainServer");
	ServerList_m.push_back(MainServerRun);
}

void SelectServer::run()
{
	#define space cout << endl; 
	cout << "Here is the list of servers: " << endl;
	for (unsigned int i = 0; i < server_names_m.size(); i++) {
		cout << server_names_m[i] << endl;
	}
	string user_input;
	space
	cout << "select one >> "; cin >> user_input;
	space
	space
	int index = 0;
	cout << "this will take a little bit of time ..." << endl;
	for (unsigned int t = 0; t < server_names_m.size(); t++) {
		if (server_names_m[t] == user_input) {
			index = t;
		}
	}
	space
	cout << "running" << endl;
	ServerList_m[index]();
}
