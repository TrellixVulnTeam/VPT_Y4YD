#pragma once
#include <string>

using namespace std;

namespace SharedMemory {

	class SharedMemoryException : public exception {
	private:
		string msg;
	public:
		SharedMemoryException(string msg) : msg(msg) {}
		string what() {
			return msg;
		}
	};
	string Create(const char* name, size_t size) throw(SharedMemoryException);
	string Open(const char* name) throw(SharedMemoryException);
	string Read(string handle);
	string Write(string handle, const char* text) throw(SharedMemoryException);
	void UnmapView(string view) throw(SharedMemoryException);
	void Close(string handle) throw(SharedMemoryException);

}