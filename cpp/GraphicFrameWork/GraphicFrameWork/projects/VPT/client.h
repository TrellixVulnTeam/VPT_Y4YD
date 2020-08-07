#pragma once
#include <iostream>
#include "../../Instance.h"
#include <vector>
#include "../../Component.h"
#include <cstdlib> 
#include <ctime> 
#include <mutex>
#include <condition_variable>
#include <queue>
#include "RelativePaths.h"
#include "../../Scene.h"
#include "PacketId.h"
#include "Env.h"
#include "Packet.h"
using namespace std;

namespace client {
	struct AppData
	{
		const char* win_name = "VPT";
		int w = 1000;
		int h = 700;
	};
	struct TextFieldData {
		int textsize = 30;
		int x_offset = 125;
		int y_offset = 25;
		int w = 500;
		int h = 100;
	};
	struct buttondata {

	};


	 
	class client : public AppInstance{
	public:
		client();
		void Init(const char* window_title, int w, int h);
		void Draw();
		void Update();
		void Input(bool wasEvent, SDL_Event e);
		void PacketProcess();
		void Loop();
		static void QueuePacket(Packet *p);
		static Packet* PollPacketQueue();
		static void sendPacket(jobject packet);
		static Packet* Request(jobject packet);
		AppData appdata;
	private:
		TextField* tf;
		Text *text;
		TextField* tf1; 
		Button* button;
	};
};