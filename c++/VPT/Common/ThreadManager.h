//testing2 because was acedently logged into wrong account richardgehant
#pragma once
#include <stdio.h>
#include <iostream>
#include <string>
#include <vector>
#include <thread>
#include "server.h"
#include <stdlib.h>
namespace ThreadManager {
	class ServerThreadManager {
	public:
		ServerThreadManager(void(*port_thread)(int port), unsigned int size);
		unsigned int GetPortThreadCount();
		void jointhreads();
		void detachthreads();
		void closeport(unsigned int port_number);
		void openport(unsigned int port_number, void(*port_thread)(int port));
		vector <thread> GetPortThreads();
	private:
		vector <thread> port_threads;
		unsigned int number_of_threads;
	};
}
