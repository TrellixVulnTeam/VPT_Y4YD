// Server.cpp : This file contains the 'main' function. Program execution begins and ends there.
//

//THIS FILE IS AN EXAMPLE OF HOW TO CREATE A SERVER WITH MY FILES; DELETE IT WHEN USING MY BRANCH
#include <iostream>
#include "server.h"
#include <thread>
#include "ThreadManager.h"
using namespace std;
int client_thread(SOCKET c){
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
void port_thread(int port){
	Nserver::listening_thread* l1 = new Nserver::listening_thread(port);
	l1->ServerLoop(client_thread);
	l1->socket_cleanup();
}
int main1()
{
	/*Nserver::WSAinit();
	Nserver::listening_thread* l1 = new Nserver::listening_thread(11);
	l1->listening_socket_init();
	l1->set_listening_socket_to_listen();
	l1->ServerLoop(client_thread);
	l1->socket_cleanup();
	Nserver::cleanup();*/
	Nserver::WSAinit();
	ThreadManager::ServerThreadManager* tmanager = new ThreadManager::ServerThreadManager(port_thread, 3);
	tmanager->jointhreads();
	Nserver::cleanup();
	return 0;
}
