#pragma once
#include "editor.h"
#include <iostream>
#include "../../Instance.h"
#include <vector>
#include "../../AppObject.h"
#include <string>
#include <algorithm>

using namespace std;

class EditorHelper : public AppInstance {
public:
	struct AppData
	{
		const char* win_name = "Editor - Add Components";
		int w = 1000;
		int h = 700;
	};
	struct TextBoxData {
		int textsize = 30;
		int x_offset = 125;
		int y_offset = 25;
		int w = 500;
		int h = 100;
	};
	EditorHelper(editor::editor* editor) : editor(editor) {};
	void Init(const char* window_title, int w, int h);
	void HandleButtonInput(SimpleButton* button);
private:
	void AddObjectButton(string text);
	int UpdateVal;
	editor::editor* editor;
};