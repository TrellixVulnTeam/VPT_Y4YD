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

namespace TestV {
	struct AppData
	{
		const char* win_name = "TestV";
		int w = 1000;
		int h = 700;
	};

	class TestV : public AppInstance {
	public:
		TestV();
		void Init(const char* window_title, int w, int h);
		void Draw();
		void Update();
		void Input();
	};
}