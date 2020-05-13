#pragma once
#include "NetworkIncludes.h"
using namespace std;
class client
{
public:
	client(string ip);
	void client_init();
	int connect_to_server();
	void sendpacket();
	void set_buf();
	void recivepacket(int recivedbytes);
	void sendmessage(string message);
	string convert_bytes_to_string(int recivedbytes);
	int recivemessage();
	void cleanup();
	~client();
private:
	int port_m;
	SOCKET client_socket;
	sockaddr_in hint;
	string ip_m;
	char buf[4096];
};

