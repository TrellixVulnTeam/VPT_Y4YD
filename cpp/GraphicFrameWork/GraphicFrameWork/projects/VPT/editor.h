#pragma once
#include <iostream>
#include "../../Instance.h"
#include <vector>
#include "../../Component.h"
#include <cstdlib> 
#include <ctime> 
#include "RelativePaths.h"
#include "Utils.h"
#include "PlaceableObjects.h"

namespace editor {
	struct AppObjSelected;
	struct AppData
	{
		const char* win_name = "Editor";
		int w = 1000;
		int h = 700;
	};
	
	struct AppObjSelected {
		bool selected;
		unsigned int index;
	};

	struct TextBoxData {
		int textsize = 30;
		int x_offset = 125;
		int y_offset = 25;
		int w = 500;
		int h = 100;
	};
	class editor : public AppInstance {
	public:
		editor() {};
		void Init(const char* window_title, int w, int h);
		void Draw();
		void Update();
		void Input(bool wasEvent, SDL_Event e);
		AppObjSelected Selected();
		string message_m;
	private:
		int UpdateVal;
	};
	
}