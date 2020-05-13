#pragma once
#include <iostream>
#include "server.h"
#include <thread>
#include "ThreadManager.h"


int Mainclient_thread(SOCKET c);
void Mainport_thread(int port);
void MainServerRun();

class SelectServer{
public:
	SelectServer();
	void run();
private:
	vector <void(*)()> ServerList_m;
	vector <string> server_names_m;
};


