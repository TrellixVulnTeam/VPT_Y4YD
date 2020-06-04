#include <iostream>
#include "../../Instance.h"
#include <vector>
#include "../../Component.h"
#include <cstdlib> 
#include <ctime> 
using namespace std;

namespace client {
	struct buttondata;
	struct AppData
	{
		const char* win_name = "VPT";
		int w = 1000;
		int h = 700;
	};
	class client : public AppInstance{
	public:
		client();
		void Init(const char* window_title, int w, int h);
		void Draw();
		void Update();
		void Input();
		AppData appdata;
	private:
		Text* text;
		Button* button;
	};
	struct buttondata {
		
	};
};