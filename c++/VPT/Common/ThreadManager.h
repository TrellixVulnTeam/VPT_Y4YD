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
	private:
		vector <thread> port_threads;
	};
}