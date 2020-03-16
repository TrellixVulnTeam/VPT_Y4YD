#include "ThreadManager.h"
ThreadManager::ServerThreadManager::ServerThreadManager(void(*port_thread)(int port), unsigned int size)
{
	for (unsigned int i = 0; i < size; i++) {
		port_threads.push_back(thread(port_thread, i));
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