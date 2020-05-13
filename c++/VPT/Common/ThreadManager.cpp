#include "ThreadManager.h"
ThreadManager::ServerThreadManager::ServerThreadManager(void(*port_thread)(int port), unsigned int size)
{
	number_of_threads = 0;
	for (unsigned int i = 0; i < size; i++) {
		port_threads.push_back(thread(port_thread, i));
		number_of_threads++;
	}
}

unsigned int ThreadManager::ServerThreadManager::GetPortThreadCount()
{
	return port_threads.size();
}

void ThreadManager::ServerThreadManager::jointhreads()
{
	for (unsigned int i = 0; i < GetPortThreadCount(); i++) {
		port_threads[i].join();
	}
}

void ThreadManager::ServerThreadManager::detachthreads()
{
	for (unsigned int i = 0; i < GetPortThreadCount(); i++) {
		port_threads[i].detach();
	}
}

void ThreadManager::ServerThreadManager::closeport(unsigned int port_number)
{
	//do stuff
}

void ThreadManager::ServerThreadManager::openport(unsigned int port_number,void(*port_thread)(int port))
{
	number_of_threads++;
	port_threads.push_back(thread(port_thread, port_number));
}

vector <thread> ThreadManager::ServerThreadManager::GetPortThreads()
{
	return port_threads;
}
