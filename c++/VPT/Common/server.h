#pragma once
#include "NetworkIncludes.h"
using namespace std;
namespace Nserver {
	enum class port_status;
	class LOG;
	void WSAinit();
	void sendmessage(string message, SOCKET c);
	int recivemessage(SOCKET c, char buf[4096]);
	string convert_bytes_to_string(int recivedbytes, char buf[4096] );
	void cleanup();
	class listening_thread{
	public:
		listening_thread(int port);
		void listening_socket_init();
		void set_listening_socket_to_listen();
		void listening();
		int ServerLoop(int(*function)(SOCKET c));
		SOCKET GetClientSocket();
		void socket_cleanup();
		port_status port_state();
		void GetClientInfo();
	private:
		int port_m;
		SOCKET listening_socket; 
		SOCKET client_socket;
		sockaddr_in hint, client;
		port_status state;
		char host[NI_MAXHOST];
		char service[NI_MAXSERV];
		LOG *port_log;
	};
	enum class port_status {taken, open, disconnected};
	class LOG {
	public:
		LOG(string logname);
		template <typename Gtype>
		void update(Gtype updatecontent);
		void cleanup();
	private:
		ofstream logfile;
	};
	template<typename Gtype>
	inline void LOG::update(Gtype updatecontent)
	{
		if (logfile.is_open()) {
			logfile << updatecontent << "/n";
		}
	}
}
